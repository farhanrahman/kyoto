package uk.ac.ic.kyoto.countries;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;

import uk.ac.ic.kyoto.trade.PublicOffer;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author cs2309
 */
public abstract class AbstractCountry extends AbstractParticipant {
	
	//TODO Register UUID and country ISO with the environment
	
	final private double landArea;
	final private String ISO;		//ISO 3166-1 alpha-3
	
	private double 	arableLandArea;
	private double 	GDP;
	private double 	GDPRate;
	private long 	carbonOutput; // In tons of carbon dioxide
	private double	emissionTarget;
	private long 	carbonOffset;
	private float 	availableToSpend;
	private long 	carbonTraded;
	private double  dirtyIndustry;
	
	/**
	 * carbonEmission and carbonEmissionReports added
	 */
	private double carbonEmission = 10.0;

	private Map<Time, Double> carbonEmissionReports;	
	
	private Set<PublicOffer> 		offers;
	private CarbonReductionHandler 	carbonReductionHandler;
	private CarbonAbsorptionHandler carbonAbsorptionHandler;

	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea,
			double GDP,	double GDPRate, double dirtyIndustry, double emissionsTarget, long carbonOffset,
			float availableToSpend, long carbonTraded) {
		
		//TODO Validate parameters
		
		super(id, name);
		this.landArea = landArea;
		this.ISO = ISO;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.dirtyIndustry = dirtyIndustry;
		this.emissionTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
		this.availableToSpend = availableToSpend;
		this.carbonTraded = carbonTraded;
		this.carbonEmissionReports = new HashMap<Time, Double>();		
	}
	
	@Override
	abstract protected void processInput(Input input);
	
	@Override
	public void initialise(){
		super.initialise();
		
		carbonAbsorptionHandler = new CarbonAbsorptionHandler();
		carbonReductionHandler = new CarbonReductionHandler();
		
		
	}
	
	protected Set<ParticipantSharedState> getSharedState(){
		Set<ParticipantSharedState> s = super.getSharedState();
		s.add(new ParticipantSharedState("Report", 
	            (Serializable) this.getCarbonEmissionReports(), getID()));
		return s;
	}
	
	public Map<Time,Double> getCarbonEmissionReports(){
		return this.carbonEmissionReports;
	}
	
	public Map<Time,Double> addToReports(Time simTime, Double emission){
		this.carbonEmissionReports.put(simTime, emission);
		return this.carbonEmissionReports;
	}
	
	public Double calculateCarbonEmission(){
		//TODO add code to calculate whether to submit true or false report (cheat)
		return new Double(carbonEmission);
	}	
	
	@EventListener
	public void calculateGDPRate(EndOfTimeCycle e){
		//TODO Implement
	}
	
	private final class CarbonReductionHandler{
		
		/**
		 * Returns the cost of investment required to
		 * reduce dirty industry.
		 * 
		 * @param percentage
		 * 
		 * Percentage is of your dirty industry.
		 * Eg. If you have 30% dirty industry, reducing
		 * by 10% will bring you down to 27%.
		 * (Because 10% of 30 is 3)
		 */
		public final double getCost(double percentage){
			// TODO improve
			double proportion = percentage;
			if (proportion > 0.31) {
				return (proportion - 0.1) * GDP;
			}
			else if (proportion > 0.19) {
				return (proportion - 0.05) * GDP;
			}
			else {
				return proportion * GDP;
			}
		}
		
		/**
		 * Returns percentage reduction of dirty industry
		 * for a given investment.
		 * 
		 * @param currency
		 * 
		 * Investment is an amount, say $10,000,000.
		 * The return value is a percentage of total industry.
		 * If this function returns 5% and you are at 30%
		 * dirty industry, you will go to 25%.
		 */

		public final double getPercentage(double investment){
			//TODO Improve
			double proportion = investment / GDP;
			double result;
			
			if (proportion > 0.21) {
				result = (proportion + 0.1) * dirtyIndustry;
			}
			else if (proportion > 0.14) {
				result = (proportion + 0.05) * dirtyIndustry;
			}
			else {
				result = proportion * dirtyIndustry;
			}
			return result;
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
		public final void invest(double investment) throws Exception{
			if(investment < GDP){
				GDP -= investment;
				carbonOutput -= (getPercentage(investment) * carbonOutput);
			}else{
				//TODO Use better exception
				throw new Exception("Investment is greater than available GDP");
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
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Returns number of carbon credits earned for a 
		 * given investment.
		 * 
		 * @param investment
		 */
		public long getCarbonCredits(double investment){
			//TODO Implementation
			throw new UnsupportedOperationException();
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
				//Test for submitting reports
				/*try{
					this.environment.act(new SubmitCarbonEmissionReport(this.calculateCarbonEmission(), SimTime.get(), this), this.getID(), this.authkey);
				}catch(ActionHandlingException e){
					logger.warn("Error trying to submit report");
				}*/
								
			}else{
				//TODO Use better exception
				throw new Exception("Investment is greated than available GDP");
			}
		}
	}

	public double getLandArea() {
		return landArea;
	}

	public double getArableLandArea() {
		return arableLandArea;
	}

	public double getGDP() {
		return GDP;
	}

	public double getGDPRate() {
		return GDPRate;
	}

	public double getDirtyIndustry() {
		return dirtyIndustry;
	}

	public double getEmissionTarget() {
		return emissionTarget;
	}

	public long getCarbonOffset() {
		return carbonOffset;
	}

	public float getAvailableToSpend() {
		return availableToSpend;
	}

	public long getCarbonTraded() {
		return carbonTraded;
	}
	
}
