package hr.aspevec.asm.strategy;

public interface ServiceStrategy
{
	/**
	 * Matching specific service implementation - strategy.
	 *
	 * @param code for country
	 *
	 * @return true if specific implementation matches to requested code.
	 */
	boolean isMatch(String code);

}