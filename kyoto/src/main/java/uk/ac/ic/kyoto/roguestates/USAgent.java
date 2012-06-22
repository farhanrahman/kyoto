package uk.ac.ic.kyoto.roguestates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.CarbonAbsorptionHandler;
import uk.ac.ic.kyoto.countries.CarbonReductionHandler;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.ic.kyoto.trade.TradeType;

public class USAgent extends AbstractCountry {

	private static final int 		Average1 = 4;
	private static final double  DecreaseIntensityByPercent = 0.95;
	private static final double  IncreaseGDPRateByPercent = 1.05;
	private static final int 		ElectionRandomAdjust = 0;
	private static final double 	EnergyIncreaseStep = 0.01; // increase by one percent until tagret reached
	
	private boolean DemocratElected; 			// chosen at random on class instantiation
	private double  AbsolutionReductionTarget; 	// Units in metric tones C02
												// Can be positive or negative
	private double IntensityTarget; 			// Target for the year
	private double IntensityRatio;				// Ratio achieved at the end of the year
	private double GDPRateTarget;	
	private double GDPTarget; 					// (GDP level needed got target growth rate)
	private int 	 PrevailingAttitude; 		// Ranges from -5 to +5, represents attitude of 
												// public toward the carbon reduction where
												// a value of 10 is positive, 0 is ambivalent
	private int	KyotoEntryYears = 3; // enter kyoto protocol if targets met for X consecutive years.
	private int 	KyotoExitYears = 3;  // exit kyoto protocol if targets met for X consecutive years.
	
	// Save the carbonOutput and emissionsTarget values each year. Used to decide on joining
	// or leaving Kyoto. In function JoiningCriteriaMet(). 
	private Map<Integer, Double> emissionsTargetMap;	
	private Map<Integer, Double> carbonOutputMap = new HashMap<Integer, Double>();
	
	// Save the GDPRate, GDPRateTarget, IntensityRatio, IntensityTarget values each year. Uses
	private Map<Integer, Double> IntensityTargetMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> IntensityRatioMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> GDPRateTargetMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> GDPRateMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> GDPMap = new HashMap<Integer, Double>();
	
	// Calculate the average over the past four years and save
	private Map<Integer, Double> AverageIntensityRatioMap = new HashMap<Integer, Double>();	
	private Map<Integer, Double> AverageGDPRateMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> AverageGDPRateTargetMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> AverageIntensityTargetMap = new HashMap<Integer, Double>();
	
	//private ParticipantTimeService timeService;
	private int CurrentYear; // numerous functions use the current year, so made global. 
	private int TicksInYear;


	private boolean debug = true;

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#behaviour()
	 * called every tick
	 */
	public void behaviour() {
		
		double ProjectedGDP = this.CalculateProjectedGDP();
		double ProjectedIntensityRatio = this.CalculateIntensityRatio(); 
		double RequiredEnergyIncrease = 0;
		double InvestmentNeeded = 0;
		double InvestAmount = 0;
					
		if(IsElectionYear(CurrentYear+1)) { // if the following year is an election year. 
			if(IsLastTick()) { // if its the last tick of the year
				// Up until this point only CDM or trading will have taken place. 

				while( (this.CalculateProjectedGDP() < this.getGDPTarget()) // keep investing until target reached
						&& (InvestmentNeeded < this.getAvailableToSpend()) ) {				
					// TODO catch exceptions
					// TODO this only needs to be calculated once.
					RequiredEnergyIncrease = this.getEnergyOutput()*USAgent.EnergyIncreaseStep;				
					if(debug) logger.info("RequiredEnergyIncrease = " + RequiredEnergyIncrease);
					
					InvestmentNeeded = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(RequiredEnergyIncrease);				
					if(debug) logger.info("InvestmentNeeded = " + InvestmentNeeded);
					
					if(InvestmentNeeded > this.getAvailableToSpend()) { // check we have enough money
						InvestAmount = this.getAvailableToSpend();
					}
					else {
						InvestAmount = InvestmentNeeded;
					}
					
					if(debug) logger.info("InvestAmount = " + InvestAmount);
					try {
						energyUsageHandler.investInCarbonIndustry(InvestAmount);
					} catch (NotEnoughCashException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				while(ProjectedIntensityRatio > this.getIntensityTarget() // while target not reached 
				&& InvestmentNeeded < this.getAvailableToSpend()) { // and cash still remains.
					
					double RequiredCarbonOutputDecrease = -1*((this.getIntensityTarget()*CalculateProjectedGDP())-this.getCarbonOutput());
					if(debug) logger.info("RequiredCarbonReduction = " + RequiredEnergyIncrease);
					
					double AbsorptionInvestmentNeeded = carbonAbsorptionHandler.getInvestmentRequired(RequiredCarbonOutputDecrease);						
					if(debug) logger.info("AbsorptionInvestmentNeeded = " + AbsorptionInvestmentNeeded);
					
					double ReductionInvestmentNeeded = carbonReductionHandler.getInvestmentRequired(RequiredCarbonOutputDecrease);
					if(debug) logger.info("ReductionInvestmentNeeded = " + ReductionInvestmentNeeded);
					
					InvestmentNeeded = Math.min(AbsorptionInvestmentNeeded, AbsolutionReductionTarget);
					
					if(InvestmentNeeded > this.getAvailableToSpend()) { // check we have enough money
						InvestAmount = this.getAvailableToSpend();
					}
					else {
						InvestAmount = InvestmentNeeded;
					}
					
					if(debug) logger.info("InvestAmount = " + InvestAmount);
					try {
						energyUsageHandler.investInCarbonIndustry(InvestAmount);
					} catch (NotEnoughCashException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				/*
				// if the party might lose even after meeting targets
				if(CalculateGDPRateScore(USAgent.ElectionRandomAdjust/2) < CalculateIntensityScore(USAgent.ElectionRandomAdjust/2)) {
					// if under the current situation the republicans would lose.
					// invest whatever the public want
				}	
				*/			
			}
		}
	}
	
	private boolean IsLastTick() {
		if(timeService.getCurrentTick()==TicksInYear-1) {
			return(true);
		}
		else {
			return(false);
		}
	}

	private double CalculateProjectedGDP() {
		double ProjectedGDP;
		double marketStateFactor = GameConst.getStableMarketState();
		double sum;
		double EnergyOutput = getEnergyOutput();
		double PreviousEnergyOutput = getPrevEnergyOutput();
		double EnergyDifference = EnergyOutput-PreviousEnergyOutput;
		
		if (EnergyDifference >= 0){	
			sum = (((EnergyDifference)/PreviousEnergyOutput)*GameConst.getEnergyGrowthScaler()*marketStateFactor+getGDPRate()*100)/2;
			if (sum < 0) {
				ProjectedGDP = -(GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(sum*GameConst.getGrowthScaler()));
			}
			else {
				ProjectedGDP = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler());
			}
		}
		else{
			sum = ((EnergyDifference)/PreviousEnergyOutput)*GameConst.getEnergyGrowthScaler();
			sum = Math.abs(sum);
			ProjectedGDP = -(GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler()));
		}
		
		ProjectedGDP /= 100; // Needs to be a % for rate formula
		logger.info("ProjectedGDP = " + ProjectedGDP);
		return ProjectedGDP;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#YearlyFunction()
	 * Called by execute() on first tick of new year. Execute also calls:
		updateGDPRate();
		updateGDP();
		updateAvailableToSpend(); 
	 */
	public void yearlyFunction() {
		CurrentYear = timeService.getCurrentYear();
		
		StoreHistoricalData();		
		ProcessHistoricalData();
		
		if(IsElectionYear(CurrentYear)) {
			HoldElection(); // will set DemocratElected to either true or false
			SetTargets(); // emissions target
		}
		
		StoreTargetData();
		ProcessTargetData();
		
		/*
		if(isKyotoMember()!=KyotoMember.ANNEXONE) { // if we are not currently a member
			if(JoiningCriteriaMet()) {
				joinKyoto(); // function sets variable kyotoMemberLevel = KyotoMember.ANNEXONE
				// also saves the join time in terms of ticks elapsed. Note: since begin?
				// joining Kyoto adds us to the monitoring list, so then we pay taxes for this. 
			}
		}
		else { // otherwise we must be a member
			if(LeavingCriteriaMet()) {
				leaveKyoto();
			}
				
		}
		*/
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#SessionFunction()
	 * Called by execute on first tick of new session.
	 * Notes:
	 * Carbon offsets are wiped at the beginning of each session. 
	 */
	//
	public void sessionFunction() {
		
	}

	private void HoldElection() {		
		if(CalculateIntensityScore(Random.randomInt(USAgent.ElectionRandomAdjust)) 
		> CalculateGDPRateScore(Random.randomInt(USAgent.ElectionRandomAdjust))) {
			setDemocratElected(true);
			if(debug) logger.info("DemocratElected");
		}
		else {
			setDemocratElected(false);
			if(debug) logger.info("RepublicanElected");
		}		
	}
	
	private double CalculateGDPRateScore(int ElectionAdjust) {
		double CurrentGDPRate = this.getGDPRate();
		double CurrentGDPRateTarget = this.getGDPRateTarget();
		double GDPRateScore = (CurrentGDPRate/CurrentGDPRateTarget);
		int 	CurrentAttitude = this.getPrevailingAttitude();
		/*
		 * Current attitude ranges from -5 to + 5. -5 being very pro carbon reduction
		 * +5 very ambivalent, and by extension, pro GDP growth.
		 * Thus this will adjust the percentage scores achieved according to what the public feel.
		 */
		double AdjustedGDPRateScore = GDPRateScore + (CurrentAttitude*GDPRateScore) + (Random.randomInt(ElectionAdjust)*GDPRateScore);
		if(debug) logger.info("AdjustedGDPRateScore = " + AdjustedGDPRateScore);
		return(AdjustedGDPRateScore);
	}
	
	private double CalculateIntensityScore(int ElectionAdjust) {
		double CurrentIntensityRatio = this.getIntensityRatio();
		double CurrentIntensityTarget = this.getIntensityTarget();
		double IntensityScore = (CurrentIntensityRatio/CurrentIntensityTarget);
		int 	CurrentAttitude = this.getPrevailingAttitude();
		/*
		 * Current attitude ranges from -5 to + 5. -5 being very pro carbon reduction
		 * +5 very ambivalent, and by extension, pro GDP growth.
		 * Thus this will adjust the percentage scores achieved according to what the public feel.
		 */
		double AdjustedIntensityScore = IntensityScore + (-1*CurrentAttitude*IntensityScore) + ElectionAdjust*IntensityScore;
		if(debug) logger.info("AdjustedIntensityScore = " + AdjustedIntensityScore);				
		return AdjustedIntensityScore;
		
	}
	
	/*
	 * This function only called when country is instantiated. 
	 */
	private void SetInitialPoliticalParty() {
		int rand = Random.randomInt(100);		 
		if (rand < 50) {
			setDemocratElected(true);
			
		}
		else {
			setDemocratElected(false);
		}		
	}
	
	public void SetTargets() {
		if(isDemocratElected()) {
			CalculateAndSetNewGDPRateTarget(1);
			CalculateAndSetNewIntensityTarget(USAgent.DecreaseIntensityByPercent);
		}
		else {
			CalculateAndSetNewGDPRateTarget(USAgent.IncreaseGDPRateByPercent);
			CalculateAndSetNewIntensityTarget(1);	
		}
	}
	
	private void CalculateAndSetNewIntensityTarget(double Multiplier) {
		setIntensityTarget(AverageIntensityRatioMap.get(CurrentYear)*Multiplier);
	}
	
	private void CalculateAndSetNewGDPRateTarget(double Multiplier) {
		setGDPRateTarget(AverageGDPRateMap.get(CurrentYear)*Multiplier);
	}

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 * Called by execute when input objects are waiting on your agent. 
	 */
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input in) {
		if (this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
		else{
			OfferMessage offerMessage = this.tradeProtocol.decodeInput(in);
			if(AnalyzeOffer(offerMessage) ) {	
				try {
					this.tradeProtocol.respondToOffer(
							this.tradeProtocol.extractNetworkAddress(in), 
							offerMessage.getOfferQuantity(),
							offerMessage);
				} catch (IllegalArgumentException e1) {
					logger.warn(e1);
				} catch (FSMException e1) {
					logger.warn(e1);
				}
			}
		}	
	};
	
	protected boolean AnalyzeOffer(OfferMessage offerMessage) {
		if(offerMessage.getOfferType()==TradeType.RECEIVE) { // CDM type
			// Democrats will opt for reduction if it is cost effective regardless of whether target has already been met.
			if(getIntensityRatio() > getIntensityTarget()) {
				double OfferUnitCost = offerMessage.getOfferUnitCost();
				double OfferQuantity = offerMessage.getOfferQuantity();
				double TradeCost = OfferUnitCost*OfferQuantity;
				double EquivalentAbsorptionCost = carbonAbsorptionHandler.getInvestmentRequired(OfferQuantity);
				double EquivalentReductionCost = carbonReductionHandler.getInvestmentRequired(OfferQuantity);
				if(TradeCost < Math.min(EquivalentReductionCost, EquivalentAbsorptionCost)) {					
					return(true);
				}
			}						
		}
		return(false);
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false; 
	}
	
	public USAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, 
			double GDP, double GDPRate, double energyOutput, double carbonOutput) {
		
		super(id,name,ISO,landArea,arableLandArea,GDP,GDPRate,energyOutput,carbonOutput);
		
		initialise();
		/*  initialise adds country to:
		 * 	CarbonTargetService.
		 *  MonitorService - don't want this however
		 *  carbonAbsorptionHandler
			carbonReductionHandler
			energyUsageHandler
			ParticipantCarbonReportingService
			TimeService
			TradeProtocol
			
			Also calls initialiseCountry();
		 */		
	}
	
	@Override
	public void initialiseCountry() {
		
		if(debug) logger.info("initialiseCountry: Entering");
		leaveKyoto(); // US starts off outside of the protocol group
		// this function removes agent from the monitor service and sets
		// kyotoMemberLevel = KyotoMember.ROGUE;
		
		SetInitialPoliticalParty(); // assigned at random to start
		SetTargets();
		// save the ticks in year for later use in functions
		setTicksInYear(timeService.getTicksInYear());
		
		if(debug) logger.info("initialiseCountry: Returning");
	}
	
	private void StoreHistoricalData() {
		if(debug) logger.info("StoreHistoricalData: Entering");
		carbonOutputMap.put(CurrentYear, getCarbonOutput());	
		emissionsTargetMap.put(CurrentYear, getEmissionsTarget());	
		GDPRateMap.put(CurrentYear, getGDPRate());
		GDPMap.put(CurrentYear, getGDP());
		setIntensityRatio(CalculateIntensityRatio()); // Derived from GDP and CarbonOutput
		IntensityRatioMap.put(CurrentYear, getIntensityRatio());
		setGDPTarget(CalculateGDPTarget());
		if(debug) logger.info("StoreHistoricalData: Returning");
	}
	
	
	
	private double CalculateGDPTarget() {
		double value = getGDP()*getGDPRateTarget();
		if(debug) logger.info("CalculateGDPTarget: value = " + value);
		return value; 
	}

	private void StoreTargetData() {
		//IntensityTargetMap[CurrentYear] = getIntensityTarget();
		IntensityRatioMap.put(CurrentYear, getIntensityRatio());
		if(debug) logger.info("StoreTargetData: IntensityRatioMap[CurrentYear] = " + IntensityRatioMap.get(CurrentYear));
		
		GDPRateTargetMap.put(CurrentYear, getGDPRate());
		if(debug) logger.info("StoreTargetData: GDPRateTargetMap[CurrentYear] = " + GDPRateTargetMap.get(CurrentYear));
	}
	
	private double getIntensityRatio() {
		if(debug) logger.info("ProcessHistoricalData: IntensityRatio = " + IntensityRatio);
		return IntensityRatio;
	}

	private void ProcessHistoricalData() {
		double dGDPRate = 0;
		double dIntensityRatio = 0;		
//		E.g. Average1 = 4
		if(CurrentYear >= Average1) { // first calculated on election year
			if(debug) logger.info("ProcessTargetData: CurrentYear >= Average1");
			
			for(int i=CurrentYear; i >= CurrentYear - Average1; i--) {
				dGDPRate += GDPRateMap.get(i);
				dIntensityRatio += IntensityRatioMap.get(i);
			}

		}
		else {
			if(debug) logger.info("ProcessHistoricalData: CurrentYear >= Average1");
			
			for(int i=CurrentYear; i >= 0; i--) {
				dGDPRate += GDPRateMap.get(i);
				dIntensityRatio += IntensityRatioMap.get(i);
			}
		}
				
		AverageGDPRateMap.put(CurrentYear, dGDPRate/Average1);
		if(debug) logger.info("ProcessHistoricalData: AverageGDPRateMap.get(CurrentYear) = " + AverageGDPRateMap.get(CurrentYear));
		
		AverageIntensityRatioMap.put(CurrentYear, dIntensityRatio/Average1);
		if(debug) logger.info("ProcessHistoricalData: AverageIntensityRatioMap[CurrentYear] = " + AverageIntensityRatioMap.get(CurrentYear));
	}
	
	private void ProcessTargetData() {
		double dIntensityTarget = 0;
		double dGDPRateTarget = 0;
		
		if(CurrentYear >= Average1) { // first calculated on election year
			if(debug) logger.info("ProcessTargetData: CurrentYear >= Average1");
			
			for(int i=CurrentYear; i >= CurrentYear - Average1; i--) {
				dIntensityTarget += IntensityTargetMap.get(CurrentYear);				
				dGDPRateTarget += GDPRateTargetMap.get(CurrentYear);
			}			
		}
		else {
			if(debug) logger.info("ProcessTargetData: CurrentYear < Average1");
			
			for(int i=CurrentYear; i >= 0; i--) {
				dIntensityTarget += IntensityTargetMap.get(CurrentYear);				
				dGDPRateTarget += GDPRateTargetMap.get(CurrentYear);
			}
		}	
		
		AverageIntensityTargetMap.put(CurrentYear,dIntensityTarget/Average1);
		if(debug) logger.info("ProcessTargetData: AverageIntensityTargetMap[CurrentYear] = " + AverageIntensityTargetMap.get(CurrentYear));
		
		AverageGDPRateTargetMap.put(CurrentYear, dGDPRateTarget/Average1);
		if(debug) logger.info("ProcessTargetData: AverageGDPRateTargetMap[CurrentYear] = " + AverageGDPRateTargetMap.get(CurrentYear));
	}
	
	boolean LeavingCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = CurrentYear-1; i > CurrentYear - KyotoExitYears; i--) {
			if (carbonOutputMap.get(i) <= emissionsTargetMap.get(i)) {
				return(false);
			}
		}
		return(true); // 
	}
	
	boolean JoiningCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = CurrentYear-1; i > CurrentYear - KyotoEntryYears; i--) {
			if (carbonOutputMap.get(i) > emissionsTargetMap.get(i)) {
				return(false);
			}
		}
		return(true); // 
	}
	
	/*
	 * Functions returns true if it is an election year, false otherwise. 
	 */
	public boolean IsElectionYear(int Year) {
		if (Year % 4 == 0) {
			if(debug) logger.info("IsElectionYear: true");
			return(true);
		}
		else {
			if(debug) logger.info("IsElectionYear: false");
			return(false);	
		}	
	}
	
	public double getIntensityTarget() {
		if(debug) logger.info("getIntensityTarget: IntensityTarget = " + IntensityTarget);
		return IntensityTarget;
	}

	public void setIntensityTarget(double intensityTarget) {
		if(debug) logger.info("setIntensityTarget: IntensityTarget = " + IntensityTarget);
		IntensityTarget = intensityTarget;
	}

	private double CalculateIntensityRatio() {
		double result = getGDP() / getCarbonOutput();
		if(debug) logger.info("CalculateIntensityRatio: result = " + result);
		return(result);
	}
	
	public boolean isDemocratElected() {
		if(debug) logger.info("isDemocratElected: DemocratElected = " + DemocratElected);
		return DemocratElected;
	}

	public void setDemocratElected(boolean democratElected) {
		if(debug) logger.info("setDemocratElected: DemocratElected = " + DemocratElected);
		DemocratElected = democratElected;
	}
	
	public void setIntensityRatio(double intensityRatio) {
		if(debug) logger.info("setIntensityRatio: IntensityRatio = " + IntensityRatio);
		IntensityRatio = intensityRatio;
	}
	public double getIntensityRatio(double intensityRatio) {
		if(debug) logger.info("getIntensityRatio: IntensityRatio = " + IntensityRatio);
		return IntensityRatio;
	}

	public double getGDPRateTarget() {
		if(debug) logger.info("setGDPRateTarget: GDPRateTarget = " + GDPRateTarget);
		return GDPRateTarget;
	}
	public void setGDPRateTarget(double gDPRateTarget) {
		if(debug) logger.info("setGDPRateTarget: GDPRateTarget = " + GDPRateTarget);
		GDPRateTarget = gDPRateTarget;
	}

	public double getGDPTarget() {
		if(debug) logger.info("getGDPTarget: GDPTarget = " + GDPTarget);
		return GDPTarget;
	}

	public void setGDPTarget(double gDPTarget) {
		if(debug) logger.info("setGDPTarget: GDPTarget = " + GDPTarget);
		GDPTarget = gDPTarget;
	}

	public int getPrevailingAttitude() {
		if(debug) logger.info("getPrevailingAttitude: PrevailingAttitude = " + PrevailingAttitude);
		return PrevailingAttitude;
	}

	public void setPrevailingAttitude(int prevailingAttitude) {
		if(debug) logger.info("setPrevailingAttitude: PrevailingAttitude = " + PrevailingAttitude);
		PrevailingAttitude = prevailingAttitude;
	}
	public int getTicksInYear() {
		if(debug) logger.info("getTicksInYear: TicksInYear = " + TicksInYear);
		return TicksInYear;
	}

	public void setTicksInYear(int ticksInYear) {
		if(debug) logger.info("setTicksInYear: TicksInYear = " + TicksInYear);
		TicksInYear = ticksInYear;
	}

}
