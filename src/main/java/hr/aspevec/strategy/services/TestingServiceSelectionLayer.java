package hr.aspevec.strategy.services;

import org.springframework.stereotype.Component;

import hr.aspevec.strategy.GenericStrategySelector;

@Component
public class TestingServiceSelectionLayer extends GenericStrategySelector<TestingServiceStrategy> implements TestingService {

	@Override
	public String getResult() {
		return selectStrategy().getResult();
	}

	@Override
	public String getResult(String input) {
		return selectStrategy().getResult(input);
	}

}
