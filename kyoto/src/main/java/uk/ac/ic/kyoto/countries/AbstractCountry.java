package uk.ac.ic.kyoto.countries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.monitor.Monitor;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.TimeService;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * 
 * @author cs2309, Adam, Sam, Stuart, Chris
 */
public abstract class AbstractCountry extends AbstractParticipant {
	
	//================================================================================
    // Definitions of Parameters of a Country
    //================================================================================
	
	final protected String 		ISO;		//ISO 3166-1 alpha-3
	
	// TODO Change visibility of fields
	/*
	 * These variables are related to land area for
	 * dealing with carbon absorption prices
	 */
	final protected	double 		landArea;

	protected 		double 		arableLandArea;
	
	/*
	 * These variables are related to carbon emissions and 
	 * calculating 'effective' carbon output
	 */
	protected 		long 		carbonOutput;		// Tons of CO2 produced every year
	protected		long		carbonAbsorption;	// Tons of CO2 absorbed by forests every year
	protected 		long 		carbonOffset; 		// Tons of CO2 that the country acquired (by trading or energy absorption)
	protected 		long		emissionsTarget;	// Number of tons of carbon you SHOULD produce
	
	/*
	 * These variables are related to GDP and
	 * available funds to spend on carbon trading and industry.
	 */
	protected 		double 		GDP;				// GDP of the country in millions of dollars. Changes every year
	protected 		double 		GDPRate;			// The rate in which the GDP changes in a given year. Expressed in %
	protected 		long  		energyOutput;		// How much Carbon we would use if the whole industry was carbon based. Measured in Tons of Carbon per year
	protected 		long 		availableToSpend;	// Measure of cash available to the country in millions of dollars. Note, can NOT be derived from GDP. Initial value can be derived from there, but cash reserves need to be able to lower independently.
	
	
	protected 		Map<Integer, Long> carbonEmissionReports;
	
	ParticipantCarbonReportingService reportingService; // TODO add visibility
	Monitor monitor;
	TimeService timeService;
	
	protected TradeProtocol tradeProtocol; // Trading network interface thing'em
	
	/*
	 * Handlers for different actions that can be performed by the country
	 */
	protected CarbonReductionHandler 	carbonReductionHandler;
	protected CarbonAbsorptionHandler 	carbonAbsorptionHandler;
	protected EnergyUsageHandler		energyUsageHandler;
	
	//================================================================================
    // Constructors and Initializers
    //================================================================================
	
	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
					double GDPRate, long emissionsTarget, long energyOutput, long carbonOutput) {

		//TODO Validate parameters
		
		super(id, name);
		
		this.landArea = landArea;
		this.ISO = ISO;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.emissionsTarget = emissionsTarget;
		this.carbonOffset = 0;
		this.availableToSpend = 0;
		this.carbonOutput = carbonOutput;
		this.carbonAbsorption = 0;
		this.carbonEmissionReports = new HashMap<Integer, Long>();
		this.energyOutput = energyOutput;
		
	}
	
	@Override
	final public void initialise(){
		super.initialise();
		
		// Add the country to the monitor service
		try {
			monitor = getEnvironmentService(Monitor.class);
			monitor.addMemberState(this);
		} catch (UnavailableServiceException e1) {
			System.out.println("Unable to reach monitor service.");
			e1.printStackTrace();
		}
		// Initialize the Action Handlers DO THEY HAVE TO BE INSTANTIATED ALL THE TIME?
		try {
			timeService = getEnvironmentService(TimeService.class);
		} catch (UnavailableServiceException e1) {
			System.out.println("TimeService doesn't work");
			e1.printStackTrace();
		}
		// Initialize the Action Handlers
		carbonAbsorptionHandler = new CarbonAbsorptionHandler(this);
		carbonReductionHandler = new CarbonReductionHandler(this);
		energyUsageHandler = new EnergyUsageHandler(this);
		
		// Connect to the Reporting Service
		try {
			this.reportingService = this.getEnvironmentService(ParticipantCarbonReportingService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach emission reporting service.");
			e.printStackTrace();
		}
		initialiseCountry();
	}
	
	//================================================================================
    // Definitions of Abstract methods
    //================================================================================
	
	@Override
	protected abstract void processInput(Input input);
	
	public abstract void YearlyFunction();
	
	public abstract void SessionFunction();
	
	abstract protected void initialiseCountry();
	
	//================================================================================
    // Public methods
    //================================================================================
	
	@Override
	final public void execute() {
		super.execute();
		if (timeService.getCurrentTick() % timeService.getTicksInYear() == 0) {			
	//		MonitorTax();
	//		checkTargets(); //did the countries meet their targets?
			updateGDPRate();
			updateCarbonOffsetYearly();
			YearlyFunction();
		}
		if (timeService.getCurrentYear() % timeService.getYearsInSession() == 0) {
			resetCarbonOffset();
			SessionFunction();
		}
		behaviour();
	}
	
	/**
	 * All individual country behaviour should occur here
	 */
	abstract protected void behaviour();
	
	
	/**
	 * Taxes individual percentage part of their GDP to pay for the monitor
	 */
	public void MonitorTax() {
		// Give a tax to Monitor agent for monitoring every year
		this.monitor.applyTaxation(GDP*GameConst.MONITOR_COST_PERCENTAGE); // Take % of GDP for monitoring
		availableToSpend -= GDP*GameConst.MONITOR_COST_PERCENTAGE;
	}

	/**
	 * Method used for monitoring. It is called by the Monitor
	 * @return
	 * Real Carbon Output of a country
	 */
	public final long getMonitored() {
		return carbonOutput;
	}

	protected Set<ParticipantSharedState> getSharedState(){
		Set<ParticipantSharedState> s = super.getSharedState();
		s.add(ParticipantCarbonReportingService.createSharedState(this.getCarbonEmissionReports(), this.getID()));
		return s;
	}
	
	public Map<Integer,Long> getCarbonEmissionReports(){
		return this.carbonEmissionReports;
	}
	
	/**
	 * Private setter function for personal reports
	 * @param simTime
	 * @param emission
	 * @return
	 */
	private Map<Integer,Long> addToReports(Time simTime, Long emission){
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
		
		// TODO implement a method to cheat
		this.addToReports(t, carbonOutput);
		return new Double(carbonOutput);
	}
	
	//================================================================================
    // Private methods
    //================================================================================
	
	/**
	 * Calculates GDP rate for the next year
	 * @author Adam, ct
	 */
	private final void updateGDPRate() {
		double marketStateFactor = 0;
		
		Economy economy;
		try {
			economy = getEnvironmentService(Economy.class);
		
		switch(economy.getEconomyState()) {
		case GROWTH:
			marketStateFactor = GameConst.GROWTH_MARKET_STATE;
		case STABLE:
			marketStateFactor = GameConst.STABLE_MARKET_STATE;
		case RECESSION:
			marketStateFactor = GameConst.RECESSION_MARKET_STATE;
		}
		
		GDPRate = GDPRate + marketStateFactor + (GameConst.GROWTH_SCALER*(energyOutput))/GDP;
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach economy service.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates GDP using GDPRate for the past year
	 * @author sc1109
	 */
	private final void updateGDP() {
		GDP = GDP + GDP * GDPRate;
	}
	
	/**
	 * Calculate available to spend for the next year as an extra 1% of GDP
	 * If we haven't spent something last year, it will be available this year too
	 */
	private final void updateAvailableToSpend() {
		availableToSpend += GDP * GameConst.PERCENTAGE_OF_GDP;
	}
	
	/**
	 * Adjusts the amount of CarbonOffset depending on the last years usage
	 */
	private final void updateCarbonOffsetYearly() {

		carbonOffset += (emissionsTarget - carbonOutput + carbonAbsorption);

	}
	
	private final void resetCarbonOffset() {
		carbonOffset = 0;
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public Double getCash(){
		return this.GDP*GameConst.PERCENTAGE_OF_GDP;
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

	public long getEmissionsTarget() {
		return emissionsTarget;
	}

	public long getCarbonOffset() {
		return carbonOffset;
	}

	public long getAvailableToSpend() {
		return availableToSpend;
	}
	
	public void setEmissionsTarget(long emissionsTarget) {
		this.emissionsTarget = emissionsTarget;
	}
	
	public void setAvailableToSpend(long availableToSpend) {
			this.availableToSpend = availableToSpend;
	}

}
