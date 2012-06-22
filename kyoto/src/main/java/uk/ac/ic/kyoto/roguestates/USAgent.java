package uk.ac.ic.kyoto.roguestates;

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
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.ic.kyoto.trade.TradeType;

public class USAgent extends AbstractCountry {

	private static final int Average1 = 4;
	private static final double DecreaseIntensityByPercent = 0.95;
	private static final double IncreaseGDPRateByPercent = 1.05;
	private static final int ElectionRandomAdjust = 0;
	
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
	private double[] carbonOutputArray;
	private double[] emissionsTargetArray;
	
	// Save the GDPRate, GDPRateTarget, IntensityRatio, IntensityTarget values each year. Uses
	private double[] IntensityTargetArray;
	private double[] IntensityRatioArray;
	private double[] GDPRateTargetArray;
	private double[] GDPRateArray;
	private double[] GDPArray;
	
	// Calculate the average over the past four years and save
	private double[] AverageIntensityRatio = null;	
	private double[] AverageGDPRate = null;
	private double[] AverageGDPRateTarget = null;
	private double[] AverageIntensityTarget = null;
	
	//private ParticipantTimeService timeService;
	private int CurrentYear; // numerous functions use the current year, so made global. 
	private boolean debug;

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#behaviour()
	 * called every tick
	 */
	public void behaviour() {
		double ProjectedGDP = this.CalculateProjectedGDP();
		double ProjectedIntensityRatio = this.CalculateIntensityRatio(); 
		double RequiredEnergyIncrease;
		double InvestmentNeeded;
		
		// if republicans in power
		if(!this.isDemocratElected()) { // the order in which meeting targets are prioritised 
			// differs depending on the party in power
			if(this.CalculateProjectedGDP() < this.getGDPTarget()) {
				RequiredEnergyIncrease = this.getEnergyOutput()*getGDPRateTarget()/100;				
				InvestmentNeeded = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(RequiredEnergyIncrease);				
				energyUsageHandler.investInCarbonIndustry(InvestmentNeeded);
			}
			else { // if the 
				if(ProjectedIntensityRatio > this.getIntensityTarget()) {
					/*
					if(isKyotoMember()) {
						if(TradeOffersExist()) {
							// if trade is more cost effective than normal reduction methods, accept
						}
						if(!TradeProposalsExist(1)) { // if we don't have any open trades
							// propose trade if it is cost effective
							// Calculate the cost per unit carbon were we to use only internal methods of reduction
							// propose trade at a value less than this price, monitor trade size of other agents
							// quantity propose at average of these
						}
					}
					*/
				}
			}
								
			// if its the last tick of the year
			if(timeService.getCurrentTick()==timeService.getTicksInYear()-1) {
				// perform all action to meet targets if needed
				if(ProjectedIntensityRatio > this.getIntensityTarget()) {
					// 
				}
				// if the party might lose even after meeting targets
				if(CalculateGDPRateScore(USAgent.ElectionRandomAdjust/2) 
						< CalculateIntensityScore(USAgent.ElectionRandomAdjust/2)) {
					// if under the current situation the republicans would lose.
					// invest in energy
				}				
			}
		}
		else { // if democrats in power
			// try to fulfil commitments through cost effective trades
			if(ProjectedIntensityRatio > this.getIntensityTarget()) {
				/*
				if(isKyotoMember()==KyotoMember.ANNEXONE) {
					if(TradeOffersExist()) {
						// if trade is more cost effective than normal reduction methods, accept
					}
					if(!TradeProposalsExist(1)) { // if we don't have any open trades
						// propose trade if it is cost effective
						// Calculate the cost per unit carbon were we to use only internal methods of reduction
						// propose trade at a value less than this price, monitor trade size of other agents
						// quantity propose at average of these
					}
				}
				*/
				if(CDMOffersExist()) {
					// if cmd offer is more cost effective than normal reduction methods accept
				}
			}
			else {
				if(ProjectedGDPRate < this.getGDPRateTarget()) {				
					//IncreaseEnergy
				}
			}

			// if its the last tick
			if(timeService.getCurrentTick()==timeService.getTicksInYear()-1) {
				// perform all action to meet targets if needed
				if(ProjectedIntensityRatio > this.getIntensityTarget()) {
					// 
					
				}
				if(ProjectedGDPRate < this.getGDPRateTarget()) {				
					//IncreaseEnergy
				}
				// if the party might lose even after meeting targets
				if(CalculateIntensityScore(USAgent.ElectionRandomAdjust/2) 
						< CalculateGDPRateScore(USAgent.ElectionRandomAdjust/2)) {
					// if under the current situation the republicans would lose.
					// invest in reduction
				}				
			}
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
		
		if(IsElectionYear()) {
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
		setIntensityTarget(AverageIntensityRatio[CurrentYear]*Multiplier);
	}
	
	private void CalculateAndSetNewGDPRateTarget(double Multiplier) {
		setGDPRateTarget(AverageGDPRate[CurrentYear]*Multiplier);
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
			if(DemocratElected || getIntensityRatio() > getIntensityTarget()) {
				double OfferUnitCost = offerMessage.getOfferUnitCost();
				double OfferQuantity = offerMessage.getOfferQuantity();
				double  TradeCost = OfferUnitCost*OfferQuantity;
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
		
		leaveKyoto(); // US starts off outside of the protocol group
		// this function removes agent from the monitor service and sets
		// kyotoMemberLevel = KyotoMember.ROGUE;
		
		carbonOutputArray = new double[100];
		emissionsTargetArray = new double[100];
		IntensityTargetArray = new double[100];
		IntensityRatioArray = new double[100];
		GDPRateTargetArray = new double[100];
		GDPRateArray = new double[100];	
		
		AverageIntensityRatio = new double[100];	
		AverageGDPRate = new double[100];
		AverageGDPRateTarget = new double[100];
		AverageIntensityTarget = new double[100];
		
		SetInitialPoliticalParty(); // assigned at random to start
		SetTargets();
	}
	
	private void StoreHistoricalData() {
		carbonOutputArray[CurrentYear] =  getCarbonOutput();	
		emissionsTargetArray[CurrentYear] =  getEmissionsTarget();	
		GDPRateArray[CurrentYear] =  getGDPRate();
		GDPArray[CurrentYear] =  getGDP();
		setIntensityRatio(CalculateIntensityRatio()); // Derived from GDP and CarbonOutput
		IntensityRatioArray[CurrentYear] = getIntensityRatio();
		setGDPTarget(CalculateGDPTarget());
	}
	
	
	
	private double CalculateGDPTarget() {
		double value = getGDP()*getGDPRateTarget();
		
		return value; 
	}

	private void StoreTargetData() {
		//IntensityTargetArray[CurrentYear] = getIntensityTarget();
		IntensityRatioArray[CurrentYear] = getIntensityRatio();
		GDPRateTargetArray[CurrentYear] = getGDPRate();
	}
	
	private double getIntensityRatio() {
		// TODO Auto-generated method stub
		return IntensityRatio;
	}

	private void ProcessHistoricalData() {
		double dGDPRate = 0;
		double dIntensityRatio = 0;		
//		E.g. Average1 = 4
		if(CurrentYear >= Average1) { // first calculated on election year
			for(int i=CurrentYear; i >= CurrentYear - Average1; i--) {
				dGDPRate += GDPRateArray[i];
				dIntensityRatio += IntensityRatioArray[i];
			}
			AverageGDPRate[CurrentYear] = dGDPRate/Average1;
			AverageIntensityRatio[CurrentYear] = dIntensityRatio/Average1;
		}
		else {
			for(int i=CurrentYear; i >= 0; i--) {
				dGDPRate += GDPRateArray[i];
				dIntensityRatio += IntensityRatioArray[i];
			}
			AverageGDPRate[CurrentYear] = dGDPRate/CurrentYear;
			AverageIntensityRatio[CurrentYear] = dIntensityRatio/CurrentYear;			
		}
	}
	
	private void ProcessTargetData() {
		double dIntensityTarget = 0;
		double dGDPTarget = 0;
		
		if(CurrentYear >= Average1) { // first calculated on election year
			for(int i=CurrentYear; i >= CurrentYear - Average1; i--) {
				dIntensityTarget += IntensityTargetArray[i];				
				dGDPTarget += GDPRateTargetArray[i];
			}
			AverageIntensityTarget[CurrentYear] = dIntensityTarget/Average1;
			AverageGDPRateTarget[CurrentYear] = dGDPTarget/Average1;
		}
		else {
			for(int i=CurrentYear; i >= 0; i--) {
				dIntensityTarget += IntensityTargetArray[i];				
				dGDPTarget += GDPRateTargetArray[i];
			}
			AverageIntensityTarget[CurrentYear] = dIntensityTarget/CurrentYear;
			AverageGDPRateTarget[CurrentYear] = dGDPTarget/CurrentYear;			
		}
	}
	
	boolean LeavingCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = CurrentYear-1; i > CurrentYear - KyotoExitYears; i--) {
			if (carbonOutputArray[i] <= emissionsTargetArray[i]) {
				return(false);
			}
		}
		return(true); // 
	}
	
	boolean JoiningCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = CurrentYear-1; i > CurrentYear - KyotoEntryYears; i--) {
			if (carbonOutputArray[i] > emissionsTargetArray[i]) {
				return(false);
			}
		}
		return(true); // 
	}
	
	/*
	 * Functions returns true if it is an election year, false otherwise. 
	 */
	public boolean IsElectionYear() {
		if (CurrentYear % 4 == 0) {
			return(true);
		}
		else {
			return(false);	
		}	
	}
	
	public double getIntensityTarget() {
		return IntensityTarget;
	}

	public void setIntensityTarget(double intensityTarget) {
		IntensityTarget = intensityTarget;
	}

	private double CalculateIntensityRatio() {
		double result = getGDP() / getCarbonOutput();
		return(result);
	}
	
	/*
	private double CalculateProjectedIntensity() {
		double result = this.CalculateProjectedGDP() / getCarbonOutput; 
	}
	*/

	public void setIntensityRatio(double intensityRatio) {
		IntensityRatio = intensityRatio;
	}
	
	public boolean isDemocratElected() {
		return DemocratElected;
	}

	public void setDemocratElected(boolean democratElected) {
		DemocratElected = democratElected;
	}

	public double getIntensityRatio(double intensityRatio) {
		return IntensityRatio;
	}

	public double getGDPRateTarget() {
		return GDPRateTarget;
	}

	public void setGDPRateTarget(double gDPRateTarget) {
		GDPRateTarget = gDPRateTarget;
	}

	public double getGDPTarget() {
		return GDPTarget;
	}

	public void setGDPTarget(double gDPTarget) {
		GDPTarget = gDPTarget;
	}

	public int getPrevailingAttitude() {
		return PrevailingAttitude;
	}

	public void setPrevailingAttitude(int prevailingAttitude) {
		PrevailingAttitude = prevailingAttitude;
	}

}
