package uk.ac.ic.kyoto.countries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;

import uk.ac.ic.kyoto.trade.PublicOffer;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.ic.kyoto.trade.PublicOffer;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author cs2309
 */
public abstract class AbstractCountry extends AbstractParticipant {
	
	//TODO Register UUID and country ISO with the environment
	
	final protected double landArea;
	final protected String ISO;		//ISO 3166-1 alpha-3
	
	protected double 	arableLandArea;
	protected double 	GDP;
	protected double 	GDPRate;
	protected long 	carbonOutput; // In tons of carbon dioxide
	protected long	emissionsTarget; // Number of tons of carbon you SHOULD produce
	protected long 	carbonOffset; // In tons of carbon
	//private float 	availableToSpend;
	protected double marketState;
	//private long 	carbonTraded;
	//private double  dirtyIndustry;
	protected float economicOutput;
	/**
	 * carbonEmission and carbonEmissionReports added
	 */
	protected double carbonEmission = 10.0;  //Farhan test

	protected Map<Integer, Double> carbonEmissionReports;	
	
	protected TradeProtocol tradeProtocol; // Trading network interface thing'em
	protected Set<PublicOffer> 		offers;
	protected CarbonReductionHandler 	carbonReductionHandler;
	protected CarbonAbsorptionHandler carbonAbsorptionHandler;

	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
					double GDPRate, long emissionsTarget, long carbonOffset,
					float economicOutput) {

		//TODO Validate parameters
		
		super(id, name);
		this.landArea = landArea;
		this.ISO = ISO;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.emissionsTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
	//	this.availableToSpend = availableToSpend; -- replaced with a function since availiable to spend can be derived from GDP
	//	this.carbonTraded = carbonTraded;
		this.carbonEmissionReports = new HashMap<Integer, Double>();
		this.economicOutput = economicOutput;
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
	
	public Map<Integer,Double> getCarbonEmissionReports(){
		return this.carbonEmissionReports;
	}
	
	public Map<Integer,Double> addToReports(Time simTime, Double emission){
		this.carbonEmissionReports.put(simTime.intValue(), emission);
		return this.carbonEmissionReports;
	}
	
	public Double calculateCarbonEmission(){
		//TODO add code to calculate whether to submit true or false report (cheat)
		return new Double(carbonEmission);
	}	
	
	public Double getCash(){
		return this.GDP*GameConst.PERCENTAGE_OF_GDP;
	}
	@EventListener
	public void calculateGDPRate(EndOfTimeCycle e){
		//TODO Make work, adjust economicOutput
		GDPRate = GDPRate + marketState + (GameConst.GROWTH_SCALER*(economicOutput))/GDP;
	}
	
	private final class CarbonReductionHandler{
		
		final Map<Long, Double> investTable = new TreeMap<Long, Double>();
//		final ArrayList<Long> investTable = new ArrayList<Long>();
		
		public CarbonReductionHandler() {
			for (double i=0.00; i <= 1.00; i += 0.01) {
				investTable.put(GameConst.CARBON_REDUCTION_COEFF*Math.round((i/Math.exp(-(1-i)))), i);
			}
		}
		
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
			return GameConst.CARBON_REDUCTION_COEFF*(percentage/Math.exp(-(1-percentage)));
		}
		
		/**
		 * Returns percentage reduction of dirty industry
		 * for a given investment.
		 * 
		 * @param currency
		 * 
		 * Investment is an amount, say $10,000,000.
		 * The return value is the percentage of your
		 * carbon output that will be reduced.
		 * Eg. If it returns 10%, you will go from
		 * 100 tons to 90 tons.
		 */
		public final double getPercentage(long investment) throws IllegalArgumentException{
			//TODO Improve
			for (Entry<Long, Double> el : investTable.entrySet()) {
				if (el.getKey() > investment) {
					return (double)el.getValue();
				}
			}
			throw new IllegalArgumentException("Out of bounds: no record in the table");
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
		public final void invest(long investment) throws Exception{
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

/*	public double getDirtyIndustry() {
		return dirtyIndustry;
	}
*/
	public double getEmissionTarget() {
		return emissionsTarget;
	}

	public long getCarbonOffset() {
		return carbonOffset;
	}
/*
	public float getAvailableToSpend() {
		return availableToSpend;
	}

	public long getCarbonTraded() {
		return carbonTraded;
	}
*/	
}
