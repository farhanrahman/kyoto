package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;

/**
 * 
 * @author Piotr, Nikunj
 */
public final class IsolatedCarbonReductionHandler{
	
	private final IsolatedAbstractCountry country;

	/**
	 * Create instance of CarbonReductionHandler.
	 * 
	 * @param abstractCountry
	 * Specify on which country will the handler operate.
	 */
	IsolatedCarbonReductionHandler(IsolatedAbstractCountry abstractCountry) {
		this.country = abstractCountry;
	}
	
	/**
	 * Calls getInvestmentRequired function with actual carbonOutput and energyOutput of a country.
	 * 
	 * @param carbonOutputChange
	 * The amount of carbon reduction which we want to price.
	 * 
	 * @return
	 * Cost of reducing carbon by the specified amount.
	 */
	public final double getInvestmentRequired(double carbonOutputChange) {
		return getInvestmentRequired(carbonOutputChange, country.carbonOutput, country.energyOutput);
	}
	
	/**
	 * Returns the investment necessary to reduce carbon output by specified amount, given carbon and energy output.
	 * The cost of reduction by a single ton of CO2 is linearly proportional to clean industry measure.
	 * 
	 * @param carbonOutputChange
	 * The amount of carbon reduction which we want to price.
	 * 
	 * @param carbonOutput
	 * The current country carbon output.
	 * 
	 * @param energyOutput
	 * The current country energy output.
	 * 
	 * @return
	 * Cost of reducing carbon by the specified amount.
	 */
	public final double getInvestmentRequired(double carbonOutputChange, double carbonOutput, double energyOutput) {
		double investmentRequired;
		
			// Calculate the clean industry measure after and before investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(carbonOutput, energyOutput);
			double cleanIndustryAfter = calculateCleanIndustryMeasure((carbonOutput - carbonOutputChange), energyOutput);
			
			// Get average price of single ton of carbon output reduction
			double averageUnitPrice = (GameConst.getCarbonReductionPriceMin() +
									    ( (GameConst.getCarbonReductionPriceMax() - GameConst.getCarbonReductionPriceMin()) * 
									      (cleanIndustryBefore + cleanIndustryAfter) /
									      (2) ) ); 
			
			// Calculate the investment that is required
			investmentRequired = (averageUnitPrice * carbonOutputChange);
		
		return investmentRequired;
	}
	
	/**
	 * Calls getCarbonAbsorptionChange with actual carbonOutput and energyOutput of the country.
	 * 
	 * @param Investment amount
	 * Amount of money that we want to spend on investment.
	 * 
	 * @return
	 * APPROXIMATE Change in carbon output from specified cost.
	 */
	public final double getCarbonOutputChange(double investmentAmount) {
		return getCarbonOutputChange(investmentAmount, country.carbonOutput, country.energyOutput);
	}
	
	/**
	 * Returns the reduction of carbon output for given investment amount.
	 * Uses binary search, which is efficient.
	 * The result might not be 100% accurate - this is only an approximation function.
	 * 
	 * @param investmentAmount
	 * Amount of money that we want to spend on investment.
	 * 
	 * @param carbonOutput
	 * Current carbon output of a country for which we do the calculations.
	 * 
	 * @param energyOutput
	 * Current energy output of a country for which we do the calculations.
	 * 
	 * @return
	 * Change in carbon output achieved with specified investment.
	 */
	public final double getCarbonOutputChange(double investmentAmount, double carbonOutput, double energyOutput) {
		double carbonOutputChange;

		double carbonDiff = carbonOutput / 2;
		
		carbonOutputChange = carbonDiff;
		
		double tempInvestmentAmount = getInvestmentRequired(carbonOutputChange, carbonOutput, energyOutput);
		
		for (int i = 0; i < 30; i++) {
			carbonDiff /= 2;
			
			// If value is higher, lower our estimate, else increase it
			if (tempInvestmentAmount < investmentAmount) {
				carbonOutputChange += carbonDiff;
				tempInvestmentAmount = getInvestmentRequired(carbonOutputChange,carbonOutput,energyOutput);
			}
			else if (tempInvestmentAmount > investmentAmount) {
				carbonOutputChange -= carbonDiff;
				tempInvestmentAmount = getInvestmentRequired(carbonOutputChange,carbonOutput,energyOutput);
			}
		}
		
		return carbonOutputChange;
	}
		
	/**
	 * Executes carbon reduction investment.
	 * On success, will reduce carbon output of a country keeping the energy output constant.
	 * On failure, will throw exception.
	 * 
	 * @param carbonOutputChange
	 * Decrease in carbon output.
	 */
	public final void investInCarbonReduction(double carbonOutputChange) throws NotEnoughCarbonOutputException, NotEnoughCashException {
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
				throw new NotEnoughCashException(this.country.availableToSpend, investmentAmount);
			}
		}
		catch (NotEnoughCarbonOutputException e) {
			throw e;
		}
		catch (NotEnoughCashException e) {
			throw e;
		}
	}
	
	/**
	 * Calculates the clean industry rate for specified carbon output and energy output.
	 * 
	 * @param carbonOutput
	 * Carbon output of the country.
	 * 
	 * @param energyOutput
	 * Total energy output of the country.
	 * 
	 * @return
	 * Measure of clean industry of the country.
	 */
	private double calculateCleanIndustryMeasure(double carbonOutput, double energyOutput){
		double cleanIndustry;
		
		if (carbonOutput <= energyOutput) {
			cleanIndustry = (1 - (carbonOutput / energyOutput));
		}
		else {
			throw new RuntimeException("calculateCleanIndustryMeasure function error: carbonOutput is greater than energyOutput");
		}
		
		return cleanIndustry;
	}
}