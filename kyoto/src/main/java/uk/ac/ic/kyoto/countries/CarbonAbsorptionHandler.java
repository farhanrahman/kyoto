package uk.ac.ic.kyoto.countries;
/**
 * 
 * @author Stuart, Adam, Piotr
 */
public final class CarbonAbsorptionHandler {
	
	// TODO EXTENSIVE testing
	
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
	 * Returns the cost of investment required to
	 * obtain a given number of carbon.
	 * 
	 * @param carbonOffset
	 */
	public long getCost(long carbonOffset) {
		double neededLand;
		long noBlocks;
		long totalCost;
		double tempLandArea;
		
		try {
			neededLand = carbonOffset / GameConst.FOREST_CARBON_ABSORPTION;
			noBlocks = (long) (neededLand / GameConst.FOREST_BLOCK_SIZE);
			totalCost = 0;
			tempLandArea = this.country.arableLandArea;
			
			for (int i=0; i < noBlocks; i++) {
				totalCost += getBlockCost(tempLandArea);
				tempLandArea -= GameConst.FOREST_BLOCK_SIZE;
			}
		}
		catch (Exception e) {
			//country.logger.warn("Problem with calculating the cost of investment: " + e);
			totalCost = Long.MAX_VALUE; // This is to prevent country actually investing for free in case of error
		}
		
		return totalCost;
	}
	
	/**
	 * Returns number of carbon credits earned for a 
	 * given investment.
	 * 
	 * @param investment
	 */
	public long getCarbonAbsorption(double investment) {
		long totalCost;
		double tempArableLandArea;
		long carbonAbsorption;
		
		try {
			totalCost = 0;
			tempArableLandArea = country.arableLandArea;
			
			while (totalCost <= investment && tempArableLandArea >= GameConst.FOREST_BLOCK_SIZE) {
				totalCost += getBlockCost(tempArableLandArea);
				tempArableLandArea -= GameConst.FOREST_BLOCK_SIZE;
			}
			
			carbonAbsorption = (long) (GameConst.FOREST_CARBON_ABSORPTION * (country.arableLandArea-tempArableLandArea) );
		}
		catch (Exception e) {
			//country.logger.warn("Problem with calculating absorption for given investment: " + e);
			carbonAbsorption = 0;
		}
		
		return carbonAbsorption;
	}
	
	/**
	 * Returns the cost of a single block of forest.
	 * Depends on the available land area.
	 * 
	 * @param landArea
	 */
	private long getBlockCost(double landArea) {
		long blockCost;
		
		try {
			if (landArea > 0) {
				blockCost = (long) (GameConst.CARBON_ABSORPTION_COEFF * GameConst.FOREST_BLOCK_SIZE / landArea);
			}
			else {
				//country.logger.warn("Trying to find a cost of a block of area for non-positive area left");
				blockCost = Long.MAX_VALUE;
			}
		}
		catch (Exception e) {
			//country.logger.warn("Problem with calculating cost of forest block: " + e);
			blockCost = Long.MAX_VALUE;
		}
		
		return blockCost;
	}
	
	/**
	 * Executes carbon absorption investment</br>
	 * 
	 * On success, will reduce GDP and increase.</br>
	 * On failure, will throw Exception.</br>
	 * 
	 * @param investment
	 * @throws Exception
	 */
	public void invest(double investment) throws Exception {
		long additionalAbsorption;
		double arableAreaUsed;
		
		try {
			// Calculate how much Carbon Offset will be gained through the investment
			additionalAbsorption = getCarbonAbsorption(investment);
			// Calculate how much arable area has to be used during the investment
			arableAreaUsed = additionalAbsorption / GameConst.FOREST_CARBON_ABSORPTION;
			
			if (investment <= country.availableToSpend) {
				
				if (arableAreaUsed <= country.arableLandArea) {
					country.availableToSpend -= investment;
					country.carbonAbsorption += additionalAbsorption;
					country.arableLandArea -= arableAreaUsed;
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
			//country.logger.warn("Problem with investing in carbon absorption: " + e);
		}
	}
}