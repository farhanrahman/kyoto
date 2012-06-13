package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam, Piotr
 */
public final class CarbonReductionHandler{
	
	private final AbstractCountry country;

	/**
	 * Create instance of CarbonReductionHandler
	 * 
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	CarbonReductionHandler(AbstractCountry abstractCountry) {
		this.country = abstractCountry;
	}
	
	/**
	 * Returns the investment necessary to reduce carbon output by specified amount.
	 * The cost of reduction by a single ton of CO2 is linearly proportional to clean industry measure.
	 * 
	 * @param carbonOutputChange
	 * The amount of carbon reduction which we want to price.
	 * 
	 * @return
	 * Cost of reducing carbon by the specified amount.
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
	 * Returns the reduction of carbon output for given investment amount.
	 * Rounds down to the nearest integer, which means that actual reduction might be slightly higher.
	 * 
	 * @param Investment amount
	 * 
	 * @return Change in carbon output from specified cost
	 */
	public final double getCarbonOutputChange(double investmentAmount) throws Exception {
		double carbonOutputChange;
		double tempInvestmentAmount;

		try {
			// Initialise variables to zero
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
	 * On success, will reduce carbon output of a country keeping the energy output constant.
	 * On failure, will throw exception.
	 * 
	 * @param Carbon output reduction
	 * 
	 * @throws Exception
	 */
	public final void investInCarbonReduction(double carbonOutputChange) throws Exception, NotEnoughCarbonOutputException, NotEnoughCashException {
		double investmentAmount;
		
		try {
			// Calculate the investment necessary to reduce carbon output by specified amount
			investmentAmount = getInvestmentRequired(carbonOutputChange);
			
			// If enough cash and carbon output, proceed with the investment
			if (investmentAmount <= this.country.availableToSpend){
				if (carbonOutputChange <= this.country.carbonOutput) {
					this.country.availableToSpend -= investmentAmount;
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
			throw new Exception("investInCarbonReduction function error: " + e.getMessage());
		}
		
	}
	
	/**
	 * Calculates the clean industry rate for specified carbon output and energy output.
	 */
	private double calculateCleanIndustryMeasure(double carbonOutput, double energyOutput) throws Exception {
		double cleanIndustry;
		
		try {
			if (carbonOutput <= energyOutput) {
				cleanIndustry = (1 - (carbonOutput / energyOutput));
			}
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