package hr.aspevec.asm.strategy.services;

import org.springframework.stereotype.Component;

import hr.aspevec.CountryEnum;

@Component
public class UKASMTestingService extends DefaultASMTestingService {

	@Override
	public String getResult(String input) {
		return "UKResult-ASM " + input;
	}

	@Override
	public boolean isMatch(String code) {
		return CountryEnum.UK.getCode().equals(code);
	}
	
}
