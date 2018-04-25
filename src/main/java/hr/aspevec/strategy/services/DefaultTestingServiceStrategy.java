package hr.aspevec.strategy.services;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DefaultTestingServiceStrategy implements TestingServiceStrategy {

	@Override
	public String getResult() {
		return "DefaultResult";
	}

	@Override
	public String getResult(String input) {
		return "DefaultResult " + input;
	}

	@Override
	public boolean isMatch(String code) {
		return false;
	}

}
