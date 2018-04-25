package hr.aspevec.asm.strategy.services;

import hr.aspevec.asm.strategy.ServiceStrategy;

public interface ASMTestingService extends ServiceStrategy {

	String getResult();
	
	String getResult(String input);
	
}
