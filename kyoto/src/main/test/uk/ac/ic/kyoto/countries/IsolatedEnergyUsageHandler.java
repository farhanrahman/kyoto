package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Adam
 */
public final class IsolatedEnergyUsageHandler {
	private final IsolatedAbstractCountry country;

	/**Create instance of EnergyUsageHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	IsolatedEnergyUsageHandler(IsolatedAbstractCountry abstractCountry) {
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
	public void reduceEnergyOutput (double amount)
			throws NotEnoughCarbonOutputException, IllegalArgumentException, Exception {
		try {
			if (amount > 0) {
				if (amount < country.carbonOutput) {
					country.energyOutput -= amount;
					country.carbonOutput -= amount;
				}
				else
					throw new NotEnoughCarbonOutputException();
			}
			else
				throw new IllegalArgumentException("Energy cannot be reduced by a negative amount");
		}
		catch (Exception e) {
			throw new Exception("reduceEnergyOutput function error: " + e.getMessage());
		}
	}
	
	/**
	 * Calculates the cost of investing in carbon industry
	 * @param growth
	 * The expected increase in carbon output
	 * @return
	 * The cost for the country
	 */
	public double calculateCostOfInvestingInCarbonIndustry (double growth)
			throws IllegalArgumentException, Exception {
		double cost;
		try {
			if (growth > 0)
				cost = growth * GameConst.CARBON_INVESTMENT_PRICE;
			else
				throw new IllegalArgumentException("It is impossible to invest in negative carbon industry growth");
		}
		catch (Exception e) {
			throw new Exception("calculateCostOfInvestingInCarbonIndustry function error: " + e.getMessage());
		}
		return cost;
	}
	
	/**
	 * Calculates the increase of carbon output
	 * @param cost
	 * The amount of money to be spent on carbon industry growth
	 * @return increase of carbon output
	 */
	public double calculateCarbonIndustryGrowth (double cost)
			throws IllegalArgumentException, Exception {
		double growth;
		try {
			if (cost > 0)
				growth = cost / GameConst.CARBON_INVESTMENT_PRICE;
			else
				throw new IllegalArgumentException("It is impossible to invest negative sum in industry growth");
		}
		catch (Exception e) {
			throw new Exception("calculateCarbonIndustryGrowth function error: " + e.getMessage());
		}
		return growth;
	}
	
	/**
	 * Invests in carbon industry.
	 * Carbon output and energy output of the country go up
	 * @param carbon
	 * The increase of the carbon output that will be achieved.
	 */
	public final void investInCarbonIndustry(double investment)
			throws NotEnoughCashException, NotEnoughCarbonOutputException, Exception{
		double growth;
		try {
			growth = calculateCarbonIndustryGrowth(investment);
			if (investment < country.availableToSpend) {
				country.carbonOutput += growth;
				country.energyOutput += growth;
				country.availableToSpend -= investment;
			}
			else {
				throw new NotEnoughCashException();
			}
		}
		catch (NotEnoughCarbonOutputException e) {
			throw new NotEnoughCarbonOutputException();
		}
		catch (Exception e) {
			throw new Exception("investInCarbonIndustry function error: " + e.getMessage());
		}
	}
}