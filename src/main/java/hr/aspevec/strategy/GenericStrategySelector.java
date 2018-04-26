package hr.aspevec.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import hr.aspevec.CountryEnum;

public class GenericStrategySelector<T extends ServiceStrategy> implements InitializingBean
{

    @Autowired
    private List<T> strategies = new ArrayList<>();

    @Autowired
    private T defaultStrategy;

    private Map<String, T> lookup = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        mapStrategyToEachCountry();
    }

    private void mapStrategyToEachCountry()
    {
        lookup = new HashMap<>();
        Stream.of(CountryEnum.values())
              .forEach(c -> assignStrategyToCountry(c));
    }

    private void assignStrategyToCountry(CountryEnum country)
    {
        T strategy = strategies.stream()
                               .filter(s -> s.isMatch(country.getCode()))
                               .findFirst()
                               .orElse(defaultStrategy);
        lookup.put(country.getCode(), strategy);
    }

    protected T selectStrategy(String countryCode)
    {
        return lookup.get(countryCode);
    }

    protected T selectStrategy()
    {
        return selectStrategy(getCountryCodeFromSession());
    }	

    /**
     * TODO implement logic for accesing country of current user - session
     */
    private String getCountryCodeFromSession()
    {
        Random generator = new Random();
        switch (generator.nextInt(3)) {
        case 0:
            return CountryEnum.UK.getCode();
        case 1:
            return CountryEnum.FR.getCode();
        case 2:
            return CountryEnum.DE.getCode();
        default:
            throw new RuntimeException("Unknown country for current user");
        }
    }

}