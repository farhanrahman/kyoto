package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Adam
 */
public final class EnergyUsageHandler {
	private final AbstractCountry country;

	/**Create instance of EnergyUsageHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	EnergyUsageHandler(AbstractCountry abstractCountry) {
		this.country = abstractCountry;
		
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
		if (amount < this.country.carbonOutput && amount > 0) {
			this.country.energyOutput -= amount;
			this.country.carbonOutput -= amount;
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
	protected void investInCarbonIndustry(long carbon) throws Exception{

		long cost = calculateCostOfInvestingInCarbonIndustry(carbon);
		if (cost > this.country.availableToSpend) {
			this.country.carbonOutput += carbon;
			this.country.energyOutput += carbon;
			this.country.availableToSpend -= cost;
		}
		else {
			throw new NotEnoughCashException();
		}
	}
}