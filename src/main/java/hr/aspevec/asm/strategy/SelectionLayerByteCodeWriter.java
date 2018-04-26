package hr.aspevec.asm.strategy;

import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ACC_SUPER;
import static org.springframework.asm.Opcodes.ALOAD;
import static org.springframework.asm.Opcodes.CHECKCAST;
import static org.springframework.asm.Opcodes.INVOKEINTERFACE;
import static org.springframework.asm.Opcodes.INVOKESPECIAL;
import static org.springframework.asm.Opcodes.INVOKEVIRTUAL;
import static org.springframework.asm.Opcodes.RETURN;
import static org.springframework.asm.Opcodes.V1_8;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.springframework.asm.ClassWriter;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;

public class SelectionLayerByteCodeWriter {

    private static final String SELECTOR_SUFIX = "StrategySelector";

    private static final Class<?> ROOT_STRATEGY_INTERFACE = ServiceStrategy.class;
    private static final Class<?> ROOT_STRATEGY_CLASS = GenericStrategySelector.class;
    private static final String SELETOR_CLASS_SIGNATURE_FORMAT = "%s<%s>;%s";
    private static final String STRATEGY_SELECTION_METHOD_NAME = "selectStrategy";

    public ClassWriter getClassWriter(Class<?> selectionInterface) {
        ClassWriter writer = createClassWriter(selectionInterface);

        configureClassConstructor(writer);
        configureClassMethods(writer, selectionInterface);

        writer.visitEnd();

        return writer;
    }

    private ClassWriter createClassWriter(Class<?> selectionInterface) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(V1_8, 
                     ACC_PUBLIC + ACC_SUPER, 
                     getPackageAndClassNameForSelectionLayer(selectionInterface), 
                     getSignatureForSelectionLayer(selectionInterface), 
                     replaceDotWithSlashInPath(ROOT_STRATEGY_CLASS.getName()),
                     new String[] {replaceDotWithSlashInPath(selectionInterface.getName())});
        return writer;
    }

    /**
     * We need to explicitly defina a constructor. If the compiler was generating code for us, 
     * and we didn't include an explicit call to a constructor in the superclass, the compiler 
     * would put one in. Here, we need to do that ourselves.
     */
    private void configureClassConstructor(ClassWriter writer) {
        MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, 
                                                       "<init>", 
                                                       "()V", 
                                                       null, 
                                                       null);

        constructor.visitCode();            // Start the code for this method
        constructor.visitVarInsn(ALOAD, 0); // Load "this" onto the stack

        constructor.visitMethodInsn(INVOKESPECIAL,  // Invoke an instance method (non-virtual)
                                    replaceDotWithSlashInPath(ROOT_STRATEGY_CLASS.getName()),   // Class on which the method is defined
                                    "<init>",   // Name of the method
                                    "()V",      // Descriptor
                                    false);     // Is this class an interface?

        constructor.visitInsn(RETURN);      // End the constructor method
        constructor.visitMaxs(1, 1);        // Specify max stack and local vars
    }

    private void configureClassMethods(ClassWriter writer, Class<?> selectionInterface) {		
        Stream.of(selectionInterface.getDeclaredMethods())
        .forEach(m -> configureClassMethod(writer, selectionInterface, m));
    }

    private void configureClassMethod(ClassWriter writer, Class<?> selectionInterface, Method method) {
        MethodVisitor methodVisitor = defineMethodVisitor(writer, method);
        defineMethodImplementation(methodVisitor, selectionInterface, method);
    }

    private MethodVisitor defineMethodVisitor(ClassWriter writer, Method method) {
        return writer.visitMethod(ACC_PUBLIC,                       // public method
                                  method.getName(),                 // name
                                  Type.getMethodDescriptor(method), // descriptor of the method - defines parameter and return types
                                  null,                             // signature (null means not generic)
                                  getMethodExceptions(method));     // exceptions (array of strings)
    }

    private void defineMethodImplementation(MethodVisitor methodVisitor, Class<?> selectionInterface, Method method) {
        methodVisitor.visitCode();              // Start the code for this method
        methodVisitor.visitVarInsn(ALOAD, 0);   // Load "this" onto the stack

        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,                                                // Invoke an instance method
                                      getPackageAndClassNameForSelectionLayer(selectionInterface),  // Class on which the method is defined
                                      STRATEGY_SELECTION_METHOD_NAME,                               // Name of the method
                                      "()" + Type.getDescriptor(ROOT_STRATEGY_INTERFACE),           // Descriptor
                                      false);                                                       // Is this class an interface?

        methodVisitor.visitTypeInsn(CHECKCAST, replaceDotWithSlashInPath(selectionInterface.getName()));    //Check if cast to our interface is possible

        for (int i = 1; i <= method.getParameterTypes().length; i++) {
            methodVisitor.visitVarInsn(ALOAD, i);   //Load parameter from stack
        }

        methodVisitor.visitMethodInsn(INVOKEINTERFACE,                                          // Invoke an interface method
                                      replaceDotWithSlashInPath(selectionInterface.getName()),  // Class on which the method is defined
                                      method.getName(),                                         // Name of the method
                                      Type.getMethodDescriptor(method),                         // Descriptor
                                      true);                                                    // Is this class an interface?

        methodVisitor.visitInsn(Opcodes.ARETURN);   // End this method
        methodVisitor.visitMaxs(1 + method.getParameterTypes().length, 1 + method.getParameterTypes().length);  // Specify max stack and local vars
    }

    /**
     * Defining the package and class name for new class (selection layer)
     * 
     * Add {@linkplain SELECTOR_SUFIX} at the end of the path and name of selectionInterface, then replace dot with slashs.
     * 
     * Example: 
     * 
     * INPUT: selectionInterface = hr.aspevec.services.TestingService
     * 
     * STEP 1: selectionInterface + SELECTOR_SUFIX = hr.aspevec.services.TestingServiceStrategySelector
     * 
     * STEP 2: replace dot with slash using replaceDotWithSlashInPath
     * 
     * OUTPUT: hr/aspevec/services/TestingServiceStrategySelector
     * 
     * @param selectionInterface class that represents main interface for which we are building the strategy selector
     * 
     * @return package and class name for new strategy selector class
     */
    private String getPackageAndClassNameForSelectionLayer(Class<?> selectionInterface) {
        return replaceDotWithSlashInPath(selectionInterface.getName() + SELECTOR_SUFIX);
    }

    /**
     * When class that we are creating, extends or implement something this signature needs to be specified.
     * 
     * Our class will: extends GenericStrategySelector<TestingService> implements TestingService
     * 
     * This needs to be represented as "Lhr/aspevec/GenericStrategySelector<Lhr/aspevec/services/TestingService;>;Lhr/aspevec/services/TestingService;";
     * 
     * We are using {@link Type} and getDescriptor method which returns Lhr/aspevec/GenericStrategySelector; for given class
     * 
     * Since that method doesn't return anything for generics there are some specifics in format and with replacing ; 
     * 
     * @param selectionInterface class that represents main interface for which we are building the strategy selector
     * 
     * @return class signature
     */
    private String getSignatureForSelectionLayer(Class<?> selectionInterface) {
        return String.format(SELETOR_CLASS_SIGNATURE_FORMAT, 
                             Type.getDescriptor(ROOT_STRATEGY_CLASS).replaceAll(";", ""),
                             Type.getDescriptor(selectionInterface),
                             Type.getDescriptor(selectionInterface));
    }

    private String replaceDotWithSlashInPath(String path) {
        return path.replace('.', '/');
    }

    private String[] getMethodExceptions(Method method) {
        return Stream.of(method.getExceptionTypes())
                     .map(e->replaceDotWithSlashInPath(e.getName()))
                     .toArray(size -> new String[size]);
    }

}

