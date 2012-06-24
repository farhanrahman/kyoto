package uk.ac.ic.kyoto.roguestates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.exceptions.CannotJoinKyotoException;
import uk.ac.ic.kyoto.exceptions.CannotLeaveKyotoException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.fsm.FSMException;

public class USAgent extends AbstractCountry {

	private static final int 		Average1 = 4;
	private static final double  DecreaseIntensityByPercent = 0.99;
	private static final double  IncreaseGDPByPercent = 1.01;
	
	private boolean DemocratElected; 			// chosen at random on class instantiation
												// Can be positive or negative
	private double IntensityTarget; 			// Target for the year
	private double GDPRateTarget;	
	private double GDPTarget; 					// (GDP level needed got target growth rate)
	private int 	 PrevailingAttitude; 		// Ranges from -5 to +5, represents attitude of 
												// public toward the carbon reduction where
												// a value of 10 is positive, 0 is ambivalent
	private int	KyotoEntryYears = 3; // enter kyoto protocol if targets met for X consecutive years.
	private int 	KyotoExitYears = 3;  // exit kyoto protocol if targets met for X consecutive years.
	
	// Save the carbonOutput and emissionsTarget values each year. Used to decide on joining
	// or leaving Kyoto. In function JoiningCriteriaMet(). 
	private Map<Integer, Double> emissionsTargetMap = new HashMap<Integer, Double>();;	
	private Map<Integer, Double> carbonOutputMap = new HashMap<Integer, Double>();
	
	// Save the GDPRate, GDPRateTarget, IntensityRatio, IntensityTarget values each year. Uses
	//private Map<Integer, Double> IntensityTargetMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> IntensityRatioMap = new HashMap<Integer, Double>();
	//private Map<Integer, Double> GDPRateTargetMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> GDPRateMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> GDPMap = new HashMap<Integer, Double>();
	private Map<Integer, Double> AverageGDPMap = new HashMap<Integer, Double>();
	// Calculate the average over the past four years and save
	private Map<Integer, Double> AverageIntensityRatioMap = new HashMap<Integer, Double>();	
	private Map<Integer, Double> AverageGDPRateMap = new HashMap<Integer, Double>();
	//private Map<Integer, Double> AverageGDPRateTargetMap = new HashMap<Integer, Double>();
	//private Map<Integer, Double> AverageIntensityTargetMap = new HashMap<Integer, Double>();
	
	//private ParticipantTimeService timeService;

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

		//if(IsElectionYear(timeService.getCurrentYear()+1)) { // if the following year is an election year. 
			
		if(IsLastTick()) { // if its the last tick of the year
			
			// Up until this point only CDM or trading will have taken place. 

			if(isDemocratElected()) {
				DoCarbonReduction();
				DoEnergyInvestments();					
			}
			else {
				DoEnergyInvestments();
				DoCarbonReduction();					
			}
		}
			/*
			// if the party might lose even after meeting targets
			if(CalculateGDPRateScore(USAgent.ElectionRandomAdjust/2) < CalculateIntensityScore(USAgent.ElectionRandomAdjust/2)) {
				// if under the current situation the republicans would lose.
				// invest whatever the public want
			}	
			*/			

		if (isKyotoMember() == KyotoMember.ANNEXONE) {
			if (getCarbonOutput() - getCarbonOffset() - getCarbonAbsorption() > getEmissionsTarget()) {
				double totalDifferenceNeeded = getCarbonOutput() - (getCarbonOffset() + getCarbonAbsorption() + getEmissionsTarget());
				double absorptionCost;
				try {
					absorptionCost = carbonAbsorptionHandler.getInvestmentRequired(totalDifferenceNeeded);
				} catch (NotEnoughLandException e) {
					absorptionCost = Double.MAX_VALUE;
				}
				double reductionCost = carbonReductionHandler.getInvestmentRequired(totalDifferenceNeeded);
				double actualInvestment;
				double factor = getTradeFactorDifference();
				if (reductionCost < absorptionCost) {
					actualInvestment = reductionCost - reductionCost*factor;
				}
				else {
					actualInvestment = absorptionCost - absorptionCost*factor;
				}
				broadcastBuyOffer(totalDifferenceNeeded, actualInvestment / totalDifferenceNeeded);
			}
			else {
				double totalFreeOffset = getEmissionsTarget() + getCarbonOffset() + getCarbonAbsorption() - getCarbonOutput();
				double actualInvestment;
				double factor = getTradeFactorDifference();
				double absorptionCost;
				try {
					absorptionCost = carbonAbsorptionHandler.getInvestmentRequired(totalFreeOffset);
				} catch (NotEnoughLandException e) {
					absorptionCost = Double.MAX_VALUE;
				}
				
				double reductionCost = carbonReductionHandler.getInvestmentRequired(totalFreeOffset);
				
				if (reductionCost < absorptionCost) {
					actualInvestment = reductionCost + reductionCost*factor;
				}
				else {
					actualInvestment = absorptionCost + absorptionCost*factor;
				}
				broadcastSellOffer(totalFreeOffset, actualInvestment / totalFreeOffset);
			}
		}
		logger.info("behaviour: Returning");
	}
	
	private void DoEnergyInvestments() {
		
		double InvestmentNeeded = 0;
		double RequiredCarbonOutputIncrease = 0;
		
		boolean EnergyEnoughCash = true;
		
		while( this.getCarbonOutput() < this.CalculateTargetCarbonOutput() && EnergyEnoughCash ) {
		
			// HOW MUCH CASH DO WE HAVE
			double AvailableCash = this.getAvailableToSpend();
			if(debug) logger.info("behaviour: AvailableCash = " + AvailableCash);
			
			// WHAT INCREASE DO WE NEED TO MEET TARGET
			double CarbonOutput = this.getCarbonOutput();
			if(debug) logger.info("behaviour: CarbonOutput = " + CarbonOutput);
			
			double TargetCarbonOutput = this.CalculateTargetCarbonOutput();
			if(debug) logger.info("behaviour: TargetCarbonOutput = " + TargetCarbonOutput);					
			
			RequiredCarbonOutputIncrease = TargetCarbonOutput-CarbonOutput;
			if(debug) logger.info("behaviour: RequiredCarbonOutputDecrease = " + RequiredCarbonOutputIncrease);
			
			double MaximumIncreasePossible = energyUsageHandler.calculateCarbonIndustryGrowth(AvailableCash)*0.9;
			
			InvestmentNeeded = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(Math.min(RequiredCarbonOutputIncrease, MaximumIncreasePossible));				
			if(debug) logger.info("behaviour: InvestmentNeeded = " + InvestmentNeeded);
			
			if(debug) logger.info("behaviour: InvestmentNeeded = " + InvestmentNeeded);
			
			try {
				energyUsageHandler.investInCarbonIndustry(InvestmentNeeded);
			} catch (NotEnoughCashException e) {
				EnergyEnoughCash = false;
				e.printStackTrace();
			}
		}
	}

	private void DoCarbonReduction() {
		double RequiredCarbonOutputDecrease = 0;
		double AbsorptionInvestmentNeeded = 0;
		double ReductionInvestmentNeeded = 0;
		double ReduceBy = 0;	
		boolean CarbonEnoughCash = true;
		boolean EnoughLand = true;
		boolean EnoughCarbon = true;
		
		while((this.CalculateCurrentIntensityRatio() > this.CalculateProjectedIntensityRatio()) // while target not reached 
		&& CarbonEnoughCash && EnoughLand && EnoughCarbon) { // booleans set to false if corresponding exception thrown
			
			// HOW MUCH CASH DO WE HAVE
			double AvailableCash = this.getAvailableToSpend();
			if(debug) logger.info("behaviour: AvailableCash = " + AvailableCash);
			
			// WHAT REDUCTION DO WE NEED TO MEET TARGET
			double CarbonOutput = this.getCarbonOutput();
			if(debug) logger.info("behaviour: CarbonOutput = " + CarbonOutput);
			double TargetCarbonOutput = this.CalculateTargetCarbonOutput();
			if(debug) logger.info("behaviour: TargetCarbonOutput = " + TargetCarbonOutput);					
			RequiredCarbonOutputDecrease = TargetCarbonOutput-CarbonOutput;
			if(debug) logger.info("behaviour: RequiredCarbonOutputDecrease = " + RequiredCarbonOutputDecrease);
			
			// IF WE SPENT ALL MONEY, WHAT ABSORPTION WOULD BE POSSIBLE
			double MaxAbsorptionPossible = 0;
			try {
				MaxAbsorptionPossible = carbonAbsorptionHandler.getCarbonAbsorptionChange(AvailableCash)*0.9; // scaled down to avoid rounding issues
			} catch (NotEnoughLandException e3) {
				//EnoughLand = false;
				e3.printStackTrace();
			}
			if(debug) logger.info("behaviour: MaxAbsorptionPossible = " + MaxAbsorptionPossible);					
			
			// IF WE SPENT ALL MONEY, WHAT REDUCTION WOULD BE POSSIBLE
			double MaxReductionPossible = 0;
			MaxReductionPossible = carbonReductionHandler.getCarbonOutputChange(AvailableCash, this.getCarbonOutput(), this.getEnergyOutput())*0.9;
			if(debug) logger.info("behaviour: MaxReductionPossible = " + MaxReductionPossible);
			
			// COST OF ABSORPTION OF THE REQUIRED AMOUNT, OR THE MAX POSSIBLE
			try {
				AbsorptionInvestmentNeeded = carbonAbsorptionHandler.getInvestmentRequired(Math.min(MaxAbsorptionPossible, RequiredCarbonOutputDecrease));
			} catch (NotEnoughLandException e2) {
				//EnoughLand = false;
				e2.printStackTrace();
			}											
			if(debug) logger.info("behaviour: AbsorptionInvestmentNeeded = " + AbsorptionInvestmentNeeded);
			
			// COST OF ABSORPTION OF THE REQUIRED AMOUNT, OR THE MAX POSSIBLE
			ReductionInvestmentNeeded = carbonReductionHandler.getInvestmentRequired(Math.min(MaxReductionPossible, RequiredCarbonOutputDecrease));
			if(debug) logger.info("behaviour: ReductionInvestmentNeeded = " + ReductionInvestmentNeeded);
			
			// 
			if(AbsorptionInvestmentNeeded < ReductionInvestmentNeeded) {
				
				//InvestAmount = CheckInvestmentAmount(AbsorptionInvestmentNeeded) -> Should no longer need this, sorted out above.
				try {
					ReduceBy = carbonAbsorptionHandler.getCarbonAbsorptionChange(AbsorptionInvestmentNeeded);
				} catch (NotEnoughLandException e1) {
					// TODO Auto-generated catch block
					EnoughLand = false;
					e1.printStackTrace();
				}
				
				try {
					carbonAbsorptionHandler.investInCarbonAbsorption(ReduceBy);
				} catch (NotEnoughCashException e) {
					CarbonEnoughCash = false;
					e.printStackTrace();
				} catch (NotEnoughLandException LandException) {
					EnoughLand = false;
					LandException.printStackTrace();
				}
			}
			else {
				//InvestAmount = CheckInvestmentAmount(ReductionInvestmentNeeded)  -> Should no longer need this, sorted out above.;
				ReduceBy = carbonReductionHandler.getCarbonOutputChange(ReductionInvestmentNeeded, this.getCarbonOutput(), this.getEnergyOutput());
				
				try {
					carbonReductionHandler.investInCarbonReduction(ReduceBy);
				} catch (NotEnoughCarbonOutputException e) {
					EnoughCarbon = false;
					e.printStackTrace();
				} catch (NotEnoughCashException e) {
					CarbonEnoughCash = false;
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
=======
	private double getTradeFactorDifference() {
		int quarterLength = timeService.getTicksInYear() / 4;
		int quarter=1;
		int moddedTick = timeService.getCurrentTick() % timeService.getTicksInYear();
		if (moddedTick >= quarterLength && moddedTick < quarterLength*2)
			quarter = 2;
		else if (moddedTick >= quarterLength*2 && moddedTick < quarterLength*3)
			quarter = 3;
		else if (moddedTick >= quarterLength*3 && moddedTick < quarterLength*4)
			quarter = 4;
		
		switch (quarter) {
		case 4:
			return 0.05;
		case 3:
			return 0.1;
		case 2:
			return 0.15;
		case 1:
			return 0.2;
		}
		
		return 1;
	}
	
	/*
>>>>>>> ee451a1d79db28b380ecbc899fca92703b01e91d
>>>>>>> 93faa91c1781407245616f044e5df31f69b3cdd3
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
	*/

	private boolean IsLastTick() {
		logger.info("IsLastTick: Entering");
		if((timeService.getCurrentTick() % timeService.getTicksInYear())==timeService.getTicksInYear()-6) {
			logger.info("IsLastTick: Returning true");
			return(true);
		}
		else {
			logger.info("IsLastTick: Returning false");
			return(false);
		}
		
	}

	private double CalculateProjectedGDPRate() {
		logger.info("CalculateProjectedGDPRate: Entering");
		double ProjectedGDPRate;
		double marketStateFactor = GameConst.getStableMarketState();
		double sum;
		double EnergyOutput = getEnergyOutput();
		double PreviousEnergyOutput = getPrevEnergyOutput();
		double EnergyDifference = EnergyOutput-PreviousEnergyOutput;
		
		if (EnergyDifference >= 0){	
			sum = (((EnergyDifference)/PreviousEnergyOutput)*GameConst.getEnergyGrowthScaler()*marketStateFactor+getGDPRate()*100)/2;
			if (sum < 0) {
				ProjectedGDPRate = -(GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(sum*GameConst.getGrowthScaler()));
			}
			else {
				ProjectedGDPRate = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler());
			}
		}
		else{
			sum = ((EnergyDifference)/PreviousEnergyOutput)*GameConst.getEnergyGrowthScaler();
			sum = Math.abs(sum);
			ProjectedGDPRate = -(GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler()));
		}
		
		ProjectedGDPRate /= 100; // Needs to be a % for rate formula
		
		logger.info("CalculateProjectedGDPRate: ProjectedGDPRate = " + ProjectedGDPRate);
		logger.info("CalculateProjectedGDPRate: Returning");
		return ProjectedGDPRate;
	}
	
	double CalculateProjectedGDPRate2() {		
		double outputGDP = (CalculateProjectedGDPRate()/100) + 1;
		logger.info("CalculateProjectedGDPRate2: outputGDP = " + outputGDP);
		return(outputGDP);
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
		
		setPrevailingAttitude(ChangeAttitude());		
		StoreHistoricalData();			
		ProcessHistoricalData();
		
		if(IsElectionYear(timeService.getCurrentYear())) {
			HoldElection(); // will set DemocratElected to either true or false
			//SetTargets(); // emissions target
		}
		
		SetTargets(); // by running each year, governing party will need to fulfil cumulatively. 
		
		if (debug) logger.info("Recording carbon output of " + getCarbonOutput() + " on year " + timeService.getCurrentYear());
		
		emissionsTargetMap.put(timeService.getCurrentYear(), getCarbonOutput());
		
		//StoreTargetData();
		
		//ProcessTargetData();
		
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
		
		if (shouldLeave) {
			try {
				leaveKyoto();
				shouldLeave = false;
			} catch (CannotLeaveKyotoException e) {
				e.printStackTrace();
			}
		}
		
		if(debug) logger.info("yearlyFunction: Returning");
	}
	
	private boolean shouldLeave = true;
	
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

		int currentYear = timeService.getCurrentYear();
		int yearsInSession = timeService.getYearsInSession();
		if (currentYear >= yearsInSession && isKyotoMember() == KyotoMember.ROGUE) {
			double thisYearOutput = emissionsTargetMap.get(currentYear);
			double lastSessionOutput = emissionsTargetMap.get(currentYear - timeService.getYearsInSession());
			
			if (lastSessionOutput - thisYearOutput / lastSessionOutput > 0.05) {
				try {
					joinKyoto();
				} catch (CannotJoinKyotoException e) {
					e.printStackTrace();
				}
			}
		}
		else if (isKyotoMember() == KyotoMember.ANNEXONE && getCarbonOutput() - getCarbonOffset() - getCarbonAbsorption() > getEmissionsTarget() && getTimesCaughtCheating() > 0) {
			try {
				leaveKyoto();
			} catch (CannotLeaveKyotoException e) {
				shouldLeave = true;
			}
		}
	}
	
	@Override
	protected double getReportedCarbonOutput() {
		if (isKyotoMember() == KyotoMember.ANNEXONE && getCarbonOutput() > getEmissionsTarget() + getCarbonOffset() + getCarbonAbsorption() && timeService.getCurrentYear() == timeService.getYearsInSession() - 1) {
			double cheatedOutput = getEmissionsTarget() + getCarbonOffset() + getCarbonAbsorption();
			return cheatedOutput -= cheatedOutput*0.002;
		}
		else {
			return super.getReportedCarbonOutput();
		}
	}

	private void HoldElection() {		
		if(debug) logger.info("HoldElection: Entering");
		double IntensityScore;
		double GDPRateScore;
		
		if(isDemocratElected()) {
			IntensityScore = CalculateIntensityScore(0);
			GDPRateScore = CalculateGDPRateScore(Random.randomInt(5)/100); // increase the GDPRate score by a random amount
			
			if(IntensityScore < GDPRateScore) { 
				setDemocratElected(false);
				if(debug) logger.info("HoldElection: RepublicanElected");
			}
			else {
				setDemocratElected(true);
				if(debug) logger.info("HoldElection: Democrat Re-Elected");			
			}
		}
		else {
			IntensityScore = CalculateIntensityScore(Random.randomInt(5)/100);
			GDPRateScore = CalculateGDPRateScore(0); // increase the Intensity Score
			
			if(GDPRateScore < IntensityScore) { 
				setDemocratElected(true);
				if(debug) logger.info("HoldElection: Democrat Elected");
			}
			else {
				setDemocratElected(false);
				if(debug) logger.info("HoldElection: Republican Re-Elected");			
			}
		}
		
		if(debug) logger.info("HoldElection: Returning");
	}
	
	private double CalculateGDPRateScore(double ElectionAdjust) {
		double AverageGDPRate = this.getAverageGDPRate(timeService.getCurrentYear());
		double CurrentGDPRate = this.getGDPRate();

		double GDPRateScore = (AverageGDPRate/CurrentGDPRate);
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
		
		double AverageIntensityRatio = AverageIntensityRatioMap.get(timeService.getCurrentYear());
		double CurrentIntensityTarget = this.getIntensityTarget();
		// called at the beginning of the year so current intensity will contain the final values for the previous year,
		// on which we judge the election.
		double IntensityScore = -1 * ( (AverageIntensityRatio - CurrentIntensityTarget) / AverageIntensityRatio);
		// since target is lower than or the same will be positive if 
		if(debug) logger.info("CalculateIntensityScore: IntensityScore = " + IntensityScore);
		
		int 	CurrentAttitude = this.getPrevailingAttitude();
		double AttitudeFactor = 1+((-1*CurrentAttitude)/10);
		if(debug) logger.info("CalculateIntensityScore: AttitudeFactor = " + AttitudeFactor);
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
		if(debug) logger.info("CalculateIntensityScore: PartyFactor = " + PartyFactor);
		
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
			CalculateAndSetNewGDPTarget(1);
			//CalculateAndSetNewGDPRateTarget(1);
			CalculateAndSetNewIntensityTarget(USAgent.DecreaseIntensityByPercent);
		}
		else {
			if(debug) logger.info("SetTargets: Republican Elected");
			CalculateAndSetNewGDPTarget(USAgent.IncreaseGDPByPercent);
			CalculateAndSetNewIntensityTarget(1);	
		}
		
		setGDPRateTarget(CalculateGDPRateTarget()); // uses the just calculated GDPRateTargets
		
		if(debug) logger.info("SetTargets: Returning");
	}

	private void CalculateAndSetNewIntensityTarget(double Multiplier) {
		double AverageIntensity = AverageIntensityRatioMap.get(timeService.getCurrentYear());
		double value = AverageIntensity*Multiplier;
		
		
		if(debug) logger.info("CalculateAndSetNewIntensityTarget: value = " + value);
		setIntensityTarget(value);
	}
	
	/*
	private void CalculateAndSetNewGDPRateTarget(double Multiplier) {
		double value = AverageGDPRateMap.get(timeService.getCurrentYear())*Multiplier;
		if(debug) logger.info("CalculateAndSetNewGDPRateTarget: value = " + value);
		setGDPRateTarget(value);
	}
	*/
	
	private void CalculateAndSetNewGDPTarget(double Multiplier) {
		double value = AverageGDPMap.get(timeService.getCurrentYear())*Multiplier;
		if(debug) logger.info("CalculateAndSetNewGDPTarget: value = " + value);
		setGDPTarget(value);
	}

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ic.kyoto.countries.AbstractCountry#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 * Called by execute when input objects are waiting on your agent. 
	 */
	protected void processInput(uk.ac.imperial.presage2.core.messaging.Input in) {
		if(debug) logger.info("processInput: Entering");
		if(debug) logger.info("processInput: " + in.toString());
		if(this.tradeProtocol!=null) {
			if (this.tradeProtocol.canHandle(in)) {
				this.tradeProtocol.handle(in);
			}
			else{
				OfferMessage offerMessage = this.tradeProtocol.decodeInput(in);
				if(AnalyzeOffer(offerMessage)) {	
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
		}
		else {
			if(debug) logger.info("processInput: tradeProtocol==null");
		}
		if(debug) logger.info("processInput: Returning");
	};
	
	protected boolean AnalyzeOffer(OfferMessage offerMessage) {
		if(debug) logger.info("AnalyzeOffer: Entering");
		if(offerMessage.getOfferType()==TradeType.RECEIVE) { // CDM type
			// Democrats will opt for reduction if it is cost effective regardless of whether target has already been met.
			if(debug) logger.info("AnalyzeOffer: TradeType==RECEIVE");
			
			
			if(CalculateCurrentIntensityRatio() > CalculateProjectedIntensityRatio()) {
				if(debug) logger.info("AnalyzeOffer: getIntensityRatio() > getIntensityTarget()");
				double OfferUnitCost = offerMessage.getOfferUnitCost();				
				double OfferQuantity = offerMessage.getOfferQuantity();
				double TradeCost = OfferUnitCost*OfferQuantity;
				double EquivalentAbsorptionCost = 0;
				
				try {
					EquivalentAbsorptionCost = carbonAbsorptionHandler.getInvestmentRequired(OfferQuantity);
				} catch (NotEnoughLandException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				double EquivalentReductionCost = carbonReductionHandler.getInvestmentRequired(OfferQuantity);
				
				if(debug) logger.info("AnalyzeOffer: OfferUnitCost = " + OfferUnitCost);
				if(debug) logger.info("AnalyzeOffer: OfferQuantity = " + OfferQuantity);
				if(debug) logger.info("AnalyzeOffer: TradeCost = " + TradeCost);
				if(debug) logger.info("AnalyzeOffer: EquivalentAbsorptionCost = " + EquivalentAbsorptionCost);
				if(debug) logger.info("AnalyzeOffer: EquivalentReductionCost = " + EquivalentReductionCost);
				
				if(TradeCost < Math.min(EquivalentReductionCost, EquivalentAbsorptionCost)) {					
					if(debug) logger.info("AnalyzeOffer: Returning true");
					return(true);
				}
			}						
		}
		else if (isKyotoMember() == KyotoMember.ANNEXONE && CalculateCurrentIntensityRatio() > CalculateProjectedIntensityRatio()) {
			double offerUnitCost = offerMessage.getOfferUnitCost();
			double offerUnits = offerMessage.getOfferQuantity();
			double offerTotalCost = offerUnitCost * offerUnits;
			if (getCarbonOutput() - getCarbonAbsorption() - getCarbonOffset() > getEmissionsTarget() && offerMessage.getOfferType() == TradeType.SELL) {
				double absorptionCost;
				try {
					absorptionCost = carbonAbsorptionHandler.getInvestmentRequired(offerUnits);
				} catch (NotEnoughLandException e) {
					absorptionCost = Double.MAX_VALUE;
				}
				if (offerTotalCost < carbonReductionHandler.getInvestmentRequired(offerUnits) && offerTotalCost < absorptionCost) {
					return true;
				}
			}
			else if (getCarbonOutput() - getCarbonAbsorption() - getCarbonOffset() < getEmissionsTarget() && offerMessage.getOfferType() == TradeType.BUY) {
				double absorptionCost;
				try {
					absorptionCost = carbonAbsorptionHandler.getInvestmentRequired(offerUnits, 300918);
					// This arable land value is the average of all countries' arable land, so a good base comparison
				}
				catch (NotEnoughLandException e) {
					return false;
				}
				if (offerTotalCost < absorptionCost && offerTotalCost < carbonReductionHandler.getInvestmentRequired(offerUnits)) {
					return true;
				}
			}
			else if (offerTotalCost > carbonReductionHandler.getInvestmentRequired(offerUnits) && offerMessage.getOfferType() == TradeType.BUY) {
				try {
					carbonReductionHandler.investInCarbonReduction(offerUnits);
				} catch (NotEnoughCarbonOutputException e) {
					return false;
				} catch (NotEnoughCashException e) {
					return false;
				}
				return true;
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
		setKyotoMemberLevel(KyotoMember.ROGUE);
		//initialise();
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
		
		// this function removes agent from the monitor service and sets
		// kyotoMemberLevel = KyotoMember.ROGUE;
		SetInitialAttitude();
		SetInitialPoliticalParty(); // assigned at random to start
		//StoreHistoricalData();
		//ProcessHistoricalData();
		//SetTargets();
		
		
		if(debug) logger.info("initialiseCountry: Returning");
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
				NewAttitude = Math.max(-5, CurrentAttitude + Random.randomInt(2)-2);	
			}
		}
		
		return(NewAttitude);
	}
	
	private int SetInitialAttitude() {
		// TODO: ask about this
		int value = Random.randomInt(10) - 5;
		if(debug) logger.info("SetInitialAttitude: value = " + value);
		return(value);
	}
	
	/*
	private double CalculateGDPTarget() {
		// TODO check
		double value = getGDP()*(getGDPRateTarget());
		if(debug) logger.info("CalculateGDPTarget: value = " + value);
		return value; 
	}
	*/
	/*
	private void StoreTargetData() {
		if(debug) logger.info("StoreTargetData: Entered");
		
		IntensityRatioMap.put(timeService.getCurrentYear(), getIntensityRatio());
		if(debug) logger.info("StoreTargetData: IntensityRatioMap[timeService.getCurrentYear()] = " + IntensityRatioMap.get(timeService.getCurrentYear()));
		
		GDPRateTargetMap.put(timeService.getCurrentYear(), getGDPRateTarget());
		if(debug) logger.info("StoreTargetData: GDPRateTargetMap[timeService.getCurrentYear()] = " + GDPRateTargetMap.get(timeService.getCurrentYear()));
		
		setGDPTarget(CalculateGDPTarget());
		
		if(debug) logger.info("StoreTargetData: Returning");
	}
	*/

	private void StoreHistoricalData() {
		if(debug) logger.info("StoreHistoricalData: Entering");
		
		double CarbonOutput = getCarbonOutput();
		carbonOutputMap.put(timeService.getCurrentYear(), CarbonOutput);	
		
		//emissionsTargetMap.put(timeService.getCurrentYear(), getEmissionsTarget());	
		double dGDPRate = 1+getGDPRate();
		GDPRateMap.put(timeService.getCurrentYear(), dGDPRate);
		
		double dGDP = getGDP();
		GDPMap.put(timeService.getCurrentYear(), dGDP);

		if(debug) logger.info("StoreTargetData: IntensityRatioMap[timeService.getCurrentYear()] = " + IntensityRatioMap.get(timeService.getCurrentYear()));
		
		//GDPRateTargetMap.put(timeService.getCurrentYear(), getGDPRateTarget());
		//if(debug) logger.info("StoreTargetData: GDPRateTargetMap[timeService.getCurrentYear()] = " + GDPRateTargetMap.get(timeService.getCurrentYear()));
		
		double dIntensityRatio = getIntensityRatio();
		IntensityRatioMap.put(timeService.getCurrentYear(), dIntensityRatio);		
		
		if(debug) logger.info("StoreHistoricalData: Returning");
	}
	
	private void ProcessHistoricalData() {
		double dGDPRate = 0;
		double dIntensityRatio = 0;		
		double dGDP = 0;
		int 	Limit;
		int 	Divider = 0;
		if(debug) logger.info("ProcessHistoricalData: Entered");
		if(debug) logger.info("ProcessHistoricalData: timeService.getCurrentYear() = " + timeService.getCurrentYear());
		if(debug) logger.info("ProcessHistoricalData: Average1 = " + Average1);
		
		if(timeService.getCurrentYear() > Average1-1) { // first calculated on election year
			if(debug) logger.info("ProcessHistoricalData: timeService.getCurrentYear() >= Average1");
			Limit = Average1-1;			
		}
		else {
			if(debug) logger.info("ProcessHistoricalData: timeService.getCurrentYear() < Average1");
			Limit = timeService.getCurrentYear();
		}
		
		for(int i=timeService.getCurrentYear(); i >= timeService.getCurrentYear() - Limit; i--) {
			dGDP += (GDPMap.get(i));
			dGDPRate += (GDPRateMap.get(i));
			dIntensityRatio += IntensityRatioMap.get(i);
			Divider++;
		}
		
		if(debug) logger.info("ProcessHistoricalData: Divider = " + Divider);
		
		AverageGDPMap.put(timeService.getCurrentYear(), dGDP/Divider);
		if(debug) logger.info("ProcessHistoricalData: AverageGDPMap.get(timeService.getCurrentYear()) = " 
		+ AverageGDPMap.get(timeService.getCurrentYear()));
		
		AverageGDPRateMap.put(timeService.getCurrentYear(), dGDPRate/Divider);
		if(debug) logger.info("ProcessHistoricalData: AverageGDPRateMap.get(timeService.getCurrentYear()) = " 
		+ AverageGDPRateMap.get(timeService.getCurrentYear()));
		
		AverageIntensityRatioMap.put(timeService.getCurrentYear(), dIntensityRatio/Divider);
		if(debug) logger.info("ProcessHistoricalData: AverageIntensityRatioMap[timeService.getCurrentYear()] = " 
		+ AverageIntensityRatioMap.get(timeService.getCurrentYear()));
		
		
		if(debug) logger.info("ProcessHistoricalData: Returning");
	}
	
	/*
	private void ProcessTargetData() {
		double dIntensityTarget = 0;
		double dGDPRateTarget = 0;
		int 	Limit;
		int 	Divider = 0;
		
		if(debug) logger.info("ProcessTargetData: Entered");
		
		if(timeService.getCurrentYear() >= Average1) { // first calculated on election year
			if(debug) logger.info("ProcessTargetData: timeService.getCurrentYear() >= Average1");
			Limit = Average1;			
		}
		else {
			if(debug) logger.info("ProcessTargetData: timeService.getCurrentYear() < Average1");
			Limit = timeService.getCurrentYear();
		}	
		
		for(int i=timeService.getCurrentYear(); i >= timeService.getCurrentYear() - Limit; i--) {
			//dIntensityTarget += IntensityTargetMap.get(timeService.getCurrentYear());				
			dGDPRateTarget += GDPRateTargetMap.get(timeService.getCurrentYear());
			Divider++;
		}
		
		//AverageIntensityTargetMap.put(timeService.getCurrentYear(),dIntensityTarget/Divider);
		//if(debug) logger.info("ProcessTargetData: AverageIntensityTargetMap[timeService.getCurrentYear()] = " 
		//+ AverageIntensityTargetMap.get(timeService.getCurrentYear()));
		
		AverageGDPRateTargetMap.put(timeService.getCurrentYear(), dGDPRateTarget/Divider);
		if(debug) logger.info("ProcessTargetData: AverageGDPRateTargetMap[timeService.getCurrentYear()] = " 
		+ AverageGDPRateTargetMap.get(timeService.getCurrentYear()));
		
		if(debug) logger.info("ProcessTargetData: Returning");
	}
	*/
	
	boolean LeavingCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = timeService.getCurrentYear()-1; i > timeService.getCurrentYear() - KyotoExitYears; i--) {
			if (carbonOutputMap.get(i) <= emissionsTargetMap.get(i)) {
				return(false);
			}
		}
		return(true); // 
	}
	
	boolean JoiningCriteriaMet() {
		// -1 since function is called on first tick of new year, want to evaluate
		// over the previous X years.
		for(int i = timeService.getCurrentYear()-1; i > timeService.getCurrentYear() - KyotoEntryYears; i--) {
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
		if (Year % 4 == 0 && Year!=0) {
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
		IntensityTarget = intensityTarget;
		if(debug) logger.info("setIntensityTarget: IntensityTarget = " + IntensityTarget);
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
		DemocratElected = democratElected;
		if(debug) logger.info("setDemocratElected: DemocratElected = " + DemocratElected);
	}	
	
	public double getIntensityRatio() {
		double value = CalculateIntensityRatio();
		if(debug) logger.info("getIntensityRatio: IntensityRatio = " + value);
		return value;
	}

	private double CalculateProjectedIntensityRatio() {
		double result = this.getGDPTarget() / CalculateTargetCarbonOutput();
		if(debug) logger.info("CalculateProjectedIntensityRatio: result = " + result);
		return(result);
	}
	
	private double CalculateTargetCarbonOutput() {
		double result = getGDPTarget() / getIntensityTarget();
		if(debug) logger.info("CalculateTargetCarbonOutput: result = " + result);
		return result;		
	}
	
	private double CalculateCurrentIntensityRatio() {
		double result = CalculateProjectedGDP() / getCarbonOutput();
		if(debug) logger.info("CalculateCurrentIntensityRatio: result = " + result);
		return(result);
	}
	
	private double CalculateProjectedGDP() {
		double result = this.CalculateProjectedGDPRate2()*this.getGDP();
		if(debug) logger.info("CalculateProjectedGDP: result = " + result);
		return(result);
	}
	
	private double CalculateGDPRateTarget() {
		double result = this.getGDPTarget()/this.getGDP();
		if(debug) logger.info("CalculateGDPRateTarget: result = " + result);
		return result;
	}
	
	public double getGDPRateTarget() {
		if(debug) logger.info("getGDPRateTarget: GDPRateTarget = " + GDPRateTarget);
		return GDPRateTarget;
	}
	public void setGDPRateTarget(double gDPRateTarget) {		
		GDPRateTarget = gDPRateTarget;
		if(debug) logger.info("setGDPRateTarget: GDPRateTarget = " + GDPRateTarget);
	}

	public double getGDPTarget() {
		if(debug) logger.info("getGDPTarget: GDPTarget = " + GDPTarget);
		return GDPTarget;
	}

	public void setGDPTarget(double gDPTarget) {		
		GDPTarget = gDPTarget;
		if(debug) logger.info("setGDPTarget: GDPTarget = " + GDPTarget);
	}

	public int getPrevailingAttitude() {
		if(debug) logger.info("getPrevailingAttitude: PrevailingAttitude = " + PrevailingAttitude);
		return PrevailingAttitude;
	}

	public void setPrevailingAttitude(int prevailingAttitude) {		
		PrevailingAttitude = prevailingAttitude;
		if(debug) logger.info("setPrevailingAttitude: PrevailingAttitude = " + PrevailingAttitude);
	}
}
