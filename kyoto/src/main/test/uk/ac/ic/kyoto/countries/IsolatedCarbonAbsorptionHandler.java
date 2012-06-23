package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;

/**
 * 
 * @author Piotr, Nikunj
 */
public final class IsolatedCarbonAbsorptionHandler {
	
	private final IsolatedAbstractCountry country;

	/**
	 * Create instance of CarbonAbsorbtionHandler.
	 * 
	 * @param abstractCountry
	 * Specify on which country will the handler operate.
	 */
	IsolatedCarbonAbsorptionHandler(IsolatedAbstractCountry abstractCountry) {
		this.country = abstractCountry;
	}
	
	/**
	 * Calls getInvestmentRequired function with actual arableLandArea of a country.
	 * 
	 * @param carbonAbsorptionChange
	 * The amount of carbon absorption which we want to price.
	 * 
	 * @return
	 * Cost of absorbing carbon by the specified amount.
	 * @throws NotEnoughLandException 
	 */
	public final double getInvestmentRequired(double carbonAbsorptionChange) throws NotEnoughLandException{
		return getInvestmentRequired(carbonAbsorptionChange, this.country.arableLandArea);
	}
	
	/**
	 * Returns the investment necessary to increase carbon absorption by specified amount, given arable land.
	 * The cost of absorption of a single ton of CO2 is linearly proportional to occupied area measure.
	 * 
	 * @param carbonAbsorptionChange
	 * The amount of carbon absorption which we want to price.
	 * 
	 * @param arableLandArea
	 * Current arable land area for which we do the calculations. 
	 * 
	 * @return
	 * Cost of absorbing carbon by the specified amount.
	 * @throws NotEnoughLandException 
	 */
	public final double getInvestmentRequired(double carbonAbsorptionChange, double arableLandArea) throws NotEnoughLandException {

		double investmentRequired;
		
		// Calculate forest area required to absorb given amount of carbon
		double forestArea = getForestAreaRequired(carbonAbsorptionChange);
		
		if (forestArea > this.country.arableLandArea) {
			throw new NotEnoughLandException();
		}
		
		// Calculate occupied area measure after and before investment
		double occupiedAreaMeasureBefore = calculateOccupiedAreaMeasure(arableLandArea, country.landArea);
		double occupiedAreaMeasureAfter = calculateOccupiedAreaMeasure((arableLandArea - forestArea), country.landArea);
		
		// Get average price of single ton of additional carbon absorption
		double averageUnitPrice = (GameConst.getCarbonAbsorptionPriceMin() +
									( (GameConst.getCarbonAbsorptionPriceMax() - GameConst.getCarbonAbsorptionPriceMin()) *
									  (occupiedAreaMeasureBefore + occupiedAreaMeasureAfter) /
									  (2) ) );
		
		// Calculate the investment that is required
		investmentRequired = (averageUnitPrice * carbonAbsorptionChange);
			
		return investmentRequired;
	}
	
	/**
	 * Calls getCarbonAbsorptionChange with actual arableLandArea of the country.
	 * 
	 * @param Investment amount
	 * Amount of money that we want to spend on investment.
	 * 
	 * @return
	 * Change in carbon absorption achieved with specified investment.
	 * @throws NotEnoughLandException 
	 */
	public final double getCarbonAbsorptionChange(double investmentAmount) throws NotEnoughLandException {
		return getCarbonAbsorptionChange(investmentAmount, country.arableLandArea);
	}
	
	/**
	 * Returns the additional carbon absorption for given investment amount, given arable land.
	 * Uses binary search, which is efficient.
	 * The result might not be 100% accurate - this is only an approximation function.
	 * 
	 * @param investmentAmount
	 * Amount of money that we want to spend on investment.
	 * 
	 * @param arableLandArea
	 * Current arable land area for which we do the calculations. 
	 * 
	 * @return
	 * Change in carbon absorption achieved with specified investment.
	 * @throws NotEnoughLandException 
	 */
	public final double getCarbonAbsorptionChange(double investmentAmount, double arableLandArea) throws NotEnoughLandException {
		double carbonAbsorptionChange;
		
		double carbonDiff = country.carbonOutput - country.carbonAbsorption;
		
		while (getForestAreaRequired(carbonDiff) > arableLandArea) {
			carbonDiff*=0.9;
		}
		
		carbonDiff/=2;
		
		carbonAbsorptionChange = carbonDiff;
		
		double tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);

		for (int i = 0; i < 30; i++) {
			carbonDiff /= 2;
			
			//If value is higher, lower our estimate, else increase it
			if (tempInvestmentAmount < investmentAmount) {
				carbonAbsorptionChange += carbonDiff;
			}
			else if (tempInvestmentAmount > investmentAmount) {
				carbonAbsorptionChange -= carbonDiff;
			}
			tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);
		}
		
		return carbonAbsorptionChange;
	}
	
	/**
	 * Calculates the forest area needed to increase absorption by a given amount.
	 * 
	 * @param carbonAbsorptionChange
	 * Change in carbon absorption.
	 * 
	 * @return
	 * Forest area required.
	 */
	public final double getForestAreaRequired(double carbonAbsorptionChange){		
		return (carbonAbsorptionChange * GameConst.getForestCarbonAbsorption());
	}
	
	/**
	 * Executes carbon absorption investment.
	 * On success, will increase carbon absorption of a country.
	 * On failure, will throw exception.
	 * 
	 * @param carbonAbsorptionChange
	 * Increase in carbon absorption.
	 */

	public final void investInCarbonAbsorption(double carbonAbsorptionChange) throws NotEnoughLandException, NotEnoughCashException {
		double investmentAmount;
		
		// Calculate the investment necessary to increase carbon absorption by specified amount
		investmentAmount = getInvestmentRequired(carbonAbsorptionChange);
		
		// Calculate the forest area needed to plant required number of trees
		double areaRequired = getForestAreaRequired(carbonAbsorptionChange);
		
		// If enough cash and arable land, proceed with the investment
		if (investmentAmount <= this.country.availableToSpend){
			if (areaRequired <= this.country.arableLandArea) {
				this.country.availableToSpend -= investmentAmount;
				this.country.carbonAbsorption += carbonAbsorptionChange;
				this.country.arableLandArea -= areaRequired;
			}
			else {
				throw new NotEnoughLandException();
			}
		}
		else {
			throw new NotEnoughCashException(this.country.availableToSpend, investmentAmount);
		}
	}
	
	/**
	 * Calculates the occupied area rate for specified arable land area and total land area.
	 * 
	 * @param arableArea
	 * Area of the country that is potentially arable.
	 * 
	 * @param totalArea
	 * Total area of the country.
	 * 
	 * @return
	 * Measure of occupied area of the country.
	 */
	private double calculateOccupiedAreaMeasure(double arableArea, double totalArea) {
		double occupiedAreaMeasure;
		
		if (arableArea <= totalArea) {
			occupiedAreaMeasure = (1 - (arableArea / totalArea));
		}
		else {
			throw new RuntimeException("calculateOccupiedAreaMeasure function error: arableLandArea is greater than landArea");
		}
		
		return occupiedAreaMeasure;
	}
	
}