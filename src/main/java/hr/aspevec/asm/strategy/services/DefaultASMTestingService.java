package hr.aspevec.asm.strategy.services;

import org.springframework.stereotype.Component;

import hr.aspevec.asm.strategy.annotation.EnableStrategySelectionLayer;

@Component
@EnableStrategySelectionLayer(definition = ASMTestingService.class)
public class DefaultASMTestingService implements ASMTestingService {

	@Override
	public String getResult() {
		return "DefaultResult-ASM";
	}

	@Override
	public String getResult(String input) {
		return "DefaultResult-ASM " + input;
	}

	@Override
	public boolean isMatch(String code) {
		return false;
	}

}
