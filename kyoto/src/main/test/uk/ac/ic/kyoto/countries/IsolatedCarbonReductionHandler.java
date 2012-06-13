package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam, Piotr
 */
public final class IsolatedCarbonReductionHandler{
	
	private final IsolatedAbstractCountry country;

	/**
	 * Create instance of CarbonReductionHandler
	 * 
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	IsolatedCarbonReductionHandler(IsolatedAbstractCountry abstractCountry) {
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
		return getInvestmentRequired(carbonOutputChange,country.carbonOutput, country.energyOutput);
	}
	
	/**
	 * Returns the investment necessary to reduce carbon output by specified amount.
	 * The cost of reduction by a single ton of CO2 is linearly proportional to clean industry measure.
	 * 
	 * @param carbonOutputChange
	 * The amount of carbon reduction which we want to price
	 * 
	 * @param carbonOutput
	 * The current country carbon output
	 * 
	 * @param energyOutput
	 * The current country energy output
	 * 
	 * @return
	 * Cost of reducing carbon by the specified amount.
	 */
	public final double getInvestmentRequired(double carbonOutputChange, double carbonOutput, double energyOutput) throws Exception {
		double investmentRequired;
		
		try {			
			// Calculate the clean industry measure after and before investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(carbonOutput, energyOutput);
			double cleanIndustryAfter = calculateCleanIndustryMeasure((carbonOutput + carbonOutputChange), energyOutput);
			
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
		return getCarbonOutputChange(investmentAmount, country.carbonOutput, country.energyOutput);
	}
	
	/**
	 * Returns the reduction of carbon output for given investment amount.
	 * Rounds down to the nearest integer, which means that actual reduction might be slightly higher.
	 * 
	 * @param Investment amount to invest
	 * @param carbonOutput current carbon output
	 * @param energyOutput current energy output
	 * 
	 * @return Change in carbon output from specified cost
	 */
	public final double getCarbonOutputChange(double investmentAmount, double carbonOutput, double energyOutput) throws Exception {
		double carbonOutputChange;

		try {
			double carbonDiff = carbonOutput;
			
			carbonOutputChange = carbonDiff/2;
			
			double tempInvestmentAmount = getInvestmentRequired(carbonOutputChange,carbonOutput,energyOutput);
			
			for (int i=0; i<20; i++) {
				carbonDiff/=2;
				
				//If value is higher, lower our estimate. Else, increase it.
				if (tempInvestmentAmount < investmentAmount) {
					carbonOutputChange += carbonDiff;
					tempInvestmentAmount = getInvestmentRequired(carbonOutputChange,carbonOutput,energyOutput);
				}
				else if (tempInvestmentAmount > investmentAmount) {
					carbonOutputChange -= carbonDiff;
					tempInvestmentAmount = getInvestmentRequired(carbonOutputChange,carbonOutput,energyOutput);
				}
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
		
		if (carbonOutput <= energyOutput) {
			cleanIndustry = (1 - (carbonOutput / energyOutput));
		}
		else {
			throw new Exception("CalculateCleanIndustryMeasure function error: carbonOutput is greater than energyOutput");
		}
		
		return cleanIndustry;
	}
}