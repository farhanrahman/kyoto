package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;

/**
 * 
 * @author Nik
 *
 */
class CountrySimulator {

	final private static int LOOK_AHEAD_YEARS = 15;

	final private static int CARBON_CHUNKS = 21;
	
	final private static double CARBON_CHUNK_SIZE = 100.0/(CARBON_CHUNKS-1);
	
	public CountrySimulator(AnnexOneReduce r) {
		this.country = r;
	}
	
	final private AnnexOneReduce country;

	private StateList[] stateList = new StateList[LOOK_AHEAD_YEARS];
	
	private static CountryState startState;

	public void simulate(double carbonOutput, double energyOutput, double carbonOffset, double emissionsTarget, double availableToSpend,
			double GDP, double GDPRate, double arableLandArea) {
		
		startState = new CountryState(carbonOutput,energyOutput,
				carbonOffset,emissionsTarget,availableToSpend,
				GDP,GDPRate,arableLandArea);

		//Reset old simulation data
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			stateList[i] = new StateList();
		}
		
		//Fill up the first array in the table
		startState.reduceCarbon();
		
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			cullStates(stateList[i].reducestates);
			for (int j = 0; j<stateList[i].reducestates.size(); j++) {
				stateList[i].reducestates.get(j).
			}
			
			
			
		}
	}

	class CountryState {

		private CountryState(final double carbonOutput, final double energyOutput,
				final double carbonOffset, final double emissionsTarget,
				final double availableToSpend, final double GDP, final double GDPRate,
				final double arableLandArea) {

			this.carbonOutput = carbonOutput;
			this.energyOutput = energyOutput;
			this.carbonOffset = carbonOffset;
			this.emissionsTarget = emissionsTarget;

			this.availableToSpend = availableToSpend;
			this.GDP = GDP;
			this.GDPRate = GDPRate;
			this.arableLandArea = arableLandArea;

			this.previousState = null;

			this.year = 0;

			this.action = null;
		}

		private CountryState(CountryState previousState,Action action) {
			this.previousState = previousState;
			this.action = action;

			if (previousState.action.type == Action.ActionType.REDUCE) {
				this.year = previousState.year;
			}
			else {
				this.year = previousState.year + 1;
			}
			
			stateList[year - 1].add(this);

			double[] investments = new double[2];
			
			double cost = country.getAbsorbReduceInvestment(getCarbonDifference(), previousState, investments);

		}

		final public double carbonOutput;
		final public double energyOutput;
		final public double carbonOffset;
		final public double emissionsTarget;

		final public double availableToSpend;
		final public double GDP;
		final public double GDPRate;
		final public double arableLandArea;

		final public CountryState previousState;
		final public int year;

		/**
		 * The action taken to get to this current state
		 */
		final private Action action;

		private void reduceCarbon() {

		}
		
		private void maintainCarbon() {
			
		}
		
		private double getCarbonDifference() {
			return (this.emissionsTarget - this.carbonOffset - this.carbonOutput);
		}
		
	}

	private static class StateList {
		private ArrayList<CountryState> reducestates = new ArrayList<CountryState>();
		private ArrayList<CountryState> maintainstates = new ArrayList<CountryState>();

		public void add(CountryState countryState) {
			if (countryState.action.type == Action.ActionType.MAINTAIN ) {
				maintainstates.add(countryState);
			}
			else {
				reducestates.add(countryState);
			}
		}	
	}


	private static class Action {

		final private ActionType type;

		public Action(ActionType type) {
			this.type = type;
		}

		private static enum ActionType {REDUCE,MAINTAIN}
	}

	private static class ReduceAction extends Action {	

		public ReduceAction(float shutDown, float buyCredit, float invest) {
			super(Action.ActionType.REDUCE);
			this.shutDownFrac = shutDown;
			this.buyCreditFrac = buyCredit;
			this.investFrac = invest;
		}

		final float shutDownFrac;
		final float buyCreditFrac;
		final float investFrac;
	}
	private static class MaintainAction extends Action {
		public MaintainAction() {
			super(Action.ActionType.MAINTAIN);
		}

	}

}
