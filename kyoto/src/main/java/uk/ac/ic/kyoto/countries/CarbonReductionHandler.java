package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam
 */
public final class CarbonReductionHandler{
	
	private final AbstractCountry country;

	/**
	 * Create instance of CarbonReductionHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	CarbonReductionHandler(AbstractCountry abstractCountry) {
		this.country = abstractCountry;
	}

	/**
	 * Returns the cost of investment required to
	 * reduce dirty industry by a specified amount of tons of carbon.
	 * 
	 * @param carbonOutputChange
	 * @return cost of reducing carbon by said amount
	 */
	public final long getCost(double carbonOutputChange){
		long cost;
		try {
			cost = (long) (GameConst.CARBON_REDUCTION_COEFF * carbonOutputChange / this.country.energyOutput);
		}
		catch (ArithmeticException e) {
			country.logger.warn("Division by 0 error: " + e);
			cost = Long.MAX_VALUE;
		}
		return cost;
	}
	
	/**
	 * Returns the reduction of carbon output
	 * for a specified cost of investment.
	 * 
	 * @param currency
	 * @return the change in carbon output from said cost
	 */
	public final double getCarbonOutputChange(long cost) {
		double carbonOutputChange;

		carbonOutputChange = this.country.energyOutput * cost / GameConst.CARBON_REDUCTION_COEFF;

		return carbonOutputChange;
	}
	
	/**
	 * Executes carbon reduction investment.
	 * On success, will reduce Carbon Output of a country keeping the Energy Output constant
	 * On failure, will throw Exception.
	 * 
	 * @param investment
	 * @throws Exception
	 */
	public final void invest(long investment) throws Exception{
		if (investment <= this.country.availableToSpend){
			this.country.availableToSpend -= investment;
			this.country.carbonOutput -= getCarbonOutputChange(investment);
		}
		else {
			throw new NotEnoughCashException();
		}
	}
}