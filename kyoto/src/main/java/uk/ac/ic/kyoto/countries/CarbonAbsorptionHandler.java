package uk.ac.ic.kyoto.countries;
/**
 * 
 * @author Stuart, Adam
 */
public final class CarbonAbsorptionHandler{
	
	// TODO EXTENSIVE testing
	
	private final AbstractCountry country;

	/**
	 * Create instance of CarbonAbsorbtionHandler
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
	public long getCost(long carbonOffset){
		double neededLand = carbonOffset / GameConst.FOREST_CARBON_OFFSET;
		long noBlocks = (long) (neededLand / GameConst.FOREST_BLOCK_SIZE);
		long totalCost = 0;
		double tempLandArea = this.country.arableLandArea;
		for (int i=0; i < noBlocks; i++) {
			totalCost += getBlockCost(tempLandArea);
			tempLandArea -= GameConst.FOREST_BLOCK_SIZE;
		}
		return totalCost;
	}
	
	/**
	 * Returns number of carbon credits earned for a 
	 * given investment.
	 * 
	 * @param investment
	 */
	public long getCarbonOffset(double investment){
		long totalCost=0;
		double tempArableLandArea = country.arableLandArea;
		while (totalCost < investment && tempArableLandArea > GameConst.FOREST_BLOCK_SIZE) {
			totalCost += getBlockCost(tempArableLandArea);
			tempArableLandArea -= GameConst.FOREST_BLOCK_SIZE;
		}
		return (long) (GameConst.FOREST_CARBON_OFFSET * (country.arableLandArea-tempArableLandArea) );
	}
	
	private long getBlockCost(double landArea) {
		long blockCost;
		if (landArea > 0)
			blockCost = (long) (GameConst.CARBON_ABSORPTION_COEFF * GameConst.FOREST_BLOCK_SIZE / landArea);
		else {
			country.logger.warn("Trying to find a cost of a block of area for non-positive area left");
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
	public void invest(double investment) throws Exception{
		// calculate how much Carbon Offset will be gained through the investment
		long newOffset = getCarbonOffset(investment);
		// calculate how much arable area has to be used during the investment
		double arableAreaUsed = newOffset/GameConst.FOREST_CARBON_OFFSET;
		
		if( investment <= country.availableToSpend) {
			
			if (arableAreaUsed <= country.arableLandArea) {
				country.availableToSpend -= investment;
				country.carbonOffset += newOffset;
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
}