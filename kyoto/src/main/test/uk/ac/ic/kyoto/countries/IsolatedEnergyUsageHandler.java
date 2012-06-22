package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;

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
			throws NotEnoughCarbonOutputException{
		
		if (amount >= 0) {
			if (amount <= country.carbonOutput) {
				country.energyOutput -= amount;
				country.carbonOutput -= amount;
			}
			else{
				throw new NotEnoughCarbonOutputException();
			}
		}
		else{
			throw new IllegalArgumentException("Energy cannot be reduced by a negative amount");
		}
	}
	
	/**
	 * Calculates the cost of investing in carbon industry
	 * @param growth
	 * The increase in carbon output
	 * @return
	 * The cost for the country
	 */
	public double calculateCostOfInvestingInCarbonIndustry (double growth){
		
		double cost;
		if (growth >= 0){
			cost = growth * GameConst.getCarbonInvestmentPrice();
		} else {
			throw new IllegalArgumentException("It is impossible to invest in negative carbon industry growth");
		}

		return cost;
	}
	
	/**
	 * Calculates the increase of carbon output
	 * @param cost
	 * The amount of money to be spent on carbon industry growth
	 * @return increase of carbon output
	 */
	public double calculateCarbonIndustryGrowth (double cost){
		double growth;
		if (cost >= 0){
			growth = cost / GameConst.getCarbonInvestmentPrice();
		}else{
			throw new IllegalArgumentException("It is impossible to invest negative sum in industry growth");
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
			throws NotEnoughCashException{
		double growth;

		growth = calculateCarbonIndustryGrowth(investment);
		if (investment <= country.availableToSpend) {
			country.carbonOutput += growth;
			country.energyOutput += growth;
			country.availableToSpend -= investment;
		}
		else {
			throw new NotEnoughCashException(country.availableToSpend, investment);
		}
	}
}