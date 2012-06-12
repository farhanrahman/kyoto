package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam, Piotr
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

	//================================================================================
    // Public functions to calculate the changes
    //================================================================================
	
	/**
	 * Returns the cost of investment required to
	 * reduce dirty industry by a specified amount of tons of carbon.
	 * The cost per unit increases linearly with increase of clean industry %
	 * 
	 * @param carbonOutputChange
	 * The amount of carbon reduction which we want to price
	 * 
	 * @return
	 * cost of reducing carbon by the specified amount.
	 * 
	 * @throws NotEnoughCarbonOutputException 
	 * when it is impossible for a given country to reduce carbon by the specified amount
	 * as they don't produce that much carbon
	 */
	public final double getInvestmentRequired(double carbonOutputChange) throws Exception {
		double investmentRequired;
		
		try {			
			// Calculate the clean industry measure after and before investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(country.carbonOutput, country.energyOutput);
			double cleanIndustryAfter = calculateCleanIndustryMeasure((country.carbonOutput + carbonOutputChange), country.energyOutput);
			
			// Get average price of single ton of carbon output reduction
			double averageUnitPrice = (GameConst.CARBON_REDUCTION_PRICE_MIN +
									    ( (GameConst.CARBON_REDUCTION_PRICE_MAX - GameConst.CARBON_REDUCTION_PRICE_MIN) * 
									      (cleanIndustryBefore + cleanIndustryAfter) /
									      (2) ) );
			
			// Calculate the investment that is required
			investmentRequired = (averageUnitPrice * carbonOutputChange);
		}
		catch (Exception e) {
			throw new Exception("getInvestmentRequired function error: " + e.getMessage());
		}
		
		return investmentRequired;
	}
	
	/**
	 * Returns the reduction of carbon output
	 * for a specified cost of investment.
	 * 
	 * @param investment cost
	 * 
	 * @return the change in carbon output from specified cost
	 */
	public final double getCarbonOutputChange(double investmentAmount) throws Exception {
		double carbonOutputChange;
		double tempInvestmentAmount;

		try {
			carbonOutputChange = 0;
			tempInvestmentAmount = 0;
			// Increase carbon output until the cost is higher than investment
			while (tempInvestmentAmount < investmentAmount) {
				carbonOutputChange += 1;
				tempInvestmentAmount = getInvestmentRequired(carbonOutputChange);
			}
		}
		catch (Exception e) {
			throw new Exception("getCarbonOutputChange function error: " + e.getMessage());
		}
		
		return carbonOutputChange;
	}
		
	/**
	 * Executes carbon reduction investment.
	 * On success, will reduce Carbon Output of a country keeping the Energy Output constant
	 * On failure, will throw Exception.
	 * 
	 * @param investment
	 * 
	 * @throws Exception
	 */
	public final void invest(double investment) throws Exception, NotEnoughCarbonOutputException, NotEnoughCashException {
		double carbonOutputChange;
		
		try {
			carbonOutputChange = getCarbonOutputChange(investment);
			
			if (investment <= this.country.availableToSpend){
				if (carbonOutputChange <= this.country.carbonOutput) {
					this.country.availableToSpend -= investment;
					this.country.carbonOutput -= carbonOutputChange;
				}
				else {
					throw new NotEnoughCarbonOutputException();
				}
			}
			else {
				throw new NotEnoughCashException();
			}
		}
		catch (Exception e) {
			throw new Exception("invest function error: " + e.getMessage());
		}
		
	}
	
	//================================================================================
    // Private methods for getting clean industry from carbon output and vice versa
    //================================================================================
	
	/**
	 * Calculates the clean industry rate for specified carbon output and energy output.
	 */
	private double calculateCleanIndustryMeasure(double carbonOutput, double energyOutput) throws Exception {
		double cleanIndustry;
		
		try {
			if (carbonOutput <= energyOutput)
				cleanIndustry = (1 - (carbonOutput / energyOutput));
			else {
				throw new Exception("carbonOutput is greater than energyOutput");
			}
		}
		catch (Exception e) {
			throw new Exception("calculateCleanIndustryMeasure function error " + e.getMessage());
		}
		
		return cleanIndustry;
	}

}