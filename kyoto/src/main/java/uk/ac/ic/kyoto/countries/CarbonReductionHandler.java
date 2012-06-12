package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.math.QuadraticEquation;

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
	public final long getCost(long carbonOutputChange) throws Exception {
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
		catch (Exception e) {
			throw new Exception("getCost function error: " + e);
		}
		
		return cost;
	}
	
	/**
	 * Returns the reduction of carbon output
	 * for a specified cost of investment.
	 * 
	 * @param investment cost
	 * 
	 * @return the change in carbon output from specified cost
	 */
	public final double getCarbonOutputChange(long cost) throws Exception {
		double carbonOutputChange;

		try {
			// Calculate the clean industry rate before the investment
			double cleanIndustryBefore = calculateCleanIndustryMeasure(country.carbonOutput, country.energyOutput);
			
			//Solve the quadratic equation to find the change of clean industry
			double a = GameConst.CARBON_REDUCTION_COEFF / 2;
			double b = GameConst.CARBON_REDUCTION_COEFF * cleanIndustryBefore + GameConst.CARBON_REDUCTION_OFFSET;
			double c = - cost;
			QuadraticEquation equation = new QuadraticEquation(a, b, c);
			System.out.println("Quadradtic roots a: "+equation.getRootOne()+" b: "+equation.getRootTwo());
			
			double cleanIndustryChange = Math.max(equation.getRootOne(), equation.getRootTwo() );
			
			carbonOutputChange = calculateCarbonOutput(cleanIndustryChange, country.energyOutput);
		}
		catch (Exception e) {
			throw new Exception("getCarbonOutputChange function error: " + e);
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
	public final void invest(long investment) throws Exception, NotEnoughCarbonOutputException, NotEnoughCashException {
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
			throw new Exception("invest function error: " + e);
		}
		
	}
	
	//================================================================================
    // Private methods for getting clean industry from carbon output and vice versa
    //================================================================================
	
	/**
	 * Calculates the clean industry rate for specified carbon output and energy output.
	 */
	private double calculateCleanIndustryMeasure(long carbonOutput, long energyOutput) throws Exception {
		double cleanIndustry;
		
		try {
			if (carbonOutput <= energyOutput)
				cleanIndustry = 1 - ((double)carbonOutput / (double)energyOutput);
			else {
				throw new Exception("carbonOutput is greater than energyOutput");
			}
		}
		catch (Exception e) {
			throw new Exception("calculateCleanIndustryMeasure function error " + e);
		}
		
		return cleanIndustry;
	}
	
	private long calculateCarbonOutput(double cleanIndustry, long energyOutput) throws Exception {
		long carbonOutput;
		
		try {
			carbonOutput = (long) ((double)energyOutput * (1 - cleanIndustry));
		}
		catch (Exception e) {
			throw new Exception("calculateCarbonOutput error: " + e);
		}
		
		return carbonOutput;
	}
}