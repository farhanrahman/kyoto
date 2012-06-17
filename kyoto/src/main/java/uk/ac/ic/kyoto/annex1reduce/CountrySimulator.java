package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.ic.kyoto.countries.GameConst;

/**
 * 
 * @author Nik
 *
 */
class CountrySimulator {

	//TODO, look ahead a minimum of 5 years or a maximum of 15 years. Always end at a session end.
	final private static int LOOK_AHEAD_YEARS = 15;

	public CountrySimulator(AnnexOneReduce country) {
		this.country = country;
	}

	final private AnnexOneReduce country;

	private StateList[] stateList = new StateList[LOOK_AHEAD_YEARS];

	private CountryState startState;

	public CountryState simulate(double carbonOutput, double energyOutput, double carbonOffset,double carbonAbsorption, double emissionsTarget, double availableToSpend,
			double GDP, double GDPRate, double arableLandArea) {

		//Initialise the starting point in the simulation
		startState = new CountryState(carbonOutput,energyOutput,
				carbonOffset,carbonAbsorption,emissionsTarget,availableToSpend,
				GDP,GDPRate,arableLandArea);

		//Reset old simulation data
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			stateList[i] = new StateList();
		}

		//Fill up the first array in the table
		startState.reduceCarbon();

		//For all years to look ahead
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			//Cull the reduce states

			System.out.println("unculledReduceSize " + i + " = " + stateList[i].reduceStates.size());
			stateList[i].reduceStates = cullStates(stateList[i].reduceStates);
			System.out.println("reduceSize " + i + " = " + stateList[i].reduceStates.size());

			//Branch off all unculled reduce states by performing a maintain action		
			Iterator<CountryState> redIt = stateList[i].reduceStates.iterator();
			while(redIt.hasNext()) {
				redIt.next().maintainCarbon();
			}

			//Cull the maintain states
			System.out.println("unculledMaintainSize " + i + " = " + stateList[i].maintainStates.size());
			stateList[i].maintainStates = cullStates(stateList[i].maintainStates);
			System.out.println("maintainSize " + i + " = " + stateList[i].maintainStates.size());

			//Branch off all unculled maintain states by performing a sell action
			Iterator<CountryState> mainIt = stateList[i].maintainStates.iterator();
			while(mainIt.hasNext()) {
				mainIt.next().sellCarbon();
			}

			//Cull the sell states
			System.out.println("unculledSellSize " + i + " = " + stateList[i].sellStates.size());
			stateList[i].sellStates = cullStates(stateList[i].sellStates);
			System.out.println("sellSize " + i + " = " + stateList[i].sellStates.size());


			//So long as we aren't in the final year
			if (i != (LOOK_AHEAD_YEARS - 1)) {
				//Branch off all unculled reduce states by performing a reduce action
				Iterator<CountryState> sellIt = stateList[i].sellStates.iterator();
				while(sellIt.hasNext()) {
					sellIt.next().reduceCarbon();
				}
			}
		}

		CountryState optimalState = getOptimalState(stateList[LOOK_AHEAD_YEARS-1].sellStates);

		return optimalState;

	}

	/**
	 * Look at the given array of countryStates and return the best one
	 * @return
	 */
	private CountryState getOptimalState(ArrayList<CountryState> states) {
		return null;
	}

	/**
	 * Goes through all states and will cull all but the most useful
	 * @param states
	 * @return The culled list of input states
	 */
	private ArrayList<CountryState> cullStates(ArrayList<CountryState> states) {

		//Firstly, remove all states with invalid attributes

		Iterator<CountryState> iterator = states.iterator();

		while(iterator.hasNext()) {
			CountryState state = iterator.next();
			if (state.availableToSpend < 0) {
				iterator.remove();
			}
			else if (state.carbonOutput < 0) {
				iterator.remove();
			}
		}

		boolean hasBeenReplaced = true;

		while (hasBeenReplaced) {
			hasBeenReplaced = false;

			ArrayList<CountryState> bestList = new ArrayList<CountryState>(states.size());

			bestList.add(states.get(0));

			for(int i = 1; i<states.size(); i++) {
				CountryState testState = states.get(i);

				boolean needsAdding = true;

				for (int j = 0; j<bestList.size(); j++) {
					CountryState bestState = bestList.get(j);
					Compare comparison = testState.action.isBetterOrWorseThan(testState,bestState);
					if (comparison == Compare.WORSE) {
						needsAdding = false;
						break;
					}
					else if (comparison == Compare.BETTER) {
						bestList.set(j, testState);
						needsAdding = false;
						hasBeenReplaced = true;
						break;
					}
				}
				if (needsAdding) {
					bestList.add(testState);
				}
			}
			states = bestList;
		}	
		return states;

	}

	private static enum Compare {
		BETTER,WORSE,UNKNOWN
	}

	/**
	 * Structure holding country attributes after performing a certain action,
	 * and also containing the chain of events leading to that action
	 * @author Nik
	 *
	 */
	class CountryState {

		final public double carbonOutput;
		final public double energyOutput;
		final public double carbonOffset;
		final public double carbonAbsorption;

		final public double emissionsTarget;

		final public double availableToSpend;
		final public double GDP;
		final public double GDPRate;
		final public double arableLandArea;

		final public CountryState previousState;
		final public int year;

		final public Action action;
		
		final public double carbonDiff;

		private CountryState(final double carbonOutput, final double energyOutput,
				final double carbonOffset,final double carbonAbsorption, final double emissionsTarget,
				final double availableToSpend, final double GDP, final double GDPRate,
				final double arableLandArea) {

			this.carbonOutput = carbonOutput;
			this.energyOutput = energyOutput;
			this.carbonOffset = carbonOffset;
			this.carbonAbsorption = carbonAbsorption;
			this.emissionsTarget = emissionsTarget;

			this.availableToSpend = availableToSpend;
			this.GDP = GDP;
			this.GDPRate = GDPRate;
			this.arableLandArea = arableLandArea;

			this.previousState = null;

			this.year = -1;

			this.action = null;
			
			carbonDiff = carbonOutput - carbonAbsorption;
		}
		
		/**
		 * 
		 * @param previousState The state that performs the action
		 * @param action The action performed by the previous state
		 */
		private CountryState(CountryState previousState,ReduceAction action) {
			this.previousState = previousState;
			this.action = action;
			this.year = previousState.year + 1;
			this.emissionsTarget = previousState.emissionsTarget;
			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;
			stateList[this.year].addReduce(this);

			double oldCarbonDifference = previousState.getCarbonDifference();

			//Price of reaching our target by investment and buying credits
			double[] investments = new double[2];
			double carbonAbsorbedReduced = action.investFrac * oldCarbonDifference;
			double investmentCost = country.getAbsorbReduceInvestment(carbonAbsorbedReduced, previousState, investments);
			double carbonOffsetIncreased = oldCarbonDifference * action.buyCreditFrac;
			double marketCost = country.getMarketBuyPrice(carbonOffsetIncreased, this.year);
			this.availableToSpend = previousState.availableToSpend - investmentCost - marketCost;

			//Amount of arable land lost
			double arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);
			this.arableLandArea = previousState.arableLandArea - arableLandCost;

			//Amount of carbon absorption we've increased by
			double absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);
			this.carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;

			//Amount of energy and carbon we've lost by shutting down factories
			double energyReduction = action.shutDownFrac * oldCarbonDifference;
			this.energyOutput = previousState.energyOutput - energyReduction;

			//Our carbon output decreased by the amount we invest in reduction plus the amount of factories we shut down
			double carbonOutputReduced = country.getCarbonReduction(investments[1],previousState);
			this.carbonOutput = previousState.carbonOutput - carbonOutputReduced - energyReduction;

			//Our carbon offset increased by the amount of carbon we bought in the market
			this.carbonOffset = previousState.carbonOffset - carbonOffsetIncreased;
			
			carbonDiff = carbonOutput - carbonAbsorption;
		}

		/**
		 * Constructor for a maintain state
		 * @param previousState
		 * @param action
		 */
		private CountryState(CountryState previousState,MaintainAction action) {
			this.previousState = previousState;
			this.action = action;
			this.year = previousState.year;
			this.emissionsTarget = previousState.emissionsTarget;
			stateList[this.year].addMaintain(this);

			//energy Increase = carbon Increase when investing in factories
			double energyCost = action.industryFrac * previousState.availableToSpend;
			double energyIncrease = country.getCarbonEnergyIncrease(energyCost);

			//Number of carbon credits gained by buying credits
			double carbonCreditIncrease = action.buyCreditOffsetFrac * energyIncrease;
			double marketBuyCost = country.getMarketBuyPrice(carbonCreditIncrease, this.year);

			//Cost of investing in absorption and reduction
			double CarbonInvestOffset = action.investOffsetFrac * energyIncrease;
			double[] investments = new double[2];
			double investmentCost = country.getAbsorbReduceInvestment(CarbonInvestOffset, previousState, investments);
			double arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);
			double absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);
			double carbonOutputReduced = country.getCarbonReduction(investments[1],previousState);


			energyOutput = previousState.energyOutput + energyIncrease;
			carbonOutput = previousState.carbonOutput + energyIncrease - carbonOutputReduced;

			carbonOffset = previousState.carbonOffset + carbonCreditIncrease;

			arableLandArea = previousState.arableLandArea - arableLandCost;

			carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;

			//TODO ensure the formula is correct
			double tempGDPRate = previousState.GDPRate + GameConst.getGrowthScaler()*(energyOutput)/previousState.GDP;
			GDPRate = tempGDPRate/100;

			GDP = previousState.GDP + previousState.GDP*GDPRate;

			availableToSpend = previousState.availableToSpend - investmentCost - marketBuyCost + (GDP * GameConst.getPercentageOfGdp());
			
			carbonDiff = carbonOutput - carbonAbsorption;
		}

		private CountryState(CountryState previousState,SellAction action) {
			this.previousState = previousState;
			this.action = action;
			this.year = previousState.year;
			this.emissionsTarget = country.getNextEmissionTarget(previousState.emissionsTarget);
			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;
			stateList[this.year].addSell(this);

			//Our current total carbon output.
			double totalCarbonOutput = previousState.carbonOutput - previousState.carbonOffset - previousState.carbonAbsorption;

			//Shut down factories
			double shutDownCarbon = totalCarbonOutput*action.shutDownFrac;

			//Invest in being clean
			double carbonAbsorbedReduced = totalCarbonOutput*action.investFrac;
			double[] investments = new double[2];
			double investmentCost = country.getAbsorbReduceInvestment(carbonAbsorbedReduced, previousState, investments);
			double absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);
			double arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);
			double carbonOutputReduced = country.getCarbonReduction(investments[1],previousState);

			//Sell additional offset over next years target
			double newCarbonOutput = totalCarbonOutput - shutDownCarbon - carbonAbsorbedReduced;
			double carbonBelowTarget = emissionsTarget - newCarbonOutput;
			double totalCreditsSold = carbonBelowTarget*action.sellFrac;

			double marketSellGain = country.getMarketSellPrice(totalCreditsSold, this.year);

			this.availableToSpend = previousState.availableToSpend - investmentCost + marketSellGain;
			this.arableLandArea = previousState.arableLandArea - arableLandCost;
			this.carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;
			this.energyOutput = previousState.energyOutput - shutDownCarbon;
			this.carbonOutput = previousState.carbonOutput - shutDownCarbon - carbonOutputReduced;
			this.carbonOffset = previousState.carbonOffset - totalCreditsSold;
			
			carbonDiff = this.carbonOutput - carbonAbsorption;
		}

		/**
		 * Branch off for all reduce actions
		 */
		private void reduceCarbon() {
			float shutDown;
			float buyCredit;
			float invest;

			//If we actually need to reduce our carbon
			if (this.getCarbonDifference() > 0) {
				
				for (int i=0; i<=100; i=i+50) {
					shutDown = i;

					for (int j=0; j<=100-shutDown; j=j+50) {
						buyCredit = j;

						invest = 100 - shutDown - buyCredit;
						new CountryState(this, new ReduceAction(shutDown/100, buyCredit/100, invest/100));
					}
				}
			}
			else {
				new CountryState(this, new ReduceAction(0,0,0));
			}
		}

		/**
		 * Branch off for all maintain actions
		 */
		private void maintainCarbon() {

			new CountryState(this, new MaintainAction(0, 0, 0));

			//Invest some chunk of our available cash in industry, offset it by buying credits or absorbing

			float industryFrac;
			float investOffsetFrac;
			float buyCreditOffsetFrac;

			//If last action we didn't buy carbon, 
			if (((ReduceAction) action).buyCreditFrac == 0) {
				for (int i=25; i<=100; i=i+25) {
					industryFrac = i;
					new CountryState(this, new MaintainAction(industryFrac/100, 0, 1));
				}
			}
			else {
				for (int i=25; i<=100; i=i+25) {
					industryFrac = i;

					for (int j=0; j<=100; j=j+25) {
						investOffsetFrac = j;
						buyCreditOffsetFrac = 100-investOffsetFrac;
						new CountryState(this, new MaintainAction(industryFrac/100, investOffsetFrac/100, buyCreditOffsetFrac/100));
					}
				}
			}
		}

		/**
		 * Branch off for all sell actions
		 */
		private void sellCarbon() {

			float shutDownFrac;
			float investFrac;
			float sellFrac;
			
			//If we've bough any credits, selling them is pointless
			if (previousState.carbonOffset > 0) {
				new CountryState(this, new SellAction(0,0,0));
			}
			else {
				for (int k = 0; k<=100; k=k+50) {
					sellFrac = k;
					new CountryState(this, new SellAction(0,0,sellFrac/100));
				}
	
				for (int i = 2; i<=6; i=i+2) {
					shutDownFrac = i;
					for (int k = 0; k<=100; k=k+50) {
						sellFrac = k;
						new CountryState(this, new SellAction(shutDownFrac/100,0, sellFrac/100));
					}
				}
				for (int i = 2; i<=6; i=i+2) {
					investFrac = i;
					for (int k = 0; k<=100; k=k+50) {
						sellFrac = k;
						new CountryState(this, new SellAction(0,investFrac/100, sellFrac/100));
					}
				}
			}
		}

		/**
		 * @return a slight overestimate of the carbon we need to offset. Positive means we need to reduce to meet our target
		 */
		private double getCarbonDifference() {
			return 1.02*(this.carbonOffset + this.carbonOutput + this.carbonAbsorption - this.emissionsTarget);
		}

	}

	/**
	 * Structure containing a list of states performed in a given year,
	 * after performing each action phase (reduce and maintain)
	 * @author Nik
	 *
	 */
	private class StateList {
		private ArrayList<CountryState> reduceStates = new ArrayList<CountryState>();
		private ArrayList<CountryState> maintainStates = new ArrayList<CountryState>();
		private ArrayList<CountryState> sellStates = new ArrayList<CountryState>();

		public void addReduce(CountryState countryState) {
			reduceStates.add(countryState);
		}
		public void addSell(CountryState countryState) {
			sellStates.add(countryState);

		}
		public void addMaintain(CountryState countryState) {
			maintainStates.add(countryState);			
		}	
	}

	/**
	 * A reduce action.
	 * @author Nik
	 *
	 */
	private static class ReduceAction extends Action {	

		public ReduceAction(float shutDown, float buyCredit, float invest) {
			this.shutDownFrac = shutDown;
			this.buyCreditFrac = buyCredit;
			this.investFrac = invest;
		}

		final float shutDownFrac;
		final float buyCreditFrac;
		final float investFrac;
		
		/**
		 * Compare two states that have just had a reduce action applied
		 */
		@Override
		Compare isBetterOrWorseThan(CountryState state1, CountryState state2) {
			
			boolean isBetter = true;
			boolean isWorse = true;

			//If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			}
			else if (state1.GDP < state2.GDP){
				isBetter = false;
			}
			
			//If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			}
			else if (state1.availableToSpend < state2.availableToSpend){
				isBetter = false;
			}
			
			//If we have less carbon output
			if (state1.carbonDiff < state2.carbonDiff) {
				isWorse = false;
			}
			else if (state1.carbonDiff > state2.carbonDiff){
				isBetter = false;
			}
			
			//If we make more energy
			if (state1.energyOutput > state2.energyOutput) {
				isWorse = false;
			}
			else if (state1.energyOutput < state2.energyOutput){
				isBetter = false;
			}
			
			if (isBetter) {
				return Compare.BETTER;
			}
			if (isWorse) {
				return Compare.WORSE;
			}
			return Compare.UNKNOWN;
		}
	}

	/**
	 * A maintain action
	 * @author Nik
	 *
	 */
	private static class MaintainAction extends Action {
		public MaintainAction(float industryFrac, float investOffsetFrac, float buyCreditOffsetFrac) {
			this.industryFrac = industryFrac;
			this.investOffsetFrac = investOffsetFrac;
			this.buyCreditOffsetFrac = buyCreditOffsetFrac;
		}
		/**
		 * Fraction of gdp spent on investing on factories.
		 */
		final float industryFrac;

		/**
		 * Fraction of carbon created by increasing industry to offset by absorption etc.
		 */
		final float investOffsetFrac;

		/**
		 * Fraction of carbon created by increasing industry to offset by buying credits etc.
		 */
		final float buyCreditOffsetFrac;

		/**
		 * Compare two states that have just had a maintain action applied
		 */
		@Override
		Compare isBetterOrWorseThan(CountryState state1,
				CountryState state2) {
			
			boolean isBetter = true;
			boolean isWorse = true;

			//If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			}
			else if (state1.GDP < state2.GDP){
				isBetter = false;
			}
			
			//If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			}
			else if (state1.availableToSpend < state2.availableToSpend){
				isBetter = false;
			}
			
			//If we have less carbon output
			if (state1.carbonDiff < state2.carbonDiff) {
				isWorse = false;
			}
			else if (state1.carbonDiff > state2.carbonDiff){
				isBetter = false;
			}
			
			//If we have a higher GDPRate
			if (state1.GDPRate > state2.GDPRate) {
				isWorse = false;
			}
			else if (state1.GDPRate < state2.GDPRate){
				isBetter = false;
			}
			
			if (isBetter) {
				return Compare.BETTER;
			}
			if (isWorse) {
				return Compare.WORSE;
			}
			return Compare.UNKNOWN;
		}
	}

	private static class SellAction extends Action {

		public SellAction(float shutDownFrac, float investFrac, float sellFrac) {
			this.shutDownFrac = shutDownFrac;
			this.investFrac = investFrac;
			this.sellFrac = sellFrac;
		}

		/**
		 * Fraction of carbon to reduce by shutting down factories.
		 * Impossible if you perform any actions in phase one
		 */
		final float shutDownFrac;

		/**
		 * Fraction of remaining carbon to reduce by investing in absorption etc.
		 */
		final float investFrac;

		/**
		 * Fraction of our current carbon offset to sell on the market next year.
		 * Calculated using next years carbon target.
		 */
		final float sellFrac;

		/**
		 * Compare two states that have just had a sell action applied
		 */
		@Override
		Compare isBetterOrWorseThan(CountryState state1,
				CountryState state2) {
			
			boolean isBetter = true;
			boolean isWorse = true;

			//If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			}
			else if (state1.GDP < state2.GDP){
				isBetter = false;
			}
			
			//If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			}
			else if (state1.availableToSpend < state2.availableToSpend){
				isBetter = false;
			}
			
			//If we have less carbon output
			if (state1.carbonDiff < state2.carbonDiff) {
				isWorse = false;
			}
			else if (state1.carbonDiff > state2.carbonDiff){
				isBetter = false;
			}
			
			//If we make more energy
			if (state1.energyOutput > state2.energyOutput) {
				isWorse = false;
			}
			else if (state1.energyOutput < state2.energyOutput){
				isBetter = false;
			}
			
			if (isBetter) {
				return Compare.BETTER;
			}
			if (isWorse) {
				return Compare.WORSE;
			}
			return Compare.UNKNOWN;
		}
	}

	/**
	 * Structure containing information on action we've taken.
	 * When passed into a CountryState constructor, action will be performed
	 * @author Nik
	 *
	 */
	private static abstract class Action {
		abstract Compare isBetterOrWorseThan(CountryState state1, CountryState state2);
	}

}
