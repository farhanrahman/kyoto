package uk.ac.ic.kyoto.countries;
/**
 * 
 * @author Stuart, Adam
 */
public final class CarbonAbsorptionHandler{
	
	private final AbstractCountry abstractCountry;

	/**
	 * Create instance of CarbonAbsorbtionHandler
	 * @param abstractCountry
	 * Specify on which country will the handler operate
	 */
	CarbonAbsorptionHandler(AbstractCountry abstractCountry) {
		this.abstractCountry = abstractCountry;
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
		double tempLandArea = this.abstractCountry.arableLandArea;
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
		double tempArableLandArea=this.abstractCountry.arableLandArea;
		while (totalCost < investment && tempArableLandArea > GameConst.FOREST_BLOCK_SIZE) {
			totalCost += getBlockCost(tempArableLandArea);
			tempArableLandArea -= GameConst.FOREST_BLOCK_SIZE;
		}
		return (long) (GameConst.FOREST_CARBON_OFFSET*(this.abstractCountry.arableLandArea-tempArableLandArea));
	}
	
	private long getBlockCost(double landArea) {
		double proportion = GameConst.FOREST_BLOCK_SIZE/landArea;
		return (long) (proportion * GameConst.CARBON_ABSORPTION_COEFF);
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
		if(investment <= this.abstractCountry.availableToSpend){
			//TODO Implement reduction in GDP
			//TODO Implement change in CO2 emissions/arable land
			//Stub for submitting reports
			
			this.abstractCountry.availableToSpend -= investment;
			long newOffset = getCarbonOffset(investment);
			this.abstractCountry.carbonOffset += newOffset;
			this.abstractCountry.arableLandArea -= newOffset/GameConst.FOREST_CARBON_OFFSET;
							
		}else{
			//TODO Use better exception
			throw new Exception("Investment is greater than available cash to spend");
		}
	}
}