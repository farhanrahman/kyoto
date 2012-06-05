package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;

import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.trade.PublicOffer;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.ic.kyoto.trade.PublicOffer;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author cs2309
 */
public abstract class AbstractCountry extends AbstractParticipant {
	
	//TODO Register UUID and country ISO with the environment
	
	final protected String ISO;		//ISO 3166-1 alpha-3
	
	
	/*
	 * These variables are related to land area for
	 * dealing with carbon absorbtion prices
	 */
	final protected double landArea;
	protected double 	arableLandArea;
	
	/*
	 * These variables are related to carbon emissions and 
	 * calculating 'effective' carbon output
	 */
	protected long 	carbonOutput; // In tons of carbon dioxide
	protected long 	carbonOffset; // In tons of carbon
	protected long	emissionsTarget; // Number of tons of carbon you SHOULD produce
	
	/*
	 * These variables are related to GDP and
	 * available funds to spend on carbon trading and industry.
	 */
	protected double 	GDP;
	protected double 	GDPRate;	// The rate in which the DGP changes in a given year. Expressed in %
	protected long  	energyOutput; // How much Carbon we would use if the whole industry was carbon based. Measured in Tons of Carbon per year
	private float 		availableToSpend; // Note, can NOT be derived from GDP. Initial value can be derived from there, but cash reserves need to be able to lower independently.
	
	//private long 	carbonTraded; 
	//private double  dirtyIndustry;

	/**
	 * carbonEmission and carbonEmissionReports added
	 */
	protected double carbonEmission = 10.0;  //Farhan test

	protected Map<Integer, Double> carbonEmissionReports;
	
	ParticipantCarbonReportingService reportingService;
	
	protected TradeProtocol tradeProtocol; // Trading network interface thing'em
	protected Set<PublicOffer> 		offers;
	protected CarbonReductionHandler 	carbonReductionHandler;
	protected CarbonAbsorptionHandler carbonAbsorptionHandler;

	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
					double GDPRate, float availableToSpend, long emissionsTarget, long carbonOffset,
					long energyOutput, long carbonOutput) {

		//TODO Validate parameters
		
		super(id, name);
		this.landArea = landArea;
		this.ISO = ISO;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.emissionsTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
		this.availableToSpend = availableToSpend;
		this.carbonOutput = carbonOutput;
		this.carbonEmissionReports = new HashMap<Integer, Double>();
		this.energyOutput = energyOutput;
	}
	
	@Override
	abstract protected void processInput(Input input);
	
	@Override
	public void initialise(){
		super.initialise();
		
		carbonAbsorptionHandler = new CarbonAbsorptionHandler();
		carbonReductionHandler = new CarbonReductionHandler();
		try {
			this.reportingService = this.getEnvironmentService(ParticipantCarbonReportingService.class);
		} catch (UnavailableServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected Set<ParticipantSharedState> getSharedState(){
		Set<ParticipantSharedState> s = super.getSharedState();
		s.add(ParticipantCarbonReportingService.createSharedState(this.getCarbonEmissionReports(), this.getID()));
		return s;
	}
	
	public Map<Integer,Double> getCarbonEmissionReports(){
		return this.carbonEmissionReports;
	}
	
	/**
	 * Private setter function for personal reports
	 * @param simTime
	 * @param emission
	 * @return
	 */
	private Map<Integer,Double> addToReports(Time simTime, Double emission){
		this.carbonEmissionReports.put(simTime.intValue(), emission);
		return this.carbonEmissionReports;
	}
	
	/**
	 * Report the carbonEmissions. This function internally
	 * updates the report already owned by the agent after
	 * calculating the carbon emission that the agent wants
	 * to report to the environment
	 * @param t: Simulation time at which report submission was made
	 * @return
	 */
	public Double reportCarbonEmission(Time t){
		//TODO add code to calculate whether to submit true or false report (cheat)
		//Once calculations done, update the report owned by this agent
		carbonEmission++; //Default code now just increments it
		this.addToReports(t, carbonEmission);
		return new Double(carbonEmission);
	}
	
	/**
	 * Reduces both the energyOutput and carbonOutput of the country
	 * It can be used to limit carbonOuput without any financial cost
	 * As the energyOuput goes down, the GDP growth goes down too
	 * 
	 * @param amount
	 * 
	 * Amount of energyOuput that should be reduced
	 * It has to be positive and lower than the total carbonOuput
	 */
	
	//================================================================================
    // Investing in 
    //================================================================================
	
	protected void reduceEnergyOutput (long amount) throws IllegalArgumentException{
		if (amount < carbonOutput && amount > 0) {
			energyOutput -= amount;
			carbonOutput -= amount;
		}
		else
			throw new IllegalArgumentException("Specified amount should be > 0 and < carbonOutput");
	}
	
	protected long calculateCostOfInvestingInCarbonIndustry (long carbon){
		return (long) (carbon * GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	protected long calculateCarbonIndustryGrowth (long cost){
		return (long) (cost / GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	protected void investInCarbonIndustry(long carbon){
		try {
			long cost = calculateCostOfInvestingInCarbonIndustry(carbon);
			carbonOutput += carbon;
			energyOutput += carbon;
			availableToSpend -= cost;
		}
		catch (Exception e) {
			// log the exception
		}
	}
	
	// GDP related functions
	
	public Double getCash(){
		return this.GDP*GameConst.PERCENTAGE_OF_GDP;
	}
	@EventListener
	public void calculateGDPRate(EndOfTimeCycle e){
		//TODO Make work, adjust economicOutput
		
		double marketStateFactor = 0;
		
		Economy.State economyState = Economy.getEconomyState();
		
		switch(economyState) {
		case GROWTH:
			marketStateFactor = GameConst.GROWTH_MARKET_STATE;
		case STABLE:
			marketStateFactor = GameConst.STABLE_MARKET_STATE;
		case RECESSION:
			marketStateFactor = GameConst.RECESSION_MARKET_STATE;
		}
		
		GDPRate = GDPRate + marketStateFactor + (GameConst.GROWTH_SCALER*(energyOutput))/GDP;
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
				//Stub for submitting reports
				/*try{
					Time t = SimTime.get();
					AbstractCountry.this.environment.act(new SubmitCarbonEmissionReport(
								AbstractCountry.this.reportCarbonEmission(t), t), 
								AbstractCountry.this.getID(), 
								AbstractCountry.this.authkey);
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
	
	public long getCurrentYear() {
		// Returns the current year we are in
		// This should probably be somewhere in the environment, not sure where
		return 0;
	}
	
	public long calculateCreditsToSell() {
		// Returns credits that a country has available to sell
		return 0;
	}
	
	public void getMonitored() {
		int time = SimTime.get().intValue();
		double latestReport = this.carbonEmissionReports.get(time);
		double trueCarbon = this.carbonEmission;
		double random = Random.randomDouble();
		
		if (random < Double.MAX_VALUE/2) {
			if (latestReport != trueCarbon) {
				//TODO - Insert sanctions here!
			}
		}
		
	}
}
