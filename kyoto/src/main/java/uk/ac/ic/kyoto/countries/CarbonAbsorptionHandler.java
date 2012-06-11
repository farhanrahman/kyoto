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
	public long getCost(long carbonOffset) throws Exception {
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
			throw new Exception("getCost function error");
		}
		
		return totalCost;
	}
	
	/**
	 * Returns number of carbon credits earned for a 
	 * given investment.
	 * 
	 * @param investment
	 */
	public long getCarbonAbsorption(double investment) throws Exception {
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
			throw new Exception("getCarbonAbsorption function error");
		}
		
		carbonAbsorption = (long) (GameConst.FOREST_CARBON_ABSORPTION * (country.arableLandArea - tempArableLandArea) );

		return carbonAbsorption;
	}
	
	/**
	 * Returns the cost of a single block of forest.
	 * Depends on the available land area.
	 * 
	 * @param landArea
	 */
	private long getBlockCost(double landArea) throws Exception {
		long blockCost;
		
		try {
			if (landArea > 0) {
				blockCost = (long) (GameConst.CARBON_ABSORPTION_COEFF * GameConst.FOREST_BLOCK_SIZE / landArea);
			}
			else {
				throw new Exception("Trying to find a cost of a block of area for non-positive area left");
			}
		}
		catch (Exception e) {
			throw new Exception("getBlockCost function error");
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
		catch (Exception e) {
			throw new Exception("invest function error");
		}
	}
}