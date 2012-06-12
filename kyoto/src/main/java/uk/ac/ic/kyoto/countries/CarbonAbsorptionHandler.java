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
	public double getCost(double carbonOffset) throws Exception {
		double neededLand;
		long noBlocks;
		double totalCost;
		double tempLandArea;
		
		try {
			neededLand = carbonOffset / GameConst.FOREST_CARBON_ABSORPTION;
			noBlocks = Math.round(neededLand / GameConst.FOREST_BLOCK_SIZE);
			totalCost = 0;
			tempLandArea = this.country.arableLandArea;
			
			for (int i=0; i < noBlocks; i++) {
				totalCost += getBlockCost(tempLandArea);
				tempLandArea -= GameConst.FOREST_BLOCK_SIZE;
			}
		}
		catch (Exception e) {
			throw new Exception("getCost function error: " + e);
		}
		
		return totalCost;
	}
	
	/**
	 * Returns number of carbon credits earned for a 
	 * given investment.
	 * 
	 * @param investment
	 */
	public double getCarbonAbsorption(double investment) throws Exception {
		double totalCost;
		double tempArableLandArea;
		double carbonAbsorption;
		
		try {
			totalCost = 0;
			tempArableLandArea = country.arableLandArea;
			
			while (totalCost <= investment && tempArableLandArea >= GameConst.FOREST_BLOCK_SIZE) {
				totalCost += getBlockCost(tempArableLandArea);
				tempArableLandArea -= GameConst.FOREST_BLOCK_SIZE;
			}
			
			carbonAbsorption = GameConst.FOREST_CARBON_ABSORPTION * (country.arableLandArea-tempArableLandArea);
		}
		catch (Exception e) {
			throw new Exception("getCarbonAbsorption function error: " + e);
		}
		
		carbonAbsorption = GameConst.FOREST_CARBON_ABSORPTION * (country.arableLandArea - tempArableLandArea);

		return carbonAbsorption;
	}
	
	/**
	 * Returns the cost of a single block of forest.
	 * Depends on the available land area.
	 * 
	 * @param landArea
	 */
	private double getBlockCost(double landArea) throws Exception {
		double blockCost;
		
		try {
			if (landArea > 0) {
				blockCost = GameConst.CARBON_ABSORPTION_COEFF * GameConst.FOREST_BLOCK_SIZE / landArea;
			}
			else {
				throw new Exception("Trying to find a cost of a block of area for non-positive area left");
			}
		}
		catch (Exception e) {
			throw new Exception("getBlockCost function error: " + e);
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
	public void invest(double investment) throws Exception, NotEnoughCashException {
		double additionalAbsorption;
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
			throw new Exception("invest function error: " + e);
		}
	}
}