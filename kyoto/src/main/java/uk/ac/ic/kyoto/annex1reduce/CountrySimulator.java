package uk.ac.ic.kyoto.annex1reduce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 * @author Nik
 *
 */
class CountrySimulator {
	
	static int numInstant = 0;

	final private static int LOOK_AHEAD_YEARS = 15;
	
	public CountrySimulator(AnnexOneReduce r) {
		this.country = r;
	}
	
	final private AnnexOneReduce country;

	private StateList[] stateList = new StateList[LOOK_AHEAD_YEARS];
	
	private ArrayList<CountryState> tempList = new ArrayList<CountryState>(10000);
	
	private static CountryState startState;

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
		
		System.out.println("initialSize = " + tempList.size());
		
		//For all years to look ahead
		for (int i=0; i<LOOK_AHEAD_YEARS; i++) {
			//Cull the reduce states
			cullStates(tempList,stateList[i].reduceStates);
			System.out.println("reduceSize " + i + " = " + stateList[i].reduceStates.size());
			
			//Branch off all unculled reduce states by performing a maintain action		
			Iterator<CountryState> redIt = stateList[i].reduceStates.iterator();
			while(redIt.hasNext()) {
				redIt.next().maintainCarbon();
			}
						
			//Cull the maintain states
			cullStates(tempList,stateList[i].maintainStates);
			
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
	private CountryState getOptimalState(HashSet<CountryState> maintainStates) {
		return null;
	}

	/**
	 * Goes through all states and will cull all but the most useful
	 * @param states
	 */
	private void cullStates(final ArrayList<CountryState> tempStates, final HashSet<CountryState> states) {
		
		//Firstly, remove all states with invalid attributes
		
		Iterator<CountryState> iterator = tempStates.iterator();
		
		while(iterator.hasNext()) {
			CountryState state = iterator.next();
			if (state.availableToSpend < 0) {
				iterator.remove();
			}
			else if (state.carbonOutput < 0) {
				iterator.remove();
			}
		}
		
		//Add all unculled objects to the set
		for (int i = 0; i<tempStates.size(); i++) {
			states.add(tempStates.get(i));
		}
		tempStates.clear();
			
		//Keep only the states with the best performance in every combination of categories
		//1 = (carbonOutput-carbonAbsorption)
		//2 = energyOutput
		//3 = carbonOffset
		//4 = availableToSpend
		//5 = arableLandArea
		
//		CountryState best1 = reduceStates.
//		CountryState best2 = reduceStates.get(0);
//		CountryState best3 = reduceStates.get(0);
//		CountryState best4 = reduceStates.get(0);
//		CountryState best5 = reduceStates.get(0);
//		
//		CountryState best12 = reduceStates.get(0);
//		CountryState best13 = reduceStates.get(0);
//		CountryState best14 = reduceStates.get(0);
//		CountryState best15 = reduceStates.get(0);
//		CountryState best23 = reduceStates.get(0);
//		CountryState best24 = reduceStates.get(0);
//		CountryState best25 = reduceStates.get(0);
//		CountryState best34 = reduceStates.get(0);
//		CountryState best35 = reduceStates.get(0);
//		CountryState best45 = reduceStates.get(0);
//		
//		CountryState best123 = reduceStates.get(0);
//		CountryState best124 = reduceStates.get(0);
//		CountryState best125 = reduceStates.get(0);
//		CountryState best134 = reduceStates.get(0);
//		CountryState best135 = reduceStates.get(0);
//		CountryState best145 = reduceStates.get(0);
//		CountryState best234 = reduceStates.get(0);
//		CountryState best235 = reduceStates.get(0);
//		CountryState best345 = reduceStates.get(0);
//		
//		CountryState best1234 = reduceStates.get(0);
//		CountryState best1235 = reduceStates.get(0);
//		CountryState best1245 = reduceStates.get(0);
//		CountryState best1345 = reduceStates.get(0);
//		CountryState best2345 = reduceStates.get(0);
//			
		
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
		
		/**
		 * 
		 * @param previousState The state that performs the action
		 * @param action The action performed by the previous state
		 */
		private CountryState(CountryState previousState,ReduceAction action) {
			this.previousState = previousState;
			this.action = action;

			this.year = previousState.year + 1;
			
			tempList.add(this);
			
			double oldCarbonDifference = previousState.getCarbonDifference();

			double[] investments = new double[2];
			
			double investmentCost = country.getAbsorbReduceInvestment(action.investFrac * oldCarbonDifference, previousState, investments);
			
			double marketCost = country.getMarketPrice(action.buyCreditFrac * oldCarbonDifference, this.year);
					
			double arableLandCost;
			double absorptionIncrease;
			
			try {
				 arableLandCost = country.getArableLandCost(investments[0],previousState.arableLandArea);
				 absorptionIncrease = country.getCarbonAbsorptionChange(investments[0],previousState.arableLandArea);

			} catch (Exception e) {
				e.printStackTrace();
				arableLandCost = 0;
				absorptionIncrease = 0;
			}
			
			
			double energyCost = action.shutDownFrac * oldCarbonDifference;
			
			this.carbonOutput = previousState.carbonOutput - oldCarbonDifference*(action.investFrac + action.shutDownFrac);
			this.energyOutput = previousState.energyOutput - energyCost;
			this.carbonOffset = previousState.carbonOffset - oldCarbonDifference*action.buyCreditFrac;
			this.carbonAbsorption = previousState.carbonAbsorption + absorptionIncrease;
			this.emissionsTarget = previousState.emissionsTarget;
			
			this.availableToSpend = previousState.availableToSpend - investmentCost - marketCost;
			
			this.GDPRate = previousState.GDPRate;
			this.GDP = previousState.GDP;
			
			this.arableLandArea = previousState.arableLandArea - arableLandCost;
			
			//TODO
//			System.out.println(numInstant++);
		}
		
		private CountryState(CountryState previousState,MaintainAction action) {
			this.previousState = previousState;
			this.action = action;

			this.year = previousState.year;
			
			tempList.add(this);

			// TODO some maintain actions here!
		}

		/**
		 * Branch off for all reduce actions
		 */
		private void reduceCarbon() {
			
			//If we actually need to reduce our carbon
			if (this.getCarbonDifference() > 0) {
				for (int i=0; i<=100; i=i+5) {
					int shutDown = i;
					
					for (int j=0; j<=100-shutDown; j=j+5) {
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
		 * Branch off for all maintain actions TODO
		 */
		private void maintainCarbon() {
			
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
	private static class StateList {
		private HashSet<CountryState> reduceStates = new HashSet<CountryState>();
		private HashSet<CountryState> maintainStates = new HashSet<CountryState>();	
	}

	/**
	 * A reduce action.
	 * @author Nik
	 *
	 */
	private static class ReduceAction implements Action {	

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
	 * A maintain action TODO
	 * @author Nik
	 *
	 */
	private static class MaintainAction implements Action {
		public MaintainAction() {
		}

	}
	
	/**
	 * Structure containing information on action we've taken.
	 * When passed into a CountryState constructor, action will be performed
	 * Yes, this interface is supposed to be completely empty.
	 * @author Nik
	 *
	 */
	private abstract static interface Action {}

}
