package uk.ac.ic.kyoto.countries;

/**
 * 
 * @author Stuart, Adam, Piotr
 */
public final class CarbonAbsorptionHandler {
	
	private final AbstractCountry country;

	/**
	 * Create instance of CarbonAbsorbtionHandler
	 * 
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	CarbonAbsorptionHandler(AbstractCountry abstractCountry) {
		this.country = abstractCountry;
	}
	
	/**
	 * Returns the investment necessary to increase carbon absorption by specified amount.
	 * The cost of absorption of a single ton of CO2 is linearly proportional to occupied area measure.
	 * 
	 * @param carbonAbsorptionChange
	 * The amount of carbon absorption which we want to price.
	 * 
	 * @return
	 * Cost of absorbing carbon by the specified amount.
	 */
	public double getInvestmentRequired(double carbonAbsorptionChange) throws Exception {
		double investmentRequired;
		
		try {
			// Calculate forest area required to absorb given amount of carbon
			double forestArea = getForestAreaRequired(carbonAbsorptionChange);
			
			// Calculate occupied area measure after and before investment
			double occupiedAreaMeasureBefore = calculateOccupiedAreaMeasure(country.arableLandArea, country.landArea);
			double occupiedAreaMeasureAfter = calculateOccupiedAreaMeasure((country.arableLandArea + forestArea), country.landArea);
			
			// Get average price of single ton of additional carbon absorption
			double averageUnitPrice = (GameConst.CARBON_ABSORPTION_PRICE_MIN +
										( (GameConst.CARBON_ABSORPTION_PRICE_MAX - GameConst.CARBON_ABSORPTION_PRICE_MIN) *
										  (occupiedAreaMeasureBefore + occupiedAreaMeasureAfter) /
										  (2) ) );
			
			// Calculate the investment that is required
			investmentRequired = (averageUnitPrice * carbonAbsorptionChange);
		}
		catch (Exception e) {
			throw new Exception("getInvestmentRequired function error: " + e);
		}
		
		return investmentRequired;
	}
	
	/**
	 * Returns the additional carbon absorption for given investment amount.
	 * Rounds down to the nearest integer, which means that actual absorption might be slightly higher.
	 * 
	 * @param Investment amount
	 * 
	 * @return Change in carbon absorption from specified cost
	 */
	public final double getCarbonAbsorptionChange(double investmentAmount) throws Exception {
		double carbonAbsorptionChange;
		double tempInvestmentAmount;

		try {
			// Initialise variables to zero
			carbonAbsorptionChange = 0;
			tempInvestmentAmount = 0;
			
			// Increase carbon output until the cost is higher than investment
			while (tempInvestmentAmount < investmentAmount) {
				carbonAbsorptionChange += 1;
				tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange);
			}
		}
		catch (Exception e) {
			throw new Exception("getCarbonAbsorptionChange function error: " + e.getMessage());
		}
		
		return carbonAbsorptionChange;
	}
	
	/**
	 * Calculates the forest area needed to increase absorption by a given amount.
	 * 
	 * @param Change in carbon absorption
	 * 
	 * @return Forest area required
	 */
	public final double getForestAreaRequired(double carbonAbsorptionChange) throws Exception {
		double forestArea;
		
		try {
			forestArea = (carbonAbsorptionChange * GameConst.FOREST_CARBON_ABSORPTION);
		}
		catch (Exception e) {
			throw new Exception("getForestAreaRequired error: " + e.getMessage());
		}
		
		return forestArea;
	}
	
	/**
	 * Executes carbon absorption investment.
	 * On success, will increase carbon absorption of a country.
	 * On failure, will throw exception.
	 * 
	 * @param Carbon absorption increase
	 * 
	 * @throws Exception
	 */
	public final void investInCarbonAbsorption(double carbonAbsorptionChange) throws Exception, NotEnoughCarbonOutputException, NotEnoughCashException {
		double investmentAmount;
		
		try {
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
				throw new NotEnoughCashException();
			}
		}
		catch (NotEnoughLandException e) {
			throw e;
		}
		catch (NotEnoughCashException e) {
			throw e;
		}
		catch (Exception e) {
			throw new Exception("investInCarbonAbsorption function error: " + e.getMessage());
		}
		
	}
	
	/**
	 * Calculates the occupied area rate for specified arable land area and total land area.
	 */
	private double calculateOccupiedAreaMeasure(double arableArea, double totalArea) throws Exception {
		double occupiedAreaMeasure;
		
		try {
			if (arableArea <= totalArea) {
				occupiedAreaMeasure = (1 - (arableArea / totalArea));
			}
			else {
				throw new Exception("arableLandArea is greater than landArea");
			}
		}
		catch (Exception e) {
			throw new Exception("calculateOccupiedAreaMeasure function error " + e.getMessage());
		}
		
		return occupiedAreaMeasure;
	}
	
}