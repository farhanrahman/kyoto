package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;
import java.util.HashSet;
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
		//		
		//		double[] investments = new double[2];
		//		double totalcost = country.getAbsorbReduceInvestment(1000,startState,investments);
		//		
		//		System.out.println(investments[0]);
		//		System.out.println(investments[1]);

		//Reset old simulation data
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			stateList[i] = new StateList();
		}

		//Fill up the first array in the table
		startState.reduceCarbon();

		System.out.println("initialSize = " + stateList[0].reduceStates.size());

		//For all years to look ahead
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			//Cull the reduce states
			stateList[i].reduceStates = cullStates(stateList[i].reduceStates);
			System.out.println("reduceSize " + i + " = " + stateList[i].reduceStates.size());

			//Branch off all unculled reduce states by performing a maintain action		
			Iterator<CountryState> redIt = stateList[i].reduceStates.iterator();
			while(redIt.hasNext()) {
				redIt.next().maintainCarbon();
			}

			//Cull the maintain states
			stateList[i].maintainStates = cullStates(stateList[i].maintainStates);

			System.out.println("maintainSize " + i + " = " + stateList[i].maintainStates.size());

			//So long as we aren't in the final year
			if (i != (LOOK_AHEAD_YEARS - 1)) {
				//Branch off all unculled reduce states by performing a reduce action
				Iterator<CountryState> mainIt = stateList[i].maintainStates.iterator();
				while(mainIt.hasNext()) {
					mainIt.next().reduceCarbon();
				}
			}
		}

		CountryState optimalState = getOptimalState(stateList[LOOK_AHEAD_YEARS-1].maintainStates);

		return optimalState;

	}

	/**
	 * Look at the given array of countryStates and return the best one
	 * @return
	 */
	private CountryState getOptimalState(ArrayList<CountryState> maintainStates) {
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
					Compare comparison = testState.isBetterOrWorseThan(bestState);
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

			this.year = 0;

			this.action = null;
		}

		private Compare isBetterOrWorseThan(CountryState bestState) {
			boolean isBetter = true;
			boolean isWorse = true;

			double carbonDiff = carbonOutput - carbonAbsorption;
			double bestCarbonDiff = bestState.carbonOutput - bestState.carbonAbsorption;

			//If we output less carbon than the other state
			if (carbonDiff < bestCarbonDiff) {
				isWorse = false;
			}
			else if (carbonDiff > bestCarbonDiff){
				isBetter = false;
			}

			//If we have more money
			if (this.availableToSpend > bestState.availableToSpend) {
				isWorse = false;
			}
			else if (this.availableToSpend < bestState.availableToSpend){
				isBetter = false;
			}
			
			//If we have a higher GDP
			if (this.GDP > bestState.GDP) {
				isWorse = false;
			}
			else if (this.GDP < bestState.GDP){
				isBetter = false;
			}
			
			//If we have a higher GDP rate
			if (this.GDPRate > bestState.GDPRate) {
				isWorse = false;
			}
			else if (this.GDPRate < bestState.GDPRate){
				isBetter = false;
			}

			//If we have fewer credits
			if (this.carbonOffset < bestState.carbonOffset) {
				isWorse = false;
			}
			else if (this.carbonOffset > bestState.carbonOffset){
				isBetter = false;
			}

			//If we make more energy
			if (this.energyOutput > bestState.energyOutput) {
				isWorse = false;
			}
			else if (this.energyOutput < bestState.energyOutput){
				isBetter = false;
			}

			if (isBetter) {
//				System.out.println("culledbetter");
				return Compare.BETTER;
			}
			if (isWorse) {
//				System.out.println("culledworse");
				return Compare.WORSE;
			}
			return Compare.UNKNOWN;
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

			stateList[this.year-1].addReduce(this);

			double oldCarbonDifference = previousState.getCarbonDifference();

			double[] investments = new double[2];

			double investmentCost = country.getAbsorbReduceInvestment(action.investFrac * oldCarbonDifference, previousState, investments);

			double marketCost = country.getMarketBuyPrice(action.buyCreditFrac * oldCarbonDifference, this.year);

			double arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);

			double absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);

			double energyReduction = action.shutDownFrac * oldCarbonDifference;

			this.carbonOutput = previousState.carbonOutput - oldCarbonDifference*(action.investFrac + action.shutDownFrac);
			this.energyOutput = previousState.energyOutput - energyReduction;
			this.carbonOffset = previousState.carbonOffset - oldCarbonDifference*action.buyCreditFrac;
			this.carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;
			this.emissionsTarget = previousState.emissionsTarget;

			this.availableToSpend = previousState.availableToSpend - investmentCost - marketCost;

			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;

			this.arableLandArea = previousState.arableLandArea - arableLandCost;

		}
		private CountryState(CountryState previousState,MaintainAction action) {
			this.previousState = previousState;
			this.action = action;

			this.year = previousState.year;

			stateList[this.year-1].addMaintain(this);

			this.emissionsTarget = country.getNextEmissionTarget(previousState.emissionsTarget);

			//Calculate the increase in energy production and carbon
			double energyCost = action.industryFrac * previousState.availableToSpend;

			double energyIncrease = country.getCarbonEnergyIncrease(energyCost);

			//Balance carbon increase with market purchases and investment

			double carbonOffsetIncrease = action.buyCreditOffsetFrac * energyIncrease;

			double marketBuyCost = country.getMarketBuyPrice(carbonOffsetIncrease, this.year);

			double totalCarbonInvestOffset = action.investOffsetFrac * energyIncrease + action.investFrac * previousState.carbonOutput;

			double[] investments = new double[2];

			double investmentCost = country.getAbsorbReduceInvestment(totalCarbonInvestOffset, previousState, investments);

			double arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);

			double absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);

			//Phase 2 - shut down factories, then sell some of the difference.

			double energyReduction = action.shutDownFrac * previousState.carbonOutput;
			double carbonReduction = energyReduction;

			double prevCarbonOutput = previousState.carbonOutput - previousState.carbonAbsorption - previousState.carbonOffset;

			//carbonDifference next year, including next years change in sanctions
			double carbonDifference = prevCarbonOutput * (action.shutDownFrac + action.investFrac) - prevCarbonOutput + this.emissionsTarget;

			double carbonOffsetDecrease = action.sellFrac*carbonDifference;

			double marketSellGain = country.getMarketSellPrice(carbonOffsetDecrease, this.year+1);



			energyOutput = previousState.energyOutput + energyIncrease - energyReduction;

			carbonOutput = previousState.carbonOutput - carbonReduction;

			carbonOffset = previousState.carbonOffset + carbonOffsetIncrease - carbonOffsetDecrease;

			arableLandArea = previousState.arableLandArea - arableLandCost;

			carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;

			//TODO ensure the formula is correct
			double tempGDPRate = previousState.GDPRate + GameConst.GROWTH_SCALER*(energyOutput)/previousState.GDP;
			GDPRate = tempGDPRate/100;

			GDP = previousState.GDP + previousState.GDP*GDPRate;

			availableToSpend = previousState.availableToSpend - investmentCost - marketBuyCost + marketSellGain + (GDP * GameConst.PERCENTAGE_OF_GDP);
		}

		/**
		 * Branch off for all reduce actions
		 */
		private void reduceCarbon() {

			//If we actually need to reduce our carbon
			if (this.getCarbonDifference() > 0) {
				for (int i=0; i<=100; i=i+10) {
					int shutDown = i;

					for (int j=0; j<=100-shutDown; j=j+10) {
						int buyCredit = j;

						int invest = 100 - shutDown - buyCredit;
						new CountryState(this, new ReduceAction(shutDown/100f, buyCredit/100f, invest/100f));
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

			//Two phases:
			//Phase one: optional
			//Invest in carbon producing industries as a % of available cash
			//Offset increase in carbon by investing in reduction/absorption OR by buying more offset from the market

			//Phase 2 - further reduction and selling
			//Invest in reduction/absorption AND/OR shut down factories - only shut down if we didn't open some
			//Sell offset on market (as % of offset below next years target)
			//Offset will go on sale next year.

			int counter = 0;

			float industryFrac;
			float investOffsetFrac;
			float buyCreditOffsetFrac;
			float shutDownFrac;
			float investFrac;
			float sellFrac;

			//Use some percentage of our available cash to make more factories
			for (int i=0; i<=100; i=i+20) {
				industryFrac = i;

				//If we don't invest in industry we can shut down factories
				if (industryFrac == 0) {
					investOffsetFrac = 0;
					buyCreditOffsetFrac = 0;

					for (int j=0; j<=5; j=j+1) {
						shutDownFrac = j;
						for (int k=0; k<=5 - shutDownFrac; k = k+1) {
							investFrac = k;
							for (int l=0; l<=100; l=l+20) {
								sellFrac = l;

								counter++;
								new CountryState(this, new MaintainAction(industryFrac/100, 
										investOffsetFrac/100, buyCreditOffsetFrac/100, 
										shutDownFrac/100, investFrac/100, sellFrac/100));
							}
						}
					}
				}
				//If we do invest in industry
				else {
					shutDownFrac = 0;
					for (int j = 0; j<=100; j=j+20) {
						investOffsetFrac = j;
						buyCreditOffsetFrac = 100 - investOffsetFrac;
						for (int l = 0; l<=5; l=l+1) {
							investFrac = l;
							for (int m = 0; m<=100; m=m+20) {
								sellFrac = m;

								counter++;
								new CountryState(this, new MaintainAction(industryFrac/100, 
										investOffsetFrac/100, buyCreditOffsetFrac/100, 
										shutDownFrac/100, investFrac/100, sellFrac/100));
							}
						}
					}
				}
			}
//			System.out.println("Counter = " + counter);
		}

		/**
		 * @return a slight overestimate of the carbon we need to offset. Positive means we need to reduce to meet our target
		 */
		private double getCarbonDifference() {
			return 1.02*(this.carbonOffset + this.carbonOutput - this.emissionsTarget);
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

		public void addReduce(CountryState countryState) {
			reduceStates.add(countryState);
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
	private class ReduceAction implements Action {	

		public ReduceAction(float shutDown, float buyCredit, float invest) {
			this.shutDownFrac = shutDown;
			this.buyCreditFrac = buyCredit;
			this.investFrac = invest;
		}

		final float shutDownFrac;
		final float buyCreditFrac;
		final float investFrac;
	}

	/**
	 * A maintain action
	 * @author Nik
	 *
	 */
	private class MaintainAction implements Action {
		public MaintainAction(float industryFrac, float investOffsetFrac, float buyCreditOffsetFrac,
				float shutDownFrac, float investFrac, float sellFrac) {
			this.industryFrac = industryFrac;
			this.investOffsetFrac = investOffsetFrac;
			this.buyCreditOffsetFrac = buyCreditOffsetFrac;
			this.shutDownFrac = shutDownFrac;
			this.investFrac = investFrac;
			this.sellFrac = sellFrac;
		}

		//Phase one actions

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

		//Phase two actions

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

	}

	/**
	 * Structure containing information on action we've taken.
	 * When passed into a CountryState constructor, action will be performed
	 * Yes, this interface is supposed to be completely empty.
	 * @author Nik
	 *
	 */
	private abstract interface Action {}

}
