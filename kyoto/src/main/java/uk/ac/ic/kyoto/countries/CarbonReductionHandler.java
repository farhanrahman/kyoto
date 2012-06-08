package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.math.QuadraticEquation;

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
	 * @return
	 * cost of reducing carbon by the specified amount.
	 * @throws NotEnoughCarbonOutputException 
	 * when it is impossible for a given country to reduce carbon by the specified amount
	 * as they don't produce that much carbon
	 * 
	 */
	public final long getCost(long carbonOutputChange) throws NotEnoughCarbonOutputException {
		long cost;
		try {			
			// Calculate the initial clean industry rate and the change during investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(country.carbonOutput, country.energyOutput);
			double cleanIndustryChange = calculateCleanIndustryMeasure(carbonOutputChange, country.energyOutput);
			
			// Use the formula y = (a * x + b + a / 2 * dx) * dx
			// to find the total cost
			// see documentation for the full description
			
			cost = (long) ((GameConst.CARBON_REDUCTION_COEFF * cleanIndustryBefore + 
							GameConst.CARBON_REDUCTION_OFFSET + 
							GameConst.CARBON_REDUCTION_COEFF * cleanIndustryChange / 2)
							* cleanIndustryChange);
		}
		catch (NotEnoughCarbonOutputException e) {
			throw new NotEnoughCarbonOutputException();
		}
		return cost;
	}
	
	/**
	 * Returns the reduction of carbon output
	 * for a specified cost of investment.
	 * 
	 * @param investment cost
	 * @return the change in carbon output from specified cost
	 */
	public final double getCarbonOutputChange(long cost) {
		double carbonOutputChange=0;

		try {
			// Calculate the clean industry rate before the investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(country.carbonOutput, country.energyOutput);
			
			//Solve the quadratic equation to find the change of clean industry
			double a = GameConst.CARBON_REDUCTION_COEFF / 2;
			double b = GameConst.CARBON_REDUCTION_COEFF * cleanIndustryBefore + GameConst.CARBON_REDUCTION_OFFSET;
			double c = - cost;
			QuadraticEquation equation = new QuadraticEquation(a, b, c);
			
			double cleanIndustryChange = Math.max(equation.getRootOne(), equation.getRootTwo() );
			carbonOutputChange = calculateCarbonOutput(cleanIndustryChange, country.energyOutput);
		}
		catch (Exception e) {
			System.out.println("Carbon reduction has died");
			e.printStackTrace();
		}
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
		// calculate the decrease in carbon output
		double carbonOutputChange = getCarbonOutputChange(investment);
		
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
	
	//================================================================================
    // Private methods for getting clean industry from carbon output and vice versa
    //================================================================================
	
	/**
	 * Calculates the clean industry rate for specified carbon output and energy output
	 */
	private double calculateCleanIndustryMeasure(long carbonOutput, long energyOutput) throws NotEnoughCarbonOutputException{
		double cleanIndustry = 1;
		try {
			if (carbonOutput <= energyOutput)
				cleanIndustry = 1 - (carbonOutput / energyOutput);
			else {
				//country.logger.warn("It is impossible for carbonOutput to exceed energyOutput");
				// Move error logging to country when it catches exception
				throw new NotEnoughCarbonOutputException();
			}
		}
		catch (ArithmeticException e) {
			//country.logger.error("Specified energyOuput was 0: " + e);
			System.out.println("Calculate clean industry measure error");
		}
		return cleanIndustry;
	}
	
	private long calculateCarbonOutput(double cleanIndustry, long energyOutput) {
		long carbonOutput;
		carbonOutput = (long) (energyOutput * (1 - cleanIndustry) ); // TODO implement exception handling
		return carbonOutput;
	}
}