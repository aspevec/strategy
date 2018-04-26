package hr.aspevec.strategy.services;

import hr.aspevec.strategy.ServiceStrategy;

public interface TestingServiceStrategy extends ServiceStrategy  {

    String getResult();

    String getResult(String input);

}
