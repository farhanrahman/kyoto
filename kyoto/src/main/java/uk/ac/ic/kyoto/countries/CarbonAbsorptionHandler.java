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
	public double getInvestmentRequired(double carbonOffset) throws Exception {
		return getInvestmentRequired(carbonOffset,this.country.arableLandArea);
	}
	
	/**
	 * Returns the investment necessary to increase carbon absorption by specified amount.
	 * The cost of absorption of a single ton of CO2 is linearly proportional to occupied area measure.
	 * 
	 * @param carbonAbsorptionChange
	 * The amount of carbon absorption which we want to price.
	 * 
	 * @param arableLandArea
	 * The current arable land area.
	 * 
	 * @return
	 * Cost of absorbing carbon by the specified amount.
	 */
	public double getInvestmentRequired(double carbonAbsorptionChange,double arableLandArea) throws Exception {
		double investmentRequired;
		
		try {
			// Calculate forest area required to absorb given amount of carbon
			double forestArea = getForestAreaRequired(carbonAbsorptionChange);
			
			// Calculate occupied area measure after and before investment
			double occupiedAreaMeasureBefore = calculateOccupiedAreaMeasure(arableLandArea, country.landArea);
			double occupiedAreaMeasureAfter = calculateOccupiedAreaMeasure((arableLandArea + forestArea), country.landArea);
			
			// Get average price of single ton of additional carbon absorption
			double averageUnitPrice = (GameConst.CARBON_ABSORPTION_PRICE_MIN +
										( (GameConst.CARBON_ABSORPTION_PRICE_MAX - GameConst.CARBON_ABSORPTION_PRICE_MIN) *
										  (occupiedAreaMeasureBefore + occupiedAreaMeasureAfter) /
										  (2) ) );
			
			// Calculate the investment that is required
			investmentRequired = (averageUnitPrice * carbonAbsorptionChange);
		}
		catch (Exception e) {
			throw new Exception("getCost function error: " + e);
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
		return getCarbonAbsorptionChange(investmentAmount,country.arableLandArea);
	}
	
	/**
	 * Returns the additional carbon absorption for given investment amount.
	 * Rounds down to the nearest integer, which means that actual absorption might be slightly higher.
	 * 
	 * @param Investment amount
	 * 
	 * @param arableLandArea current arable land area
	 * 
	 * @return Change in carbon absorption from specified cost
	 */
	public final double getCarbonAbsorptionChange(double investmentAmount,double arableLandArea) throws Exception {
		double carbonAbsorptionChange;
		double tempInvestmentAmount;

		try {
			// Initialise variables to zero
			carbonAbsorptionChange = 0;
			tempInvestmentAmount = 0;
			
			// Increase carbon output until the cost is higher than investment
			while (tempInvestmentAmount < investmentAmount) {
				carbonAbsorptionChange += 1;
				tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);
			}
		}
		catch (Exception e) {
			throw new Exception("getCarbonOutputChange function error: " + e.getMessage());
		}
		
		return carbonAbsorptionChange;
	}
	//TODO
	public final double getCarbonAbsorptionChangeNew(double investmentAmount,double arableLandArea) throws Exception {
		double carbonAbsorptionChange;
		
		try {
			double carbonDiff = country.carbonOutput;
			
			carbonAbsorptionChange = carbonDiff/2;
			
			double tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);

			for (int i=0; i<20; i++) {
				carbonDiff/=2;
				
				//If value is higher, lower our estimate. Else, increase it.

				if (tempInvestmentAmount < investmentAmount) {
					carbonAbsorptionChange += carbonDiff;
					tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);

				}
				else if (tempInvestmentAmount > investmentAmount) {
					carbonAbsorptionChange -= carbonDiff;
					tempInvestmentAmount = getInvestmentRequired(carbonAbsorptionChange, arableLandArea);
				}
			}
			
		}
		catch (Exception e) {
			throw new Exception("getCarbonOutputChange function error: " + e.getMessage());
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
			throw new Exception("getCarbonAbsorptionChange error: " + e.getMessage());
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
				}
				else {
					throw new NotEnoughLandException();
				}
			}
			else {
				throw new NotEnoughCashException();
			}
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
		
		if (arableArea <= totalArea) {
			occupiedAreaMeasure = (1 - (arableArea / totalArea));
		}
		else {
			throw new Exception("calculateOccupiedAreaMeasure function error: arableLandArea is greater than landArea");
		}
		
		return occupiedAreaMeasure;
	}
	
}