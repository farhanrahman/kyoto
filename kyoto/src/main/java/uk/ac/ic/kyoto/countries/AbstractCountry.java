package uk.ac.ic.kyoto.countries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

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
	
	//TODO Register UUID and country ISO with the environment
	
	final protected String ISO;		//ISO 3166-1 alpha-3
	
	/*
	 * These variables are related to land area for
	 * dealing with carbon absorption prices
	 */
	final protected double 	landArea;
	protected double 		arableLandArea;
	
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
	protected long 		availableToSpend; // Note, can NOT be derived from GDP. Initial value can be derived from there, but cash reserves need to be able to lower independently.
	
	//private long 	carbonTraded; 
	//private double  dirtyIndustry;

	protected Map<Integer, Long> carbonEmissionReports;
	
	ParticipantCarbonReportingService reportingService; // TODO add visibility
	Monitor monitor;
	
	protected TradeProtocol tradeProtocol; // Trading network interface thing'em
	protected CarbonReductionHandler 	carbonReductionHandler;
	protected CarbonAbsorptionHandler carbonAbsorptionHandler;

	protected Logger logger;
	
	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
					double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
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
		this.carbonEmissionReports = new HashMap<Integer, Long>();
		this.energyOutput = energyOutput;
		
		// Create an instance of a logger
		logger = Logger.getLogger(name);
	}
	
	@Override
	abstract protected void processInput(Input input);
	
	@Override
	public void initialise(){
		super.initialise();
		
		// Add the country to the monitor service
		try {
			this.monitor = this.getEnvironmentService(Monitor.class);
			this.monitor.addMemberState(this);
		} catch (UnavailableServiceException e1) {
			System.out.println("Unable to reach monitor service.");
			e1.printStackTrace();
		}
		
		carbonAbsorptionHandler = new CarbonAbsorptionHandler();
		carbonReductionHandler = new CarbonReductionHandler();
		try {
			this.reportingService = this.getEnvironmentService(ParticipantCarbonReportingService.class);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach emission reporting service.");
			e.printStackTrace();
		}
	}
	
	public abstract void YearlyFunction();
	
	public abstract void SessionFunction();
	
	private void GDPRate() {
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
	
	@Override
	public void execute() {
		super.execute();
		try {
			TimeService timeService = getEnvironmentService(TimeService.class);
			if (timeService.getCurrentTick() % timeService.getTicksInYear() == 0) {
				YearlyFunction();
				MonitorTax();
				GDPRate();
			}
			if (timeService.getCurrentYear() % timeService.getYearsInSession() == 0) {
				SessionFunction();
			}
		} catch (UnavailableServiceException e) {
			logger.warn(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	

	public void MonitorTax() {
		// Give a tax to Monitor agent for monitoring every year
		this.monitor.taxForMonitor(availableToSpend*GameConst.MONITOR_COST_PERCENTAGE); // Take % of money for monitoring
		availableToSpend -= availableToSpend*GameConst.MONITOR_COST_PERCENTAGE;
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
		
		// This  is an example of how reporting your carbon output is structured
		/*try{
		Time t = SimTime.get();
		AbstractCountry.this.environment.act(new SubmitCarbonEmissionReport(
					AbstractCountry.this.reportCarbonEmission(t), t), 
					AbstractCountry.this.getID(), 
					AbstractCountry.this.authkey);
		}catch(ActionHandlingException e){
			logger.warn("Error trying to submit report");
		}*/
		
		this.addToReports(t, carbonOutput);
		return new Double(carbonOutput);
	}
	

	
	//================================================================================
    // Energy Output Control functions
    //================================================================================
	
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
	protected void reduceEnergyOutput (long amount) throws IllegalArgumentException{
		if (amount < carbonOutput && amount > 0) {
			energyOutput -= amount;
			carbonOutput -= amount;
		}
		else
			throw new IllegalArgumentException("Specified amount should be > 0 and < carbonOutput");
	}
	
	/**
	 * Calculates the cost of investing in carbon industry
	 * @param carbon
	 * The expected increase in carbon output
	 * @return
	 * The cost for the country
	 */
	protected long calculateCostOfInvestingInCarbonIndustry (long carbon){
		return (long) (carbon * GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	/**
	 * Calculates the increase of carbon output
	 * @param cost
	 * The amount of money to be spent on carbon industry growth
	 * @return
	 * The increase of carbon output
	 */
	protected long calculateCarbonIndustryGrowth (long cost){
		return (long) (cost / GameConst.CARBON_INVESTMENT_PRICE);
	}
	
	/**
	 * Invests in carbon industry.
	 * Carbon output and energy output of the country go up
	 * @param carbon
	 * The increase of the carbon output that will be achieved.
	 */
	protected void investInCarbonIndustry(long carbon){
		try {
			long cost = calculateCostOfInvestingInCarbonIndustry(carbon);
			if (cost > availableToSpend) {
				carbonOutput += carbon;
				energyOutput += carbon;
				availableToSpend -= cost;
			}
			else {
				// TODO log that there is not enough money
			}
		}
		catch (Exception e) {
			// TODO log the exception
		}
	}
	
	// GDP related functions
	
	public Double getCash(){
		return this.GDP*GameConst.PERCENTAGE_OF_GDP;
	}
	
	protected final class CarbonReductionHandler{
		
		/**
		 * Returns the cost of investment required to
		 * reduce dirty industry by a specified amount of tons of carbon.
		 * 
		 * @param carbonOutputChange
		 * @return cost of reducing carbon by said amount
		 */
		public final long getCost(double carbonOutputChange){
			long cost;
			
			cost = (long) (GameConst.CARBON_REDUCTION_COEFF * carbonOutputChange / energyOutput);
			
			return cost;
		}
		
		/**
		 * Returns the reduction of carbon output
		 * for a specified cost of investment.
		 * 
		 * @param currency
		 * @return the change in carbon output from said cost
		 */
		public final double getCarbonOutputChange(long cost) {
			double carbonOutputChange;
			
			carbonOutputChange = energyOutput * cost / GameConst.CARBON_REDUCTION_COEFF;
			
			return carbonOutputChange;
		}
		
		/**
		 * Executes carbon reduction investment.
		 * On success, will reduce Carbon Output of a country keeping the Energy Output constant
		 * On failure, will throw Exception.
		 * 
		 * @param investment
		 * @throws Exception
		 */
		public final void invest(long investment) throws Exception{
			if (investment < availableToSpend){
				availableToSpend -= investment;
				carbonOutput -= getCarbonOutputChange(investment);
			}
			else {
				throw new Exception("Investment is greated than available cash to spend");
			}
		}
	}
	
	protected final class CarbonAbsorptionHandler{
		
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
			double tempLandArea = arableLandArea;
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
			double tempArableLandArea=arableLandArea;
			while (totalCost < investment && tempArableLandArea > GameConst.FOREST_BLOCK_SIZE) {
				totalCost += getBlockCost(tempArableLandArea);
				tempArableLandArea -= GameConst.FOREST_BLOCK_SIZE;
			}
			return (long) (GameConst.FOREST_CARBON_OFFSET*(arableLandArea-tempArableLandArea));
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
			if(investment <= availableToSpend){
				//TODO Implement reduction in GDP
				//TODO Implement change in CO2 emissions/arable land
				//Stub for submitting reports
				
				availableToSpend -= investment;
				long newOffset = getCarbonOffset(investment);
				carbonOffset += newOffset;
				arableLandArea -= newOffset/GameConst.FOREST_CARBON_OFFSET;
								
			}else{
				//TODO Use better exception
				throw new Exception("Investment is greated than available cash to spend");
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

	public double getEmissionTarget() {
		return emissionsTarget;
	}

	public long getCarbonOffset() {
		return carbonOffset;
	}

	public long getAvailableToSpend() {
		return availableToSpend;
	}

	/**
	 * Method used for monitoring. Is called randomly by the Monitor agent
	 * @return 
	 */
	public final long getMonitored() {
//		long latestReport = this.carbonEmissionReports.get(SimTime.get().intValue());
//		long trueCarbon = this.carbonOutput;
//		
//		if (latestReport != trueCarbon) {
//				//TODO - Insert sanctions here!
//		}
		return carbonOutput;
	}
}
