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
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.ic.kyoto.trade.TradeType;

public class USAgent extends AbstractCountry {

	private static final int 		Average1 = 4;
	private static final double  DecreaseIntensityByPercent = 0.95;
	private static final double  IncreaseGDPRateByPercent = 1.05;
	private static final int 		ElectionRandomAdjust = 0;
	private static final double 	EnergyIncreaseStep = 0.01; // increase by one percent until tagret reached
	
	private boolean DemocratElected; 			// chosen at random on class instantiation
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
	private static boolean DecrementAttitude = true;

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#behaviour()
	 * called every tick
	 */
	public void behaviour() {
		logger.info("behaviour: Entering");

		double IncreaseEnergyByAmount = 0;
		double InvestmentNeeded = 0;
		double InvestAmount = 0;
		boolean EnoughCash = true;
					
		if(IsElectionYear(CurrentYear+1)) { // if the following year is an election year. 
			if(IsLastTick()) { // if its the last tick of the year
				// Up until this point only CDM or trading will have taken place. 

				while( (this.CalculateProjectedGDP() < this.getGDPTarget()) // keep investing until target reached
						&& EnoughCash ) {	// will not enter if cost for next 
					// step is more than we have available		
					// TODO catch exceptions
					IncreaseEnergyByAmount = this.getEnergyOutput()*USAgent.EnergyIncreaseStep;	// will change after each step is completed			
					if(debug) logger.info("behaviour: IncreaseEnergyByAmount = " + IncreaseEnergyByAmount);
					
					InvestmentNeeded = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(IncreaseEnergyByAmount);				
					if(debug) logger.info("behaviour: InvestmentNeeded = " + InvestmentNeeded);
					
					InvestAmount = CheckInvestmentAmount(InvestmentNeeded); 
					
					if(debug) logger.info("behaviour: InvestAmount = " + InvestAmount);
					try {
						energyUsageHandler.investInCarbonIndustry(InvestAmount);
					} catch (NotEnoughCashException e) {
						EnoughCash = false;
						// TODO Make EnoughCash etc global fields so that once set agent won't keep re-attempting
						e.printStackTrace();
					}
				}
				
				double RequiredCarbonOutputDecrease = 0;
				double AbsorptionInvestmentNeeded = 0;
				double ReductionInvestmentNeeded = 0;
				double ReduceBy = 0;				
				boolean EnoughLand = true;				
				boolean EnoughCash2 = true;
				boolean EnoughCarbon = true;

				while(this.CalculateIntensityRatio() > this.getIntensityTarget() // while target not reached 
				&& EnoughCash2 && EnoughLand && EnoughCarbon) { // booleans set to false if corresponding exception thrown
					
					RequiredCarbonOutputDecrease = -1*((this.getIntensityTarget()*CalculateProjectedGDP())-this.getCarbonOutput());
					if(debug) logger.info("behaviour: RequiredCarbonOutputDecrease = " + RequiredCarbonOutputDecrease);
					
					AbsorptionInvestmentNeeded = carbonAbsorptionHandler.getInvestmentRequired(RequiredCarbonOutputDecrease);						
					if(debug) logger.info("behaviour: AbsorptionInvestmentNeeded = " + AbsorptionInvestmentNeeded);
					
					ReductionInvestmentNeeded = carbonReductionHandler.getInvestmentRequired(RequiredCarbonOutputDecrease);
					if(debug) logger.info("behaviour: ReductionInvestmentNeeded = " + ReductionInvestmentNeeded);
					
					if(AbsorptionInvestmentNeeded < ReductionInvestmentNeeded) {
						InvestAmount = CheckInvestmentAmount(AbsorptionInvestmentNeeded);
						// TODO check available land
						ReduceBy = carbonAbsorptionHandler.getCarbonAbsorptionChange(InvestAmount);
						try {
							carbonAbsorptionHandler.investInCarbonAbsorption(ReduceBy);
						} catch (NotEnoughLandException e) {
							EnoughLand = false;
							e.printStackTrace();
						} catch (NotEnoughCashException e) {
							EnoughCash = false;
							e.printStackTrace();
						}
					}
					else {
						InvestAmount = CheckInvestmentAmount(ReductionInvestmentNeeded);
						ReduceBy = carbonReductionHandler.getCarbonOutputChange(InvestAmount, this.getCarbonOutput(), this.getEnergyOutput());
						try {
							carbonReductionHandler.investInCarbonReduction(ReduceBy);
						} catch (NotEnoughCarbonOutputException e) {
							EnoughCarbon = false;
							e.printStackTrace();
						} catch (NotEnoughCashException e) {
							EnoughCash2 = false;
							e.printStackTrace();
						}
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
		logger.info("behaviour: Returning");
	}
	
	private double CheckInvestmentAmount(double investmentNeeded) {
		double InvestAmount;
		if(investmentNeeded > this.getAvailableToSpend()) { // check we have enough money
			logger.info("CheckInvestmentAmount: InvestmentNeeded > this.getAvailableToSpend()");
			InvestAmount = this.getAvailableToSpend();
		}
		else {
			InvestAmount = investmentNeeded;
		}
		if(debug) logger.info("CheckInvestmentAmount: InvestAmount = " + InvestAmount);
		return(InvestAmount);
	}

	private boolean IsLastTick() {
		logger.info("IsLastTick: Entering");
		if(timeService.getCurrentTick()==TicksInYear-1) {
			logger.info("IsLastTick: Returning true");
			return(true);
		}
		else {
			logger.info("IsLastTick: Returning false");
			return(false);
		}
		
	}

	private double CalculateProjectedGDP() {
		logger.info("CalculateProjectedGDP: Entering");
		double ProjectedGDP;
		double marketStateFactor = GameConst.getStableMarketState();
		double sum;
		double EnergyOutput = getEnergyOutput();
		double PreviousEnergyOutput = getPrevEnergyOutput(); // TODO what value needed to ensure working?
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
		logger.info("CalculateProjectedGDP: ProjectedGDP = " + ProjectedGDP);
		logger.info("CalculateProjectedGDP: Returning");
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
		if(debug) logger.info("yearlyFunction: Entering");
		CurrentYear = timeService.getCurrentYear();
		
		setPrevailingAttitude(ChangeAttitude());
		
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
		if(debug) logger.info("yearlyFunction: Returning");
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
	// TODO what should be here?	
	}

	private void HoldElection() {		
		if(debug) logger.info("HoldElection: Entering");
		if(CalculateIntensityScore(Random.randomInt(USAgent.ElectionRandomAdjust)) 
		> CalculateGDPRateScore(Random.randomInt(USAgent.ElectionRandomAdjust))) {
			setDemocratElected(true);
			if(debug) logger.info("HoldElection: DemocratElected");
		}
		else {
			setDemocratElected(false);
			if(debug) logger.info("HoldElection: RepublicanElected");
		}
		if(debug) logger.info("HoldElection: Returning");
	}
	
	private double CalculateGDPRateScore(double ElectionAdjust) {
		double CurrentGDPRate = this.getAverageGDPRate(CurrentYear);
		double CurrentGDPRateTarget = this.getGDPRateTarget();
		
		double GDPRateScore = (CurrentGDPRate/CurrentGDPRateTarget);
		if(debug) logger.info("CalculateGDPRateScore: GDPRateScore = " + GDPRateScore);
		
		int 	CurrentAttitude = this.getPrevailingAttitude();
		double AttitudeFactor = 1+(CurrentAttitude/10);
		if(debug) logger.info("CalculateGDPRateScore: AttitudeFactor = " + AttitudeFactor);
		/*
		 * Current attitude ranges from -5 to + 5. -5 being very pro carbon reduction
		 * +5 very ambivalent, and by extension, pro GDP growth.
		 * Thus this will adjust the percentage scores achieved according to what the public feel.
		 */
		double PartyFactor;
		if(!isDemocratElected()) { // republicans get a boost to their GDPScore
			PartyFactor = 1.2;
		}
		else {
			PartyFactor = 1;
		}
		if(debug) logger.info("CalculateGDPRateScore: PartyFactor = " + PartyFactor);
		
		double AdjustedGDPRateScore = GDPRateScore*PartyFactor*AttitudeFactor*(1+ElectionAdjust);
		if(debug) logger.info("CalculateGDPRateScore: AdjustedGDPRateScore = " + AdjustedGDPRateScore);
		
		if(debug) logger.info("CalculateGDPRateScore: Returning");
		return(AdjustedGDPRateScore);
		
	}
	
	private double getAverageGDPRate(int key) {
		double value = AverageGDPRateMap.get(key);
		if(debug) logger.info("getAverageGDPRate: AverageGDPRateMap.get(" + key + ") = "+ value);
		return value;
	}

	private double CalculateIntensityScore(double ElectionAdjust) {
		if(debug) logger.info("CalculateIntensityScore: Entering");
		double CurrentIntensityRatio = this.getIntensityRatio();
		double CurrentIntensityTarget = this.getIntensityTarget();
		double IntensityScore = (CurrentIntensityRatio/CurrentIntensityTarget);
		if(debug) logger.info("CalculateIntensityScore: IntensityScore = " + IntensityScore);
		
		int 	CurrentAttitude = this.getPrevailingAttitude();
		double AttitudeFactor = 1+((-1*CurrentAttitude)/10);
		if(debug) logger.info("CalculateGDPRateScore: AttitudeFactor = " + AttitudeFactor);
		/*
		 * Current attitude ranges from -5 to + 5. -5 being very pro carbon reduction
		 * +5 very ambivalent, and by extension, pro GDP growth.
		 * Thus this will adjust the percentage scores achieved according to what the public feel.
		 */
		double PartyFactor;
		if(isDemocratElected()) { // democrats get a boost to their intensity score
			PartyFactor = 1.2;
		}
		else {
			PartyFactor = 1;
		}
		if(debug) logger.info("CalculateGDPRateScore: PartyFactor = " + PartyFactor);
		
		double AdjustedIntensityScore = IntensityScore*AttitudeFactor*PartyFactor*(1+ElectionAdjust);
		if(debug) logger.info("CalculateIntensityScore: AdjustedIntensityScore = " + AdjustedIntensityScore);				
		
		if(debug) logger.info("CalculateIntensityScore: Returning");
		return AdjustedIntensityScore;
		
	}
	
	/*
	 * This function only called when country is instantiated. 
	 */
	private void SetInitialPoliticalParty() {
		if(debug) logger.info("SetInitialPoliticalParty: Entering");
		int rand = Random.randomInt(100);		 
		if(debug) logger.info("SetInitialPoliticalParty: rand = " + rand);
		if (rand < 50) {
			setDemocratElected(true);			
		}
		else {
			setDemocratElected(false);
		}
		if(debug) logger.info("SetInitialPoliticalParty: Returning");
	}
	
	public void SetTargets() {
		if(debug) logger.info("SetTargets: Entering");
		if(isDemocratElected()) {
			if(debug) logger.info("SetTargets: Democrat Elected");
			CalculateAndSetNewGDPRateTarget(1);
			CalculateAndSetNewIntensityTarget(USAgent.DecreaseIntensityByPercent);
		}
		else {
			if(debug) logger.info("SetTargets: Republican Elected");
			CalculateAndSetNewGDPRateTarget(USAgent.IncreaseGDPRateByPercent);
			CalculateAndSetNewIntensityTarget(1);	
		}
		if(debug) logger.info("SetTargets: Returning");
	}
	
	private void CalculateAndSetNewIntensityTarget(double Multiplier) {
		double value = AverageIntensityRatioMap.get(CurrentYear)*Multiplier;
		if(debug) logger.info("CalculateAndSetNewIntensityTarget: value = " + value);
		setIntensityTarget(value);
	}
	
	private void CalculateAndSetNewGDPRateTarget(double Multiplier) {
		double value = AverageGDPRateMap.get(CurrentYear)*Multiplier;
		if(debug) logger.info("CalculateAndSetNewGDPRateTarget: value = " + value);
		setGDPRateTarget(value);
	}

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 * Called by execute when input objects are waiting on your agent. 
	 */
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input in) {
		if(debug) logger.info("processInput: Entering");
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
		if(debug) logger.info("processInput: Returning");
	};
	
	protected boolean AnalyzeOffer(OfferMessage offerMessage) {
		if(debug) logger.info("AnalyzeOffer: Entering");
		if(offerMessage.getOfferType()==TradeType.RECEIVE) { // CDM type
			// Democrats will opt for reduction if it is cost effective regardless of whether target has already been met.
			if(debug) logger.info("AnalyzeOffer: TradeType==RECEIVE");
			if(getIntensityRatio() > getIntensityTarget()) {
				if(debug) logger.info("AnalyzeOffer: getIntensityRatio() > getIntensityTarget()");
				double OfferUnitCost = offerMessage.getOfferUnitCost();				
				double OfferQuantity = offerMessage.getOfferQuantity();
				double TradeCost = OfferUnitCost*OfferQuantity;
				double EquivalentAbsorptionCost = carbonAbsorptionHandler.getInvestmentRequired(OfferQuantity);
				double EquivalentReductionCost = carbonReductionHandler.getInvestmentRequired(OfferQuantity);
				if(TradeCost < Math.min(EquivalentReductionCost, EquivalentAbsorptionCost)) {					
					if(debug) logger.info("AnalyzeOffer: Returning true");
					return(true);
				}
			}						
		}
		if(debug) logger.info("AnalyzeOffer: Returning false");
		return(false);
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		if(debug) logger.info("acceptTrade: Entering");
		// TODO Auto-generated method stub
		if(debug) logger.info("acceptTrade: Returning");
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
		SetInitialAttitude();
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
	
	private int ChangeAttitude() {
		int CurrentAttitude = this.getPrevailingAttitude();
		int NewAttitude = CurrentAttitude;
		
		if(CurrentAttitude==5) {
			DecrementAttitude = true;
		}
		else {
			if(CurrentAttitude==-5) {
				DecrementAttitude = false;
			}
		}
		// advance attitude with probability 1/2
		if(Random.randomInt(100) < 50) {
			if(!DecrementAttitude) {
				NewAttitude = Math.min(5, CurrentAttitude + Random.randomInt(2));
			}
			else {
				NewAttitude = Math.max(-5, CurrentAttitude + Random.randomInt(-2));	
			}
		}
		
		return(NewAttitude);
	}
	
	private int SetInitialAttitude() {
		// TODO: ask about this
		int value = Random.randomInt(5) + Random.randomInt(-5);
		if(debug) logger.info("SetInitialAttitude: value = " + value);
		return(value);
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
