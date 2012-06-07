package uk.ac.ic.kyoto.countries;

class EnergyUsageHandler {
	private final AbstractCountry abstractCountry;

	/**Create instance of EnergyUsageHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	EnergyUsageHandler(AbstractCountry abstractCountry) {
		this.abstractCountry = abstractCountry;
		
	}
	
	/**
	 * Reduces both the energyOutput and carbonOutput of the country
	 * It can be used to limit carbonOuput without any financial cost
	 * As the energyOuput goes down, the GDP growth goes down too
	 * 
	 * @param amount
	 * 
	 * Amount of energyOuput that should be reduced
	 * It has to be positive and lower than the total carbonOuput
	 */
	protected void reduceEnergyOutput (long amount) throws IllegalArgumentException{
		if (amount < this.abstractCountry.carbonOutput && amount > 0) {
			this.abstractCountry.energyOutput -= amount;
			this.abstractCountry.carbonOutput -= amount;
		}
		else
			throw new IllegalArgumentException("Specified amount should be > 0 and < carbonOutput");
	}
	
	/**
	 * Calculates the cost of investing in carbon industry
	 * @param carbon
	 * The expected increase in carbon output
	 * @return
	 * The cost for the country
	 */
	protected long calculateCostOfInvestingInCarbonIndustry (long carbon){
		return (long) (carbon * GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	/**
	 * Calculates the increase of carbon output
	 * @param cost
	 * The amount of money to be spent on carbon industry growth
	 * @return
	 * The increase of carbon output
	 */
	protected long calculateCarbonIndustryGrowth (long cost){
		return (long) (cost / GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	/**
	 * Invests in carbon industry.
	 * Carbon output and energy output of the country go up
	 * @param carbon
	 * The increase of the carbon output that will be achieved.
	 */
	protected void investInCarbonIndustry(long carbon){
		try {
			long cost = calculateCostOfInvestingInCarbonIndustry(carbon);
			if (cost > this.abstractCountry.availableToSpend) {
				this.abstractCountry.carbonOutput += carbon;
				this.abstractCountry.energyOutput += carbon;
				this.abstractCountry.availableToSpend -= cost;
			}
			else {
				// TODO log that there is not enough money
			}
		}
		catch (Exception e) {
			// TODO log the exception
		}
	}
}