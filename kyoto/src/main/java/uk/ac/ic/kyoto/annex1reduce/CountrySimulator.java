package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.GameConst;

/**
 * 
 * @author Nik
 * 
 */
class CountrySimulator {

	private Logger logger = Logger.getLogger(CountrySimulator.class);

	private int LOOK_AHEAD_YEARS;
	private int SANCTION_YEAR;

	public CountrySimulator(AnnexOneReduce country) {
		this.country = country;
	}

	final private AnnexOneReduce country;

	private StateList[] stateList;

	private CountryState startState;

	public ActionList simulate(double carbonOutput, double energyOutput,
			double prevEnergyOutput, double carbonOffset,
			double carbonAbsorption, double emissionsTarget,
			double availableToSpend, double GDP, double GDPRate,
			double arableLandArea, int yearsUntilSanctions) {

		this.SANCTION_YEAR = yearsUntilSanctions;
		this.LOOK_AHEAD_YEARS = 8;

		// Initialise the starting point in the simulation
		startState = new CountryState(carbonOutput, energyOutput,
				prevEnergyOutput, carbonOffset, carbonAbsorption,
				emissionsTarget, availableToSpend, GDP, GDPRate, arableLandArea);

		// Init the array lists
		stateList = new StateList[LOOK_AHEAD_YEARS];

		// Reset old simulation data
		for (int i = 0; i < LOOK_AHEAD_YEARS; i++) {
			stateList[i] = new StateList();
		}

		// Fill up the first array in the table
		startState.reduceCarbon();

		// For all years to look ahead
		for (int i = 0; i < LOOK_AHEAD_YEARS; i++) {
			// Cull the reduce states

//			 logger.info("unculledReduceSize " + i + " = "
//			 + stateList[i].reduceStates.size());
			stateList[i].reduceStates = cullStates(stateList[i].reduceStates);
//			 logger.info("reduceSize " + i + " = "
//			 + stateList[i].reduceStates.size());

			// Branch off all unculled reduce states by performing a maintain
			// action
			Iterator<CountryState> redIt = stateList[i].reduceStates.iterator();
			while (redIt.hasNext()) {
				redIt.next().maintainCarbon();
			}

			// Cull the maintain states
//			 logger.info("unculledMaintainSize " + i + " = "
//			 + stateList[i].maintainStates.size());
			stateList[i].maintainStates = cullStates(stateList[i].maintainStates);
//			 logger.info("maintainSize " + i + " = "
//			 + stateList[i].maintainStates.size());

			// Branch off all unculled maintain states by performing a sell
			// action
			Iterator<CountryState> mainIt = stateList[i].maintainStates
					.iterator();
			while (mainIt.hasNext()) {
				mainIt.next().sellCarbon();
			}

			// Cull the sell states
//			 logger.info("unculledSellSize " + i + " = "
//			 + stateList[i].sellStates.size());
			stateList[i].sellStates = cullStates(stateList[i].sellStates);
//			 logger.info("sellSize " + i + " = "
//			 + stateList[i].sellStates.size());

			// So long as we aren't in the final year
			if (i != (LOOK_AHEAD_YEARS - 1)) {
				// Branch off all unculled reduce states by performing a reduce
				// action
				Iterator<CountryState> sellIt = stateList[i].sellStates
						.iterator();
				while (sellIt.hasNext()) {
					sellIt.next().reduceCarbon();
				}
			}
		}

		ActionList megaAction = getOptimalState(stateList[LOOK_AHEAD_YEARS - 1].sellStates);

		return megaAction;

	}

	/**
	 * Look at the given array of countryStates and return one of the optimal
	 * states
	 * 
	 * @return
	 */
	private ActionList getOptimalState(ArrayList<CountryState> states) {

		HashSet<CountryState> startSellStates = new HashSet<CountryState>();

		ArrayList<CountryState> startSellList = new ArrayList<CountryState>();

		for (int i = 0; i < states.size(); i++) {
			CountryState state = states.get(i).getStartSellState();
			startSellStates.add(state);
			startSellList.add(state);
		}

		// Iterator<CountryState> iterator = startSellStates.iterator();
		// while (iterator.hasNext()) {
		// logger.info("");
		// CountryState state = iterator.next();
		// state.printPath();
		// logger.info("");
		// state.printActionPath();
		//
		// }
		logger.info("");
		logger.info("NumStates = " + startSellStates.size());

		float s_investFrac = 0;
		float s_sellFrac = 0;
		float s_shutDownFrac = 0;

		float m_buyCreditOffsetFrac = 0;
		float m_industryFrac = 0;
		float m_investOffsetFrac = 0;

		float r_buyCreditFrac = 0;
		float r_investFrac = 0;
		float r_shutDownFrac = 0;

		for (int i = 0; i < startSellList.size(); i++) {
			CountryState optimalState = startSellList.get(i);

			SellAction sellAction = (SellAction) optimalState.action;
			MaintainAction maintainAction = (MaintainAction) optimalState.previousState.action;
			ReduceAction reduceAction = (ReduceAction) optimalState.previousState.previousState.action;

			s_investFrac += sellAction.investFrac;
			s_sellFrac += sellAction.sellFrac;
			s_shutDownFrac += sellAction.shutDownFrac;

			m_buyCreditOffsetFrac += maintainAction.buyCreditOffsetFrac;
			m_industryFrac += maintainAction.industryFrac;
			m_investOffsetFrac += maintainAction.investOffsetFrac;

			r_buyCreditFrac += reduceAction.buyCreditFrac;
			r_investFrac += reduceAction.investFrac;
			r_shutDownFrac += reduceAction.shutDownFrac;

		}

		int size = startSellList.size();

		s_investFrac /= size;
		s_sellFrac /= size;
		s_shutDownFrac /= size;

		m_buyCreditOffsetFrac /= size;
		m_industryFrac /= size;
		m_investOffsetFrac /= size;

		r_buyCreditFrac /= size;
		r_investFrac /= size;
		r_shutDownFrac /= size;

		logger.info("");
		logger.info(country.getName());
		logger.info("s_investFrac " + s_investFrac);
		logger.info("s_sellFrac " + s_sellFrac);
		logger.info("s_shutDownFrac " + s_shutDownFrac);

		logger.info("m_buyCreditOffsetFrac " + m_buyCreditOffsetFrac);
		logger.info("m_industryFrac " + m_industryFrac);
		logger.info("m_investOffsetFrac " + m_investOffsetFrac);

		logger.info("r_buyCreditFrac " + r_buyCreditFrac);
		logger.info("r_investFrac " + r_investFrac);
		logger.info("r_shutDownFrac " + r_shutDownFrac);

		ActionList megaAction = new ActionList(new ReduceAction(r_shutDownFrac,
				r_buyCreditFrac, r_investFrac), new MaintainAction(
				m_industryFrac, m_investOffsetFrac, m_buyCreditOffsetFrac),
				new SellAction(s_shutDownFrac, s_investFrac, s_sellFrac));

		return megaAction;
	}

	/**
	 * Goes through all states and will cull all but the most useful
	 * 
	 * @param states
	 * @return The culled list of input states
	 */
	private ArrayList<CountryState> cullStates(ArrayList<CountryState> states) {

		// Firstly, remove all states with invalid attributes

		Iterator<CountryState> iterator = states.iterator();

		while (iterator.hasNext()) {
			CountryState state = iterator.next();
			if (state.availableToSpend < 0) {
				iterator.remove();
			} else if (state.carbonOutput < 0) {
				iterator.remove();
			} else if (state.arableLandArea < 0) {
				iterator.remove();
			} else if (state.toBeCulled == true) {
				iterator.remove();
			}
		}

		boolean hasBeenReplaced = true;

		while (hasBeenReplaced) {
			hasBeenReplaced = false;

			ArrayList<CountryState> bestList = new ArrayList<CountryState>(
					states.size());

			bestList.add(states.get(0));

			for (int i = 1; i < states.size(); i++) {
				CountryState testState = states.get(i);

				boolean needsAdding = true;

				for (int j = 0; j < bestList.size(); j++) {
					CountryState bestState = bestList.get(j);
					Compare comparison = testState.action.isBetterOrWorseThan(
							testState, bestState);
					if (comparison == Compare.WORSE) {
						needsAdding = false;
						break;
					} else if (comparison == Compare.BETTER) {
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
		BETTER, WORSE, UNKNOWN
	}

	/**
	 * Structure holding country attributes after performing a certain action,
	 * and also containing the chain of events leading to that action
	 * 
	 * @author Nik
	 * 
	 */
	class CountryState {

		final public double carbonOutput;
		final public double energyOutput;
		final public double previousEnergyOutput;
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

		final public double netCarbonOutput;

		boolean toBeCulled = false;

		private CountryState(final double carbonOutput,
				final double energyOutput, final double previousEnergyOutput,
				final double carbonOffset, final double carbonAbsorption,
				final double emissionsTarget, final double availableToSpend,
				final double GDP, final double GDPRate,
				final double arableLandArea) {

			this.previousEnergyOutput = previousEnergyOutput;

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

			netCarbonOutput = carbonOutput - carbonAbsorption;
		}

		/**
		 * Constructor for a reduce state
		 * 
		 * @param previousState
		 *            The state that performs the action
		 * @param action
		 *            The action performed by the previous state
		 */
		private CountryState(CountryState previousState, ReduceAction action) {

			this.previousEnergyOutput = previousState.energyOutput;

			this.previousState = previousState;
			this.action = action;
			this.year = previousState.year + 1;
			this.emissionsTarget = previousState.emissionsTarget;
			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;
			stateList[this.year].addReduce(this);

			double oldCarbonDifference = previousState.getCarbonDifference();

			// Price of reaching our target by investment and buying credits
			double[] investments = new double[2];
			double carbonAbsorbedReduced = action.investFrac
					* oldCarbonDifference;
			double investmentCost = country.getAbsorbReduceInvestment(
					carbonAbsorbedReduced, previousState, investments);
			double carbonOffsetIncreased = oldCarbonDifference
					* action.buyCreditFrac;
			double marketCost = carbonOffsetIncreased
					* country.getMarketBuyUnitPrice(this.year);
			this.availableToSpend = previousState.availableToSpend
					- investmentCost - marketCost;

			// Amount of arable land lost
			double arableLandCost = country.getArableLandCost(investments[0],
					previousState.arableLandArea);
			this.arableLandArea = previousState.arableLandArea - arableLandCost;

			// Amount of carbon absorption we've increased by
			double absorptionIncrease = country.getCarbonAbsorptionChange(
					investments[0], previousState.arableLandArea);
			this.carbonAbsorption = previousState.carbonAbsorption
					+ absorptionIncrease;

			// Amount of energy and carbon we've lost by shutting down factories
			double energyReduction = action.shutDownFrac * oldCarbonDifference;
			this.energyOutput = previousState.energyOutput - energyReduction;

			// Our carbon output decreased by the amount we invest in reduction
			// plus the amount of factories we shut down
			double carbonOutputReduced = country.getCarbonReduction(
					investments[1], previousState);
			this.carbonOutput = previousState.carbonOutput
					- carbonOutputReduced - energyReduction;

			// Our carbon offset increased by the amount of carbon we bought in
			// the market
			this.carbonOffset = previousState.carbonOffset
					+ carbonOffsetIncreased;

			netCarbonOutput = carbonOutput - carbonAbsorption;
		}

		/**
		 * Constructor for a maintain state
		 * 
		 * @param previousState
		 * @param action
		 */
		private CountryState(CountryState previousState, MaintainAction action) {

			this.previousEnergyOutput = previousState.previousEnergyOutput;

			this.previousState = previousState;
			this.action = action;
			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;
			this.year = previousState.year;
			this.emissionsTarget = previousState.emissionsTarget;
			stateList[this.year].addMaintain(this);

			// energy Increase = carbon Increase when investing in factories
			double energyIncreasePrice = action.industryFrac
					* previousState.availableToSpend;
			double energyIncrease = country
					.getCarbonEnergyIncrease(energyIncreasePrice);

			// Number of carbon credits gained by buying credits
			double carbonCreditIncrease = action.buyCreditOffsetFrac
					* energyIncrease;
			double marketBuyPrice = carbonCreditIncrease
					* country.getMarketBuyUnitPrice(this.year);

			// Amount of carbon we need to reduce by investing in offsets
			double[] investments = new double[2];
			double investmentPrice = country.getAbsorbReduceInvestment(
					action.investOffsetFrac * energyIncrease, previousState,
					investments);
			double arableLandReduction = country.getArableLandCost(
					investments[0], previousState.arableLandArea);
			double absorptionIncrease = country.getCarbonAbsorptionChange(
					investments[0], previousState.arableLandArea);
			double carbonOutputReduction = country.getCarbonReduction(
					investments[1], previousState);

			this.energyOutput = previousState.energyOutput + energyIncrease;
			this.carbonOutput = previousState.carbonOutput + energyIncrease
					- carbonOutputReduction;

			this.arableLandArea = previousState.arableLandArea - arableLandReduction;

			this.carbonAbsorption = previousState.carbonAbsorption
					+ absorptionIncrease;
			
			this.carbonOffset = previousState.carbonOffset
					+ carbonCreditIncrease;
			
			this.availableToSpend = previousState.availableToSpend
					- investmentPrice - marketBuyPrice - energyIncreasePrice;

			this.netCarbonOutput = carbonOutput;
		}

		/**
		 * Create a new sellState
		 * 
		 * @param previousState
		 * @param action
		 */
		private CountryState(CountryState previousState, SellAction action) {

			this.previousEnergyOutput = previousState.previousEnergyOutput;

			this.previousState = previousState;
			this.action = action;
			this.year = previousState.year;
			this.emissionsTarget = country
					.getNextEmissionTarget(previousState.emissionsTarget);
			stateList[this.year].addSell(this);

			// Our current total carbon output, including any offsets
			double totalCarbonOutput = previousState.carbonOutput
					- previousState.carbonOffset
					- previousState.carbonAbsorption;

			// Shut down factories
			double shutDownCarbonReduction = totalCarbonOutput
					* action.shutDownFrac;

			// Invest in being clean
			double carbonAbsorbedReduced = totalCarbonOutput
					* action.investFrac;
			double[] investments = new double[2];
			double investmentCost = country.getAbsorbReduceInvestment(
					carbonAbsorbedReduced, previousState, investments);
			double arableLandReduction = country.getArableLandCost(
					investments[0], previousState.arableLandArea);
			double absorptionIncrease = country.getCarbonAbsorptionChange(
					investments[0], previousState.arableLandArea);
			double carbonOutputReduction = country.getCarbonReduction(
					investments[1], previousState);

			// Sell additional offset over next years target
			double newCarbonOutput = totalCarbonOutput
					- shutDownCarbonReduction - carbonAbsorbedReduced;
			double carbonBelowTarget = emissionsTarget - newCarbonOutput;

			double totalCreditsSold = carbonBelowTarget * action.sellFrac;

			double marketSellEarnings = totalCreditsSold
					* country.getMarketSellUnitPrice(this.year);

			this.arableLandArea = previousState.arableLandArea
					- arableLandReduction;
			this.carbonAbsorption = previousState.carbonAbsorption
					+ absorptionIncrease;
			this.energyOutput = previousState.energyOutput
					- shutDownCarbonReduction;
			
			this.carbonOutput = previousState.carbonOutput
					- shutDownCarbonReduction - carbonOutputReduction;
			
			double tempCarbonOffset = previousState.carbonOffset - totalCreditsSold;
			
			double tempCarbonOutput = this.carbonOutput - this.carbonAbsorption
					- tempCarbonOffset;
			
			double carbonAboveTarget = tempCarbonOutput - previousState.emissionsTarget;
			
			if (carbonAboveTarget > 0) {
				toBeCulled = true;
			}
			
			//Reset carbon offset at the end of a session
			if (this.year == SANCTION_YEAR) {
				this.carbonOffset = 0;
				SANCTION_YEAR += 10;
			} else {
				this.carbonOffset = previousState.carbonOffset - totalCreditsSold;
			}
			
			GDPRate = calculateGDPRate(previousState.GDPRate,
					this.energyOutput, this.previousEnergyOutput);

			GDP = previousState.GDP + previousState.GDP * GDPRate;
			
			double tempAvailableToSpend = previousState.availableToSpend
					- investmentCost + marketSellEarnings;

			if (tempAvailableToSpend < 0) {
				toBeCulled = true;
			}

			availableToSpend = tempAvailableToSpend
					+ (GDP * GameConst.getPercentageOfGdp());

			netCarbonOutput = this.carbonOutput - this.carbonAbsorption;
		}

		private double getCarbonDifference() {
			double netCarbonOutput = this.carbonOutput - this.carbonAbsorption
					- this.carbonOffset;
			double carbonDifference = netCarbonOutput - this.emissionsTarget;
			return carbonDifference;
		}

		/**
		 * Branch off for all reduce actions
		 */
		private void reduceCarbon() {
			float shutDown;
			float buyCredit;
			float invest;

			int maxGrains = 10;
			int minGrains = 2;
			int yearModifier = Math.max(0, 4 * this.year);
			int numGrains = Math.max(maxGrains - yearModifier, minGrains);
			float grain = 100f / numGrains;

			// If we actually need to reduce our carbon
			if (this.getCarbonDifference() > 0) {

				for (float i = 0; i <= 100; i = i + grain) {
					shutDown = i;

					for (float j = 0; j <= 100 - shutDown; j = j + grain) {
						buyCredit = j;

						invest = 100 - shutDown - buyCredit;
						new CountryState(this, new ReduceAction(shutDown / 100,
								buyCredit / 100, invest / 100));
					}
				}
			}
			// Else, do nothing
			else {
				new CountryState(this, new ReduceAction(0, 0, 0));
			}
		}

		/**
		 * Branch off for all maintain actions
		 */
		private void maintainCarbon() {

			float industryFrac;
			float investOffsetFrac;
			float buyCreditOffsetFrac;

			// Do nothing
			new CountryState(this, new MaintainAction(0, 0, 0));

			// If we shut down factories last phase, it's pointless to invest in
			// factories this phase
			if (((ReduceAction) action).shutDownFrac != 0) {
				return;
			}

			// Invest some chunk of our available cash in industry, offset it by
			// buying credits or absorbing

			int maxGrains = 10;
			int minGrains = 2;
			int yearModifier = Math.max(0, 4 * this.year);
			int numGrains = Math.max(maxGrains - yearModifier, minGrains);
			float max = 90f;
			float grain = max / numGrains;
			float min = grain;

			for (float i = min; i <= max; i = i + grain) {
				industryFrac = i;
				investOffsetFrac = 100;
				buyCreditOffsetFrac = 0;

				new CountryState(this, new MaintainAction(industryFrac / 100,
						investOffsetFrac / 100, buyCreditOffsetFrac / 100));
			}

			// If last phase we didn't buy carbon, there's no point in buying
			// more as it's not going to be a good price.
			if (((ReduceAction) action).buyCreditFrac == 0) {
				return;
			}

			for (float i = min; i <= max; i = i + grain) {
				industryFrac = i;
				investOffsetFrac = 0;
				buyCreditOffsetFrac = 100;

				new CountryState(this, new MaintainAction(industryFrac / 100,
						investOffsetFrac / 100, buyCreditOffsetFrac / 100));

			}
		}

		/**
		 * Branch off for all sell actions
		 */
		private void sellCarbon() {

			float shutDownFrac;
			float investFrac;
			float sellFrac;

			int maxGrains = 10;
			int minGrains = 2;
			int yearModifier = Math.max(0, 4 * this.year);
			int numGrains = Math.max(maxGrains - yearModifier, minGrains);
			float max = 2f;
			float grain = max / numGrains;
			float min = grain;

			// Do nothing
			new CountryState(this, new SellAction(0, 0, 0));

			// Ensure we don't try to sell if the sell price is 0
			if (country.getMarketSellUnitPrice(this.year) < 1) {
				return;
			}

			for (float i = min; i <= max; i = i + grain) {
				shutDownFrac = 0;
				investFrac = i;
				sellFrac = 100;
				new CountryState(this, new SellAction(shutDownFrac / 100,
						investFrac / 100, sellFrac / 100));
			}

			// If we've invested in factories last phase, shutting them down
			// this phase is silly
			if (((MaintainAction) action).industryFrac != 0) {
				return;
			}

			for (float i = min; i <= max; i = i + grain) {
				shutDownFrac = i;
				investFrac = 0;
				sellFrac = 100;
				new CountryState(this, new SellAction(shutDownFrac / 100,
						investFrac / 100, sellFrac / 100));
			}

		}

		public CountryState getStartSellState() {

			CountryState sellState = null;
			CountryState maintainState = null;
			CountryState reduceState = null;
			CountryState startState = this;
			while (startState.previousState != null) {
				sellState = maintainState;
				maintainState = reduceState;
				reduceState = startState;
				startState = startState.previousState;
			}
			return sellState;
		}

		public void printState() {
			logger.info("Year " + year);
			logger.info("AvailToSpend " + this.availableToSpend);
			logger.info("CNetOutput " + this.netCarbonOutput);
			logger.info("energyOutput " + this.energyOutput);
			logger.info("GDP " + this.GDP);
			logger.info("GDPRate " + this.GDPRate);
			if (action != null) {
				action.printDetails();
			}
		}

		public void printPath() {
			printState();
			if (this.previousState != null) {
				this.previousState.printPath();
			}
		}

		public void printActionPath() {
			if (action != null) {
				action.printDetails();
			}
			if (this.previousState != null) {
				this.previousState.printActionPath();
			}
		}

	}

	private static double calculateGDPRate(double oldGDPRate,
			double energyOutput, double prevEnergyOutput) {

		double sum;
		double marketStateFactor = 0.5;
		double GDPRate = oldGDPRate;
		if (energyOutput - prevEnergyOutput >= 0) {
			sum = (((energyOutput - prevEnergyOutput) / prevEnergyOutput)
					* GameConst.getEnergyGrowthScaler() * marketStateFactor + GDPRate * 100) / 2;
			GDPRate = GameConst.getMaxGDPGrowth() - GameConst.getMaxGDPGrowth()
					* Math.exp(-sum * GameConst.getGrowthScaler());
		} else {
			sum = ((energyOutput - prevEnergyOutput) / prevEnergyOutput)
					* GameConst.getEnergyGrowthScaler();
			sum = Math.abs(sum);
			GDPRate = -(GameConst.getMaxGDPGrowth() - GameConst
					.getMaxGDPGrowth()
					* Math.exp(-sum * GameConst.getGrowthScaler()));
		}

		GDPRate /= 100; // Needs to be a % for rate formula

		return GDPRate;
	}

	/**
	 * Structure containing a list of states performed in a given year, after
	 * performing each action phase (reduce and maintain)
	 * 
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
	 * 
	 * @author Nik
	 * 
	 */
	static class ReduceAction extends Action {

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

			// If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			} else if (state1.availableToSpend < state2.availableToSpend) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			} else if (state1.GDP < state2.GDP) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDPRate > state2.GDPRate) {
				isWorse = false;
			} else if (state1.GDPRate < state2.GDPRate) {
				isBetter = false;
			}

			// If we make more energy
			if (state1.energyOutput > state2.energyOutput) {
				isWorse = false;
			} else if (state1.energyOutput < state2.energyOutput) {
				isBetter = false;
			}

			// If we have less carbon output
			if (state1.netCarbonOutput < state2.netCarbonOutput) {
				isWorse = false;
			} else if (state1.netCarbonOutput > state2.netCarbonOutput) {
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

		@Override
		public void printDetails() {
			System.out.println("Reduce State");
			System.out.println("BuyCredits = " + this.buyCreditFrac);
			System.out.println("Invest = " + this.investFrac);
			System.out.println("shutDownFrac = " + this.shutDownFrac);
		}
	}

	/**
	 * A maintain action
	 * 
	 * @author Nik
	 * 
	 */
	static class MaintainAction extends Action {
		public MaintainAction(float industryFrac, float investOffsetFrac,
				float buyCreditOffsetFrac) {
			this.industryFrac = industryFrac;
			this.investOffsetFrac = investOffsetFrac;
			this.buyCreditOffsetFrac = buyCreditOffsetFrac;
		}

		/**
		 * Fraction of gdp spent on investing on factories.
		 */
		final float industryFrac;

		/**
		 * Fraction of carbon created by increasing industry to offset by
		 * absorption etc.
		 */
		final float investOffsetFrac;

		/**
		 * Fraction of carbon created by increasing industry to offset by buying
		 * credits etc.
		 */
		final float buyCreditOffsetFrac;

		/**
		 * Compare two states that have just had a maintain action applied
		 */
		@Override
		Compare isBetterOrWorseThan(CountryState state1, CountryState state2) {

			boolean isBetter = true;
			boolean isWorse = true;

			// If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			} else if (state1.availableToSpend < state2.availableToSpend) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			} else if (state1.GDP < state2.GDP) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDPRate > state2.GDPRate) {
				isWorse = false;
			} else if (state1.GDPRate < state2.GDPRate) {
				isBetter = false;
			}

			// If we make more energy
			if (state1.energyOutput > state2.energyOutput) {
				isWorse = false;
			} else if (state1.energyOutput < state2.energyOutput) {
				isBetter = false;
			}

			// If we have less carbon output
			if (state1.netCarbonOutput < state2.netCarbonOutput) {
				isWorse = false;
			} else if (state1.netCarbonOutput > state2.netCarbonOutput) {
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

		@Override
		public void printDetails() {
			System.out.println("Maintain State");
			System.out.println("BuyCreditOffset = " + this.buyCreditOffsetFrac);
			System.out.println("Industry = " + this.industryFrac);
			System.out.println("InvestOffset = " + this.investOffsetFrac);
		}
	}

	static class SellAction extends Action {

		public SellAction(float shutDownFrac, float investFrac, float sellFrac) {
			this.shutDownFrac = shutDownFrac;
			this.investFrac = investFrac;
			this.sellFrac = sellFrac;
		}

		/**
		 * Fraction of carbon to reduce by shutting down factories. Impossible
		 * if you perform any actions in phase one
		 */
		final float shutDownFrac;

		/**
		 * Fraction of remaining carbon to reduce by investing in absorption
		 * etc.
		 */
		final float investFrac;

		/**
		 * Fraction of our current carbon offset to sell on the market next
		 * year. Calculated using next years carbon target.
		 */
		final float sellFrac;

		/**
		 * Compare two states that have just had a sell action applied
		 */
		@Override
		Compare isBetterOrWorseThan(CountryState state1, CountryState state2) {

			boolean isBetter = true;
			boolean isWorse = true;

			// If we have more money
			if (state1.availableToSpend > state2.availableToSpend) {
				isWorse = false;
			} else if (state1.availableToSpend < state2.availableToSpend) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDP > state2.GDP) {
				isWorse = false;
			} else if (state1.GDP < state2.GDP) {
				isBetter = false;
			}

			// If we have a higher GDP
			if (state1.GDPRate > state2.GDPRate) {
				isWorse = false;
			} else if (state1.GDPRate < state2.GDPRate) {
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

		@Override
		public void printDetails() {
			System.out.println("Sell State");
			System.out.println("Invest = " + this.investFrac);
			System.out.println("shutDownFrac = " + this.shutDownFrac);
			System.out.println("Sell = " + this.sellFrac);
		}
	}

	/**
	 * Structure containing information on action we've taken. When passed into
	 * a CountryState constructor, action will be performed
	 * 
	 * @author Nik
	 * 
	 */
	private static abstract class Action {
		abstract Compare isBetterOrWorseThan(CountryState state1,
				CountryState state2);

		public abstract void printDetails();
	}

	static class ActionList {
		final ReduceAction reduce;
		final MaintainAction maintain;
		final SellAction sell;

		public ActionList(ReduceAction r, MaintainAction m, SellAction s) {
			reduce = r;
			maintain = m;
			sell = s;
		}
	}

}
