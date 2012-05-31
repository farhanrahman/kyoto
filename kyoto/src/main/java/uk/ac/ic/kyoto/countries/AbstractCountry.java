package uk.ac.ic.kyoto.countries;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

abstract class AbstractCountry extends AbstractParticipant {
	
	//TODO fields taken from early meeting. Do we still agree and want to follow this model?
	
	final private double landArea;
	
	private double 	arableLandArea;
	private double 	GDP;
	private double 	GDPRate;
	private double 	dirtyIndustry;
	private double	emissionTarget;
	private long 	carbonOffset;
	private float 	availableToSpend;
	private long 	carbonTraded;
	
	private Set<PublicOffer> 		offers;
	private CarbonReductionHandler 	carbonReductionHandler;
	private CarbonAbsorptionHandler carbonAbsorptionHandler;

	private AbstractCountry(UUID id, String name, double landArea, double arableLandArea, double GDP,
					double GDPRate, double dirtyIndustry, double emissionsTarget, long carbonOffset,
					float availableToSpend, long carbonTraded) {
		
		super(id, name);
		this.landArea = landArea;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.dirtyIndustry = dirtyIndustry;
		this.emissionTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
		this.availableToSpend = availableToSpend;
		this.carbonTraded = carbonTraded;		
	}
	
	@Override
	abstract protected void processInput(Input arg0);
	
	@Override
	public void initialise(){
		super.initialise();
		
		carbonAbsorptionHandler = new CarbonAbsorptionHandler();
		carbonReductionHandler = new CarbonReductionHandler();
	}
	
	@Override
	abstract public void execute();
	
	//TODO Should carbonReduction and carbonAbsorption be broadcast to the environment (an action)?
	
	private final class CarbonReductionHandler{
		
		/**
		 * Returns the cost of investment required to
		 * reduce dirty industry.
		 * 
		 * @param percentage
		 */
		public final double getCost(double percentage){
			//TODO Implementation
		}
		
		/**
		 * Returns percentage reduction of dirty industry
		 * for a given investment.
		 * 
		 * @param currency
		 */
		public final long getPercentage(double investment){
			//TODO Implementation
		}
		
		/**
		 * Executes carbon reduction investment.</br>
		 * 
		 * On success, will reduce GDP and dirtyIndustry.</br>
		 * On failure, will throw Exception.</br>
		 * 
		 * @param investment
		 * @throws Exception
		 */
		public final void execute(double investment) throws Exception{
			if(investment < GDP){
				//TODO Implement reduction in GDP
				//TODO Implement reduction in dirtyIndustry
			}else{
				//TODO Use better exception
				throw new Exception("Investment is greated than available GDP");
			}
		}
	}
	
	private final class CarbonAbsorptionHandler{
		
		/**
		 * Returns the cost of investment required to
		 * obtain a given number of carbon credits.
		 * 
		 * @param carbonCredits
		 */
		public double getCost(long carbonCredits){
			//TODO Implementation
		}
		
		/**
		 * Returns number of carbon credits earned for a 
		 * given investment.
		 * 
		 * @param investment
		 */
		public long getCarbonCredits(double investment){
			//TODO Implementation
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
		public void execute(double investment) throws Exception{
			if(investment <= GDP){
				//TODO Implement reduction in GDP
				//TODO Implement change in CO2 emissions/arable land
			}else{
				//TODO Use better exception
				throw new Exception("Investment is greated than available GDP");
			}
		}
	}
	
}
