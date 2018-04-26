package hr.aspevec.asm.strategy.annotation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.asm.ClassWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import hr.aspevec.asm.strategy.DynamicClassLoader;
import hr.aspevec.asm.strategy.SelectionLayerByteCodeWriter;

@Component
public class SelectionLayerProcessor implements BeanDefinitionRegistryPostProcessor {

	private DynamicClassLoader loader = new DynamicClassLoader();
	private SelectionLayerByteCodeWriter byteCodeWriter = new SelectionLayerByteCodeWriter();

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (String beanName : registry.getBeanDefinitionNames()) {
			try {
				final String className = registry.getBeanDefinition(beanName).getBeanClassName();
				if (className != null) {
					processAnnotation(registry, beanName, className);
				}
			}
			catch (final ClassNotFoundException e) {
				System.out.println(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private void processAnnotation(final BeanDefinitionRegistry registry, final String beanName, final String className) throws ClassNotFoundException
	{
		final Class<? extends Object> selectionClass = Class.forName(className);
		EnableStrategySelectionLayer[] selectionLayers = (EnableStrategySelectionLayer[]) selectionClass.getAnnotationsByType(EnableStrategySelectionLayer.class);

		if (ArrayUtils.isNotEmpty(selectionLayers))
		{		
			for (EnableStrategySelectionLayer selectionLayer : selectionLayers) {
				createSelectionLayer(registry, beanName, selectionLayer);
			}
		}
	}

	private void createSelectionLayer(final BeanDefinitionRegistry registry, final String beanName, EnableStrategySelectionLayer selectionLayer) {
		Class<?> definition = selectionLayer.definition();
		ClassWriter writer = byteCodeWriter.getClassWriter(definition);
		Class<?> selectorClazz = loader.defineClass(definition.getName() + "StrategySelector", writer.toByteArray());

		BeanDefinitionBuilder selectorBdb = BeanDefinitionBuilder.genericBeanDefinition(selectorClazz);
		selectorBdb.getRawBeanDefinition().setPrimary(true);
		selectorBdb.addPropertyReference("defaultStrategy", beanName);

		registry.registerBeanDefinition(selectorBdb.getBeanDefinition().getBeanClassName(), selectorBdb.getBeanDefinition());	
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0) throws BeansException {
		// TODO Auto-generated method stub	
	}

}
