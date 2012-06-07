package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam
 */
public final class CarbonReductionHandler{
	
	private final AbstractCountry abstractCountry;

	/**
	 * Create instance of CarbonReductionHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	CarbonReductionHandler(AbstractCountry abstractCountry) {
		this.abstractCountry = abstractCountry;
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
		
		cost = (long) (GameConst.CARBON_REDUCTION_COEFF * carbonOutputChange / this.abstractCountry.energyOutput);
		
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
		
		carbonOutputChange = this.abstractCountry.energyOutput * cost / GameConst.CARBON_REDUCTION_COEFF;
		
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
		if (investment < this.abstractCountry.availableToSpend){
			this.abstractCountry.availableToSpend -= investment;
			this.abstractCountry.carbonOutput -= getCarbonOutputChange(investment);
		}
		else {
			throw new Exception("Investment is greater than available cash to spend");
		}
	}
}