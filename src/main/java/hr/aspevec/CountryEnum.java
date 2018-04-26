package hr.aspevec;

/**
 * Enumerator with codes for all the countries in the system.
 */
public enum CountryEnum {

    UK("UK"),
    FR("FR"),
    DE("DE");

    private String code;

    private  CountryEnum(String code) {
        this.code = code;
    }

    public String getCode()
    {
        return this.code;
    }
}
