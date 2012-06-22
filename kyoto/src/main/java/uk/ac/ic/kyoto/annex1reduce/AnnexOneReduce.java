package uk.ac.ic.kyoto.annex1reduce;

import java.awt.event.ActionListener;
import java.util.UUID;

import uk.ac.ic.kyoto.annex1reduce.CountrySimulator.*;
import uk.ac.ic.kyoto.countries.IsolatedAbstractCountry;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * 
 * @author Nik
 * 
 */
public class AnnexOneReduce extends IsolatedAbstractCountry {
	private EU eu;
	final private CountrySimulator simulator;

	public AnnexOneReduce(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate,
				energyOutput, carbonOutput);
		simulator = new CountrySimulator(this);
	}

	@Override
	public void initialiseCountry() {
		/* Add the country to the EU service */
		try {
			this.eu = this.getEnvironmentService(EU.class);
			this.eu.addMemberState(this);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach EU service.");
			e.printStackTrace();
		}
	}

	/**
	 * Take an input and process the data.
	 */
	@Override
	protected void processInput(Input in) {
		// TODO needs to 
	}

	@Override
	protected void yearlyFunction() {
		// Run the simulation once at the current market price
		runSimulation();
	}

	@Override
	protected void sessionFunction() {
		// TODO
	}

	@Override
	protected void behaviour() {

	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * The amount of carbon we want to buy at the current (or lower) price
	 */
	double buyCarbon;

	/**
	 * Average cost of the carbon we want to buy
	 */
	double buyCarbonAveragePrice;
	
	/**
	 * Amount of carbon we want to sell
	 */
	double sellCarbon;

	/**
	 * Average cost of the carbon we want to sell
	 */
	double sellCarbonAveragePrice;

	/**
	 * Runs the simulation with current country information Returns the amount
	 * of carbon we want to buy and sell and the average buy and sell price
	 */
	private ActionList runSimulation() {

		ActionList optimal = simulator.simulate(getCarbonOutput(),
				getEnergyOutput(), getPrevEnergyOutput(), getCarbonOffset(),
				getCarbonAbsorption(), getEmissionsTarget(),
				getAvailableToSpend(), getGDP(), getGDPRate(),
				getArableLandArea(), getYearsUntilSanctions());

		double carbonDifference = this.getCarbonOffset()
				+ this.getCarbonAbsorption() - this.getCarbonOffset();

		// The amount of carbon we want to buy at the current price
		buyCarbon = carbonDifference * optimal.reduce.buyCreditFrac;

		double r_investCarbon = carbonDifference * optimal.reduce.investFrac;
		double[] r_investments = new double[2];
		double totalCost = getAbsorbReduceInvestment(r_investCarbon,
				r_investments);
		double creditCost = getMarketBuyPrice(buyCarbon);

		double estimatedMoney = getAvailableToSpend() - totalCost - creditCost;

		// The estimated money remaining after
		double industryInvest = estimatedMoney *= optimal.maintain.industryFrac;

		double carbonIncrease = energyUsageHandler
				.calculateCarbonIndustryGrowth(industryInvest);

		buyCarbon += carbonIncrease * optimal.maintain.buyCreditOffsetFrac;

		buyCarbonAveragePrice = getMarketBuyPrice(buyCarbon) / buyCarbon;

		return optimal;
	}

	// TODO get years until sanctions
	private int getYearsUntilSanctions() {
		return 10;
	}
	/**
	 * Called at the end of the year, after all trades have been completed. Will
	 * recalculate optimal path and perform it, and will update sellCarbon and
	 * sellCarbonAveragePrice to the values they need to be to sell in the next
	 * session
	 */
	private void performReduceMaintainActions() {

		// Assume all buys have been completed, work out new optimal path
		disableBuying();

		ActionList optimal = runSimulation();

		// Reenable buying
		enableBuying();

		// REDUCE PHASE ACTIONS

		double carbonDifference = this.getCarbonOffset()
				+ this.getCarbonAbsorption() - this.getCarbonOffset();

		// Shut down factories
		double r_shutDownCarbon = carbonDifference
				* optimal.reduce.shutDownFrac;

		if (r_shutDownCarbon != 0) {
			try {
				energyUsageHandler.reduceEnergyOutput(r_shutDownCarbon);
			} catch (NotEnoughCarbonOutputException e) {
				e.printStackTrace();
			}
		}

		double r_investCarbon = carbonDifference * optimal.reduce.investFrac;

		// Invest in carbon reduction and absorption
		double[] r_investments = new double[2];
		getAbsorbReduceInvestment(r_investCarbon, r_investments);

		// Invest in carbon absorption
		if (r_investments[0] != 0) {
			double carbonChange = carbonAbsorptionHandler
					.getCarbonAbsorptionChange(r_investments[0]);
			try {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonChange);
			} catch (NotEnoughLandException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// Invest in carbon reduction
		if (r_investments[1] != 0) {
			double carbonChange = carbonReductionHandler
					.getCarbonOutputChange(r_investments[1]);
			try {
				carbonReductionHandler.investInCarbonReduction(carbonChange);
			} catch (NotEnoughCarbonOutputException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// MAINTAIN PHASE ACTIONS

		// Invest in industry
		double industryPrice = getAvailableToSpend()
				* optimal.maintain.industryFrac;

		double carbonToOffset = energyUsageHandler
				.calculateCarbonIndustryGrowth(industryPrice);

		if (industryPrice != 0) {
			try {
				energyUsageHandler.investInCarbonIndustry(industryPrice);
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		double m_investCarbon = carbonToOffset
				* optimal.maintain.investOffsetFrac;

		// Invest in carbon reduction and absorption
		double[] m_investments = new double[2];
		getAbsorbReduceInvestment(m_investCarbon, m_investments);

		// Invest in carbon absorption
		if (m_investments[0] != 0) {
			double carbonChange = carbonAbsorptionHandler
					.getCarbonAbsorptionChange(m_investments[0]);
			try {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonChange);
			} catch (NotEnoughLandException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// Invest in carbon reduction
		if (m_investments[1] != 0) {
			double carbonChange = carbonReductionHandler
					.getCarbonOutputChange(m_investments[1]);
			try {
				carbonReductionHandler.investInCarbonReduction(carbonChange);
			} catch (NotEnoughCarbonOutputException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// SELL PHASE ACTIONS

		// Our current total carbon output, including any offsets
		double totalCarbonOutput = getCarbonOutput() - getCarbonOffset()
				- getCarbonAbsorption();

		// Shut down factories
		double shutDownCarbonReduction = totalCarbonOutput
				* optimal.sell.shutDownFrac;

		if (shutDownCarbonReduction != 0) {
			try {
				energyUsageHandler.reduceEnergyOutput(shutDownCarbonReduction);
			} catch (NotEnoughCarbonOutputException e) {
				e.printStackTrace();
			}
		}

		// Invest in being clean
		double s_investCarbon = totalCarbonOutput * optimal.sell.investFrac;

		// Invest in carbon reduction and absorption
		double[] s_investments = new double[2];
		getAbsorbReduceInvestment(s_investCarbon, s_investments);

		// Invest in carbon absorption
		if (s_investments[0] != 0) {
			double carbonChange = carbonAbsorptionHandler
					.getCarbonAbsorptionChange(s_investments[0]);
			try {
				carbonAbsorptionHandler.investInCarbonAbsorption(carbonChange);
			} catch (NotEnoughLandException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// Invest in carbon reduction
		if (s_investments[1] != 0) {
			double carbonChange = carbonReductionHandler
					.getCarbonOutputChange(s_investments[1]);
			try {
				carbonReductionHandler.investInCarbonReduction(carbonChange);
			} catch (NotEnoughCarbonOutputException e) {
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				e.printStackTrace();
			}
		}

		// Calculate new sell price and next years expected emissions targets

		// Sell additional offset over next years target
		double newCarbonOutput = totalCarbonOutput - shutDownCarbonReduction
				- s_investCarbon;
		double carbonBelowTarget = getNextEmissionTarget(getEmissionsTarget())
				- newCarbonOutput;

		sellCarbon = carbonBelowTarget * optimal.sell.shutDownFrac;

		sellCarbonAveragePrice = getMarketSellPrice(sellCarbon) / sellCarbon;
	}

	/**
	 * For a given amount of carbon to reduce, return the amount of money we
	 * should invest in Absorption and Reduction. For safety's sake, will tend
	 * to overestimate a bit.
	 * 
	 * @param carbonReduction
	 *            Amount to reduce carbon by
	 * @param investments
	 *            Pass in a double[2], returned [0] = money to invest in
	 *            Absorption, [1] = money to invest in Reduction
	 * @return Total Cost
	 */
	public double getAbsorbReduceInvestment(double carbonReduction,
			double[] investments) {
		return getAbsorbReduceInvestment(carbonReduction, getArableLandArea(),
				getCarbonOutput(), getEnergyOutput(), investments);
	}

	/**
	 * For a given amount of carbon to reduce, return the amount of money we
	 * should invest in Absorption and Reduction. For safety's sake, will tend
	 * to overestimate a bit.
	 * 
	 * @param carbonReduction
	 *            Amount to reduce carbon by
	 * @param state
	 *            A given country state
	 * @param investments
	 *            Pass in a double[2], returned [0] = money to invest in
	 *            Absorption, [1] = money to invest in Reduction
	 * @return Total Cost
	 */
	public double getAbsorbReduceInvestment(double carbonReduction,
			CountrySimulator.CountryState state, double[] investments) {
		return getAbsorbReduceInvestment(carbonReduction, state.arableLandArea,
				state.carbonOutput, state.energyOutput, investments);
	}

	private double getAbsorbReduceInvestment(double carbonReduction,
			double arableLandArea, double carbonOutput, double energyOutput,
			double[] investments) {

		if (carbonReduction <= 0) {
			investments[0] = 0;
			investments[1] = 0;
			return 0;
		}

		// Overestimate a bit
		carbonReduction *= 1.01;

		double prevCost;
		try {
			prevCost = this.carbonAbsorptionHandler.getInvestmentRequired(
					carbonReduction, arableLandArea);
		} catch (Exception e) {
			e.printStackTrace();
			investments[0] = 0;
			investments[1] = 0;
			return 0;
		}

		double absorbFrac = 0.5;
		double reduceFrac = 0.5;

		double fracDiff = 0.25;

		// Attempt to minimise cost for a given amount of carbon
		for (int i = 0; i < 10; i++) {

			double absorbCost;
			double reduceCost;

			try {
				absorbCost = this.carbonAbsorptionHandler
						.getInvestmentRequired(absorbFrac * carbonReduction,
								arableLandArea);
				reduceCost = this.carbonReductionHandler.getInvestmentRequired(
						reduceFrac * carbonReduction, carbonOutput,
						energyOutput);
			} catch (Exception e) {
				e.printStackTrace();
				investments[0] = 0;
				investments[1] = 0;
				return 0;
			}

			double totalCost = absorbCost + reduceCost;

			if (totalCost < prevCost) {
				reduceFrac += fracDiff;
				absorbFrac -= fracDiff;
			} else {
				reduceFrac -= fracDiff;
				absorbFrac += fracDiff;
			}
			prevCost = totalCost;
			fracDiff /= 2;
		}

		absorbFrac = ((double) Math.round(1000 * absorbFrac)) / 1000;
		reduceFrac = ((double) Math.round(1000 * reduceFrac)) / 1000;

		try {
			if (absorbFrac == 0) {
				investments[0] = 0;
			} else {
				investments[0] = this.carbonAbsorptionHandler
						.getInvestmentRequired(absorbFrac * carbonReduction,
								arableLandArea);
			}

			if (reduceFrac == 0) {
				investments[1] = 0;
			} else {
				investments[1] = this.carbonReductionHandler
						.getInvestmentRequired(reduceFrac * carbonReduction,
								carbonOutput, energyOutput);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return (investments[0] + investments[1]);
	}

	public double getArableLandCost(double absorbInvestment,
			double arableLandArea) {

		if (absorbInvestment <= 0) {
			return 0;
		}

		double carbonAbsorptionChange;
		double cost;

		try {
			carbonAbsorptionChange = carbonAbsorptionHandler
					.getCarbonAbsorptionChange(absorbInvestment, arableLandArea);

			cost = carbonAbsorptionHandler
					.getForestAreaRequired(carbonAbsorptionChange);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return cost;
	}

	public double getCarbonAbsorptionChange(double investmentAmount,
			double arableLandArea) {

		if (investmentAmount <= 0) {
			return 0;
		}

		double change;

		try {
			change = carbonAbsorptionHandler.getCarbonAbsorptionChange(
					investmentAmount, arableLandArea);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return change;
	}

	public double getCarbonReduction(double reductionCost,
			CountrySimulator.CountryState state) {

		if (reductionCost <= 0) {
			return 0;
		}
		double reduction;
		try {
			reduction = carbonReductionHandler.getCarbonOutputChange(
					reductionCost, state.carbonOutput, state.energyOutput);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return reduction;
	}

	private boolean buyingEnabled = true;

	/**
	 * Disables buying
	 */
	private void disableBuying() {
		buyingEnabled = false;
	}

	/**
	 * Reenables buying
	 */
	private void enableBuying() {
		buyingEnabled = true;
	}

	/**
	 * 
	 * @param carbonOffset
	 *            The amount of carbon we want to offset by buying credits
	 * @param year
	 *            The year we want to get the estimated price for. Year = 1 is
	 *            current year, Year = 2 is next year etc.
	 * @return Cost of purchasing credits. Returns a very, very large number if
	 *         not enough credits available.
	 */
	public double getMarketBuyPrice(double carbonOffset) {

		if (carbonOffset == 0) {
			return 0;
		}

		if (buyingEnabled == false) {
			return Double.MAX_VALUE / 1000000;
		}

		return carbonOffset * 500;
	}

	/**
	 * Returns 0 if selling <=0 amounts of carbon offset
	 * 
	 * @param carbonOffset
	 * @param year
	 * @return
	 */
	public double getMarketSellPrice(double carbonOffset) {

		if (carbonOffset <= 0) {
			return 0;
		}

		// TODO calculate a selling price
		return carbonOffset * 100;
	}

	public double getCarbonEnergyIncrease(double industryInvestment) {

		if (industryInvestment <= 0) {
			return 0;
		}

		double increase;

		try {
			increase = energyUsageHandler
					.calculateCarbonIndustryGrowth(industryInvestment);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return increase;
	}

	public double getNextEmissionTarget(double emissionsTarget) {
		// TODO currently returns old emissions target times the 10th root of
		// 0.95
		return emissionsTarget * 0.99488;
	}
}
