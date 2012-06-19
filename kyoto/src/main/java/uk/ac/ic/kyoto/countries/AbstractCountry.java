package uk.ac.ic.kyoto.countries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitor;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitor.addRemoveType;
import uk.ac.ic.kyoto.actions.AddToCarbonTarget;
import uk.ac.ic.kyoto.actions.ApplyMonitorTax;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReport;
import uk.ac.ic.kyoto.countries.OfferMessage.OfferMessageType;
import uk.ac.ic.kyoto.exceptions.UnauthorisedExecuteException;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * Class from which all countries are derived
 * 
 * @author cs2309, Adam, Sam, Stuart, Chris
 */
public abstract class AbstractCountry extends AbstractParticipant {
	
	//================================================================================
    // Definitions of Parameters of a Country
    //================================================================================

	// TODO Change visibility of fields?
	
	final protected String 		ISO;		//ISO 3166-1 alpha-3
	
	/*
	 *  KyotoMember level variable shows whether country is annex one,
	 *  non-annex one, or rogue states
	 */
	public enum KyotoMember {
		ROGUE,
		ANNEXONE,
		NONANNEXONE
	}
	private KyotoMember kyotoMemberLevel; 
	
	/*
	 * These variables are related to land area for
	 * dealing with carbon absorption prices
	 * TODO: What are the units of these?
	 */
	final double landArea;
	protected double arableLandArea;
	
	/*
	 * These variables are related to carbon emissions and 
	 * calculating 'effective' carbon output
	 */
	double carbonOutput;		// Tons of CO2 produced every year
	double carbonAbsorption;	// Tons of CO2 absorbed by forests every year

	double carbonOffset; 		// Tons of CO2 that the country acquired (by trading or energy absorption)
	double emissionsTarget;		// Number of tons of carbon you SHOULD produce
	
	/*
	 * These variables are related to GDP and
	 * available funds to spend on carbon trading and industry.
	 */
	double GDP;				// GDP of the country in millions of dollars. Changes every year
	double GDPRate;			// The rate in which the GDP changes in a given year. Expressed in %
	double energyOutput;		// How much Carbon we would use if the whole industry was carbon based. Measured in Tons of Carbon per year
	double availableToSpend;	// Measure of cash available to the country in millions of dollars. Note, can NOT be derived from GDP. Initial value can be derived from there, but cash reserves need to be able to lower independently.
	
	
	protected Map<Integer, Double> carbonEmissionReports;
	
	/* Environment Services */
	
	protected ParticipantCarbonReportingService reportingService;
	protected ParticipantTimeService timeService;
	
	protected TradeProtocol tradeProtocol; // Trading network interface thing'em
	
	/*
	 * Handlers for different actions that can be performed by the country
	 */
	protected CarbonReductionHandler 	carbonReductionHandler;
	protected CarbonAbsorptionHandler 	carbonAbsorptionHandler;
	protected EnergyUsageHandler		energyUsageHandler;
	
	/*Flag for single initialisation of AbstractCountry*/
	private boolean initialised = false;

	private double prevEnergyOutput; //Keeps track of the previous years EnergyOutput to calculate GDP
	
	private DataStore dataStore = new DataStore();
	
	private boolean executeLock = false; /*Lock for stopping multiple execution of the execute block*/
	
	//================================================================================
    // Constructors and Initializers
    //================================================================================
	/*Constructor for testing*/
	public AbstractCountry(UUID id, String name, String ISO){
		super(id,name);
		this.landArea = 0;
		this.ISO = ISO;
	}
	
	public AbstractCountry(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP, double GDPRate, double energyOutput,
			double carbonOutput) {

		//TODO Validate parameters
		super(id, name);
		
		this.landArea = landArea;
		this.ISO = ISO;
		this.arableLandArea = arableLandArea;
		this.GDP = GDP;
		this.GDPRate = GDPRate;
		this.emissionsTarget = 0;
		this.carbonOffset = 0;
		this.availableToSpend = 0;
		this.carbonOutput = carbonOutput;
		this.carbonAbsorption = 0;
		this.carbonEmissionReports = new HashMap<Integer, Double>();
		this.energyOutput = energyOutput;
		this.prevEnergyOutput = energyOutput;
		this.kyotoMemberLevel = KyotoMember.ANNEXONE;
	}
	
	@Override
	final public void initialise(){
		try{
			if(this.initialised == false){
				super.initialise();
				
				try {
					environment.act(new AddToCarbonTarget(this), getID(), authkey);
				} catch (ActionHandlingException e2) {
					e2.printStackTrace();
				}
				try {
					if (isKyotoMember() == KyotoMember.ANNEXONE)
						environment.act(new AddRemoveFromMonitor(this, addRemoveType.ADD), getID(), authkey);
				} catch (ActionHandlingException e2) {
					e2.printStackTrace();
				}
				// Initialize the Action Handlers TODO: DO THEY HAVE TO BE INSTANTIATED ALL THE TIME?
				try {
					timeService = getEnvironmentService(ParticipantTimeService.class);
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
				
				this.tradeProtocol = new TradeProtocol(getID(), this.authkey, environment, network, this) {
					
					@Override
					protected boolean acceptExchange(NetworkAddress from, Offer trade) {
						return acceptTrade(from, trade);
					}
				};	
				this.initialised = true;
				initialiseCountry();
			}else{
				throw new AlreadyInitialisedException();
			}
		} catch(AlreadyInitialisedException ex){
			ex.printStackTrace();
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	final public void execute() {
		try{
				super.execute();
				if(!this.isExecuteLocked()){
					this.acquireExecuteLock(); //acquire the lock
				}else{
					throw new UnauthorisedExecuteException(
							SimTime.get().intValue(),
							this.getID(),
							this.getName());
				}
				
				
				if (timeService.getCurrentTick() % timeService.getTicksInYear() == 0) {
					System.out.println(this.ISO + " first day of new year on tick " + timeService.getCurrentTick());
					
					updateGDPRate();
					updateGDP();
					updateAvailableToSpend();
					if (kyotoMemberLevel == KyotoMember.ANNEXONE) {
						MonitorTax();
					}

					updateCarbonOffsetYearly();
					
					try {
						reportCarbonOutput();
					} catch (ActionHandlingException e) {
						e.printStackTrace();
					}
					
					yearlyFunction();
				}
				
			if ((timeService.getCurrentYear() % timeService.getYearsInSession()) + (timeService.getCurrentTick() % timeService.getTicksInYear()) == 0) {
				resetCarbonOffset();
				sessionFunction();
			}
	
			//leave a 10-tick grace period to allow current trades to complete before performing end of year routine
			if (timeService.getCurrentTick() % timeService.getTicksInYear() < timeService.getTicksInYear() - 10 ) {
				behaviour();
			}
			
			//assume by this point all trades are complete and it's safe to report
			else if (timeService.getCurrentTick() % timeService.getTicksInYear() == timeService.getTicksInYear() - 3 ){
				try {
					System.out.println(this.ISO + "reporting on tick " + timeService.getCurrentTick());
					reportCarbonOutput();
				} catch (ActionHandlingException e) {
					e.printStackTrace();
				}
			}
			
			logSimulationData();
			
			this.releaseExecuteLock();
			
		} catch(UnauthorisedExecuteException e){
			logger.warn(e);
		}
	}
	
	/**
	 * function to set executeLock to true.
	 */
	private synchronized void acquireExecuteLock(){
		this.executeLock = true;
	}
	
	/**
	 * void function to set executeLock to false
	 */
	private synchronized void releaseExecuteLock(){
		this.executeLock = false;
	}
	
	/**
	 * function returns whether the executeLock is
	 * true or false
	 * @return the state of the executeLock
	 */
	private synchronized boolean isExecuteLocked(){
		return this.executeLock;
	}
	
	protected void reportCarbonOutput() throws ActionHandlingException {
		environment.act(new SubmitCarbonEmissionReport(carbonOutput), getID(), authkey);
	}
	
	/**
	 * All individual country behaviour should occur here
	 */
	abstract protected void behaviour();
	
	
	/**
	 * Taxes individual percentage part of their GDP to pay for the monitor
	 */
	final void MonitorTax() {
		// Give a tax to Monitor agent for monitoring every year
		try {
			environment.act(new ApplyMonitorTax(GDP*GameConst.getMonitorCostPercentage()), getID(), authkey);
			availableToSpend -= GDP*GameConst.getMonitorCostPercentage();
		} catch (ActionHandlingException e) {
			logger.warn(e.getMessage(), e);
			e.printStackTrace();
		} // Take % of GDP for monitoring
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
	private Map<Integer,Double> addToReports(Time simTime, double emission){
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

	@Override
	abstract protected void processInput(Input input);
	abstract protected void yearlyFunction();
	abstract protected void sessionFunction();
	abstract protected void initialiseCountry();
	abstract protected boolean acceptTrade(NetworkAddress from, Offer trade);
	
	//================================================================================
    // Private methods
    //================================================================================
	
	/**
	 * Calculates GDP rate for the next year
	 * @author Adam, ct
	 */
	private final void updateGDPRate() {
		double marketStateFactor = 0;
		double sum;
		Economy economy;
		
		try {
			economy = getEnvironmentService(Economy.class);
			
			switch (economy.getEconomyState()) {
			case GROWTH:
				marketStateFactor = GameConst.getGrowthMarketState();
				break;
			case STABLE:
				marketStateFactor = GameConst.getStableMarketState();
				break;
			case RECESSION:
				marketStateFactor = GameConst.getRecessionMarketState();
				break;
			default:
				marketStateFactor = GameConst.getStableMarketState();
				break;
			}
			
			if (energyOutput-prevEnergyOutput >= 0){	
				sum = (((energyOutput-prevEnergyOutput)/prevEnergyOutput)*GameConst.getEnergyGrowthScaler() *marketStateFactor+GDPRate*100)/2;
			}
			else{
				sum = ((energyOutput-prevEnergyOutput)/prevEnergyOutput)*GameConst.getEnergyGrowthScaler();
			}

			GDPRate = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler());
			
			GDPRate /= 100; // Needs to be a % for rate formula
			
			prevEnergyOutput = energyOutput;
				
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
		GDP += GDP * GDPRate;
	}
	
	/**
	 * Calculate available to spend for the next year as an extra 0.5% of GDP
	 * If we haven't spent something last year, it will be available this year too
	 */
	private final void updateAvailableToSpend() {
		availableToSpend += GDP * GameConst.getPercentageOfGdp();
	}
	
	/**
	 * Adjusts the amount of CarbonOffset depending on the last years usage
	 * @author ct
	 */
	private final void updateCarbonOffsetYearly() {
		if (kyotoMemberLevel == KyotoMember.ANNEXONE) {
			if (emissionsTarget <= carbonOffset +carbonAbsorption +carbonOutput ){
				if (carbonOffset > 0) {
					if ((emissionsTarget - carbonOutput + carbonAbsorption)  > carbonOffset)
						carbonOffset = 0;
					else
						carbonOffset += (emissionsTarget - carbonOutput + carbonAbsorption);
				}
			}
		}
	}
	
	private final void resetCarbonOffset() {
		carbonOffset = 0;
	}
	
	//================================================================================
    // Log simulation data function
    //================================================================================
	/**
	 * Stores all simulation data into MongoDB
	 * @author waffles
	 */
	private final void logSimulationData() {
		this.dataStore.addGdp(this.getGDP());
		this.dataStore.addGdpRate(this.getGDPRate());
		this.dataStore.addAvailableToSpend(this.getAvailableToSpend());
		this.dataStore.addEmissionsTarget(this.getEmissionsTarget());
		this.dataStore.addCarbonOffset(this.getCarbonOffset());
		this.dataStore.addCarbonOutput(this.getCarbonOutput());
		this.dataStore.addIsKyotoMember(this.isKyotoMember());	
			/* TODO
			 * is cheating?
			 * carbon reduction - cost, quantity
			 * carbon absorption - cost, quantity
			 * energy usage - cost, quantity
			 */
	}
	
	private final void dumpSimulationData(){
		
		this.persist.setProperty(DataStore.gdpKey, this.dataStore.getGdpHistory().toString());
		this.persist.setProperty(DataStore.gdpRateKey, this.dataStore.getGdpRateHistory().toString());
		this.persist.setProperty(DataStore.availableToSpendKey, this.dataStore.getAvailableToSpendHistory().toString());
		this.persist.setProperty(DataStore.emissionTargetKey, this.dataStore.getEmissionsTargetHistory().toString());
		this.persist.setProperty(DataStore.carbonOffsetKey, this.dataStore.getCarbonOffsetHistory().toString());
		this.persist.setProperty(DataStore.carbonOutputKey, this.dataStore.getCarbonOutputHistory().toString());
		this.persist.setProperty(DataStore.isKyotoMemberKey, this.dataStore.getIsKyotoMemberHistory().toString());
	}
	
	@Override
	public void onSimulationComplete(){
		this.dumpSimulationData();
	}
	
	//================================================================================
    // Trade protocol methods
    //================================================================================
	
	final void payMoney(double amount) {
		this.availableToSpend -= amount;
	}
	
	final void receiveMoney(double amount) {
		this.availableToSpend += amount;
	}
	
	final void sellOffset(double amount) {
		this.carbonOffset -= amount;
	}
	
	final void receiveOffset(double amount) {
		this.carbonOffset += amount;
	}
	
	protected final OfferMessage broadcastSellOffer(int quantity, double unitCost){
		Offer trade = new Offer(quantity, unitCost, TradeType.SELL);
		OfferMessage returnObject = new OfferMessage(
				trade,
				this.tradeProtocol.tradeToken.generate(),
				OfferMessageType.BROADCAST_MESSAGE,
				this.getID());
		this.network.sendMessage(
					new MulticastMessage<OfferMessage>(
							Performative.PROPOSE, 
							Offer.TRADE_PROPOSAL, 
							SimTime.get(), 
							this.network.getAddress(),
							this.tradeProtocol.getAgentsNotInConversation(),
							returnObject)
				);
			return returnObject;
	}

	protected final OfferMessage broadcastBuyOffer(int quantity, double unitCost){
		Offer trade = new Offer(quantity, unitCost, TradeType.BUY);
		
		/*DEBUG*/
			System.out.println();
			System.out.println(this.tradeProtocol.getActiveConversationMembers().toString());
			System.out.println(this.network.getConnectedNodes());
			System.out.println();
		/*DEBUG*/
		
		OfferMessage returnObject = new OfferMessage(
				trade, 
				this.tradeProtocol.tradeToken.generate(), 
				OfferMessageType.BROADCAST_MESSAGE,
				this.getID());
		
		this.network.sendMessage(
					new MulticastMessage<OfferMessage>(
							Performative.PROPOSE, 
							Offer.TRADE_PROPOSAL, 
							SimTime.get(), 
							this.network.getAddress(),
							this.tradeProtocol.getAgentsNotInConversation(),
							returnObject)
				);
			return returnObject;
	}
	
	protected final OfferMessage broadcastInvesteeOffer(double quantity, InvestmentType itype){
		double unitCost;
		try {
			if (itype.equals(InvestmentType.ABSORB)) {
				unitCost = this.carbonAbsorptionHandler.getInvestmentRequired(quantity)/quantity;
			}
			else {
				unitCost = this.carbonReductionHandler.getInvestmentRequired(quantity)/quantity;
			}
			
			Offer trade = new Offer(quantity, unitCost, TradeType.RECEIVE, itype);
			
			OfferMessage returnObject = new OfferMessage(
					trade,
					this.tradeProtocol.tradeToken.generate(),
					OfferMessageType.BROADCAST_MESSAGE,
					this.getID());
			this.network.sendMessage(
						new MulticastMessage<OfferMessage>(
								Performative.PROPOSE, 
								Offer.TRADE_PROPOSAL, 
								SimTime.get(), 
								this.network.getAddress(),
								this.tradeProtocol.getAgentsNotInConversation(),
								returnObject)
					);
			return returnObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//================================================================================
    // Kyoto membership functions
    //================================================================================
	
	private int leaveTime=0, joinTime=0;
	
	protected final void leaveKyoto() throws IllegalStateException {
		if (timeService.getCurrentTick() == 0) {
			kyotoMemberLevel = KyotoMember.ROGUE;
			
			try {
				environment.act(new AddRemoveFromMonitor(this, addRemoveType.REMOVE), getID(), authkey);
			} catch (ActionHandlingException e) {
				System.out.println("Exception wilst removing from monitor: " + e);
				e.printStackTrace();
			}
			return;
		}
		else if (timeService.getCurrentTick() - joinTime >= timeService.getTicksInYear()*GameConst.getMinimumKyotoMembershipDuration()) {
			kyotoMemberLevel = KyotoMember.ROGUE;
			leaveTime=timeService.getCurrentTick();
			
			try {
				environment.act(new AddRemoveFromMonitor(this, addRemoveType.REMOVE), getID(), authkey);
			} catch (ActionHandlingException e) {
				System.out.println("Exception wilst removing from monitor: " + e);
				e.printStackTrace();
			}
			return;
		}
		throw new IllegalStateException("Cannot leave Kyoto Protocol.");
	}
	
	protected final void joinKyoto() throws IllegalStateException {
		if (timeService.getCurrentTick() - leaveTime >= timeService.getTicksInYear()*GameConst.getMinimumKyotoRejoinTime()) {
			kyotoMemberLevel = KyotoMember.ANNEXONE;
			joinTime = timeService.getCurrentTick();
			
			try {
				environment.act(new AddRemoveFromMonitor(this, addRemoveType.ADD), getID(), authkey);
			} catch (ActionHandlingException e) {
				System.out.println("Exception whilst adding to monitor: " + e);
				e.printStackTrace();
			}
			return;
		}
		throw new IllegalStateException("Cannot join Kyoto Protocol.");
	}
	
	//================================================================================
    // Public getters
    //================================================================================
	
	public String getISO() {
		return ISO;
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

	public double getEmissionsTarget() {
		return emissionsTarget;
	}

	public double getCarbonOffset() {
		return carbonOffset;
	}

	public double getEnergyOutput(){
		return energyOutput;
	}
	
	public double getPrevEnergyOut(){
		return prevEnergyOutput;
	}
	
	public double getCarbonOutput(){
		return carbonOutput;
	}
	
	public double getAvailableToSpend() {
		return availableToSpend;
	}
	
	void setEmissionsTarget(double emissionsTarget) {
		this.emissionsTarget = emissionsTarget;
	}
	
	void setAvailableToSpend(double availableToSpend) {
			this.availableToSpend = availableToSpend;
	}
	
	public KyotoMember isKyotoMember() {
		return kyotoMemberLevel;
	}
	
	public double getCarbonAbsorption() {
		return carbonAbsorption;
	}
	
	public void setKyotoMemberLevel(KyotoMember level) {
		if (SimTime.get().intValue() == 0) {
			kyotoMemberLevel = level;
		}
	}
}
