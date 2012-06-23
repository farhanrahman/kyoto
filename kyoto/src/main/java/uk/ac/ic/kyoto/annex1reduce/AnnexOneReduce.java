package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;
import uk.ac.ic.kyoto.annex1reduce.CountrySimulator.ActionList;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.IsolatedAbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * 
 * @author Nik
 * 
 */
public class AnnexOneReduce extends AbstractCountry {
	final private CountrySimulator simulator;

	public AnnexOneReduce(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate,
				energyOutput, carbonOutput);
		simulator = new CountrySimulator(this);
	}

	MarketData marketData;

	@Override
	public void initialiseCountry() {
		marketData = new MarketData();
	}

	@Override
	protected void yearlyFunction() {
		needToSimulate = true;
	}

	@Override
	protected void sessionFunction() {
	}

	private boolean needToSimulate = true;

	/**
	 * The amount of carbon we want to buy at the current (or lower) price
	 */
	private double buyCarbonQuantity = 0;

	/**
	 * Average cost of each carbon credit we want to buy
	 */
	private double buyCarbonUnitPrice = 0;

	/**
	 * Amount of carbon we want to sell
	 */
	private double sellCarbonQuantity = 0;

	/**
	 * Average cost of the carbon we want to sell
	 */
	private double sellCarbonUnitPrice = 1000000000;

	@Override
	protected void behaviour() {

		// TODO fix this Update our market buy and sell price information
		// marketData.update();

		// If the expected buy price has increased by more than 5%, we should
		// resimulate
		if (1.05 * buyCarbonUnitPrice < marketData.getBuyingPrice()) {
			needToSimulate = true;
		}

		// If our expected sell price has decreed by more than 5%, we should
		// resimulate
		if (0.95 * sellCarbonUnitPrice > marketData.getSellingPrice()) {
			needToSimulate = true;
		}

		int currentTickInYear = timeService.getCurrentTick()
				% timeService.getTicksInYear();

		// If we're right at the end of a year, perform all the actions.
		// Will also recalculate the buying amounts
		if (currentTickInYear == timeService.getTicksInYear() - 6) {
			performReduceMaintainActions();
		} else if (needToSimulate) {
			runSimulation();
		}

		/*
		 * Now, send out buy and sell offers for the tick
		 */
		if (buyCarbonQuantity > 100) {
			broadcastSellOffer(buyCarbonQuantity / 2, buyCarbonUnitPrice);
		} else if (sellCarbonQuantity > 100) {
			broadcastBuyOffer(sellCarbonQuantity / 2, sellCarbonUnitPrice);
		}
	}

	/**
	 * Runs the simulation with current country information Returns the amount
	 * of carbon we want to buy and sell, and sets the new buy and sell price
	 */
	public ActionList runSimulation() {

		needToSimulate = false;

		ActionList optimal = simulator.simulate(getCarbonOutput(),
				getEnergyOutput(), getPrevEnergyOutput(), getCarbonOffset(),
				getCarbonAbsorption(), getEmissionsTarget(),
				getAvailableToSpend(), getGDP(), getGDPRate(),
				getArableLandArea(), getYearsUntilSanctions());

		double carbonDifference = this.getCarbonOffset()
				+ this.getCarbonAbsorption() - this.getCarbonOffset();

		// The amount of carbon we want to buy at the current price
		buyCarbonQuantity = carbonDifference * optimal.reduce.buyCreditFrac;

		double r_investCarbon = carbonDifference * optimal.reduce.investFrac;
		double[] r_investments = new double[2];
		double absorbReduceCost = getAbsorbReduceInvestment(r_investCarbon,
				r_investments);
		double creditCost = getMarketBuyPrice(buyCarbonQuantity);

		double estimatedMoney = getAvailableToSpend() - absorbReduceCost
				- creditCost;

		// The cost of investing in industry
		double industryInvest = estimatedMoney * optimal.maintain.industryFrac;

		double carbonIncrease = energyUsageHandler
				.calculateCarbonIndustryGrowth(industryInvest);

		buyCarbonQuantity += carbonIncrease
				* optimal.maintain.buyCreditOffsetFrac;

		buyCarbonUnitPrice = marketData.getBuyingPrice();

		if (optimal.sell.sellFrac > 0) {
			double nextEmissionsTarget = getNextEmissionTarget(getEmissionsTarget());
			double carbonBelowTarget = nextEmissionsTarget
					* (optimal.sell.investFrac + optimal.sell.shutDownFrac);

			sellCarbonQuantity = optimal.sell.sellFrac * carbonBelowTarget;
		} else {
			sellCarbonQuantity = 0;
		}

		sellCarbonUnitPrice = marketData.getSellingPrice();

		return optimal;
	}

	/**
	 * Respond to any broadcasted trades
	 */
	@Override
	protected void processInput(Input input) {

		try {
			if (this.tradeProtocol.canHandle(input)) {
				this.tradeProtocol.handle(input);
			} else {
				OfferMessage offer = this.tradeProtocol.decodeInput(input);
				NetworkAddress address = this.tradeProtocol
						.extractNetworkAddress(input);

				TradeType type = offer.getOfferType();

				double quantity = offer.getOfferQuantity();
				double averagePrice = offer.getOfferUnitCost();

				// If the offer is to buy credits from us
				if (type == TradeType.BUY) {
					if (averagePrice >= 0.975 * sellCarbonUnitPrice) {
						double amountToSell = Math.min(sellCarbonQuantity,
								quantity);
						this.tradeProtocol.respondToOffer(address,
								amountToSell, offer);
					}
				}
				// If the offer is to sell credits or CDM to us
				else if (type == TradeType.SELL || type == TradeType.INVEST) {
					if (averagePrice <= 1.025 * buyCarbonUnitPrice) {
						double amountToBuy = Math.min(buyCarbonQuantity,
								quantity);
						this.tradeProtocol.respondToOffer(address, amountToBuy,
								offer);
					}
				}
			}
		} catch (Exception e) {
			logger.warn(getName()
					+ ": Problem with investments after successful trade: "
					+ e.getMessage());
		}
	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer offer) {
		/*
		 * Accept trades from all people only if: The amount of carbon we want
		 * to sell is greater than or equal to the offer we get sent back to us
		 * AND the price is greater than or equal to the average sell price
		 * 
		 * If we accept the offer, we should decrease buyCarbon by the amount of
		 * carbon we sold.
		 * 
		 * If we accept the offer, we should set needToSimulate as true so we
		 * can resimulate and recalculate buy and sell amounts
		 * 
		 * If we reject the offer, don't do anything.
		 */

		TradeType type = offer.getType();

		double quantity = offer.getQuantity();

		double averagePrice = offer.getUnitCost();

		if (type == TradeType.BUY || type == TradeType.INVEST) {
			if (averagePrice <= buyCarbonUnitPrice) {
				if (quantity <= buyCarbonQuantity) {
					return true;
				} else {
					return false;
				}
			}
		} else if (type == TradeType.SELL) {
			if (averagePrice >= sellCarbonUnitPrice) {
				if (quantity <= sellCarbonQuantity) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	protected void tradeWasSuccessful(NetworkAddress from, OfferMessage offer) {

		TradeType type = offer.getOfferType();

		double quantity = offer.getOfferQuantity();

		if (type == TradeType.BUY || type == TradeType.INVEST) {
			buyCarbonQuantity -= quantity;
		} else if (type == TradeType.SELL) {
			sellCarbonQuantity -= quantity;
		}

		// Recalculate everything
		needToSimulate = true;
	}

	private boolean buyingEnabled = true;

	/**
	 * 
	 * @param carbonOffset
	 *            The amount of carbon we want to offset by buying credits
	 * @return Estimated cost of purchasing credits
	 */
	public double getMarketBuyPrice(double carbonOffset) {

		if (carbonOffset == 0) {
			return 0;
		}

		// Return an obscenely high buying price
		if (buyingEnabled == false) {
			return Double.MAX_VALUE / 100000000000.0;
		}

		return carbonOffset * marketData.getBuyingPrice();
	}

	private void disableBuying() {
		buyingEnabled = false;
	}

	private void enableBuying() {
		buyingEnabled = true;
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

		return carbonOffset * marketData.getSellingPrice();
	}

	// TODO get years until sanctions (when carbon credits reset)
	private int getYearsUntilSanctions() {
		 int years = timeService.getCurrentYear()
		 % GameConst.getYearsInSession();
		 years = GameConst.getYearsInSession() - years;
		 return years;
	}

	/**
	 * Called at the end of the year, after all trades have been completed. Will
	 * recalculate optimal path and perform it.
	 */
	public void performReduceMaintainActions() {

		// Assume all buys have been completed, work out new optimal path
		disableBuying();

		ActionList optimal = runSimulation();

		// Reenable buying
		enableBuying();

		// REDUCE PHASE ACTIONS

		double r_netCarbonOutput = this.getCarbonOutput()
				- this.getCarbonAbsorption() - this.getCarbonOffset();

		// Positive carbon difference means we need to reduce our carbon
		double r_carbonDifference = 1.03 * (r_netCarbonOutput - this
				.getEmissionsTarget());

		// If we have carbon to offset
		if (r_carbonDifference > 0) {

			// Shut down factories
			double r_shutDownCarbon = r_carbonDifference
					* optimal.reduce.shutDownFrac;

			if (r_shutDownCarbon > 0) {
				try {
					energyUsageHandler.reduceEnergyOutput(r_shutDownCarbon);
				} catch (NotEnoughCarbonOutputException e) {
					logger.warn(e);
				}
			}

			// Calculate ratio of absorption to reduction
			double r_investCarbon = r_carbonDifference
					* optimal.reduce.investFrac;
			double[] r_carbon = getAbsorbReduceCarbon(r_investCarbon);

			// Invest in carbon absorption
			if (r_carbon[0] > 0) {
				try {
					carbonAbsorptionHandler
							.investInCarbonAbsorption(r_carbon[0]);
				} catch (NotEnoughLandException e) {
					logger.warn(e);
				} catch (NotEnoughCashException e) {
					logger.warn(e);
				}
			}

			// Invest in carbon reduction
			if (r_carbon[1] > 0) {
				try {
					carbonReductionHandler.investInCarbonReduction(r_carbon[1]);
				} catch (NotEnoughCarbonOutputException e) {
					logger.warn(e);
				} catch (NotEnoughCashException e) {
					logger.warn(e);
				}
			}
		}

		// MAINTAIN PHASE ACTIONS

		// Calculate amount we want to invest in industry
		double m_industryPrice = getAvailableToSpend()
				* optimal.maintain.industryFrac;

		// Invest in industry if we need to
		if (m_industryPrice > 0) {

			try {
				energyUsageHandler.investInCarbonIndustry(m_industryPrice);
			} catch (NotEnoughCashException e) {
				logger.warn(e);
			}

			double m_netCarbonOutput = this.getCarbonOutput()
					- this.getCarbonAbsorption() - this.getCarbonOffset();

			// Positive carbon difference means we need to reduce our carbon
			double m_carbonDifference = 1.03 * (m_netCarbonOutput - this
					.getEmissionsTarget());

			// If we need to reduce our carbon further
			if (m_carbonDifference > 0) {
				// Calculate ratio of absorption to reduction
				double[] m_carbon = getAbsorbReduceCarbon(m_carbonDifference);

				// Invest in carbon absorption
				if (m_carbon[0] > 0) {
					try {
						carbonAbsorptionHandler
								.investInCarbonAbsorption(m_carbon[0]);
					} catch (NotEnoughLandException e) {
						logger.warn(e);
					} catch (NotEnoughCashException e) {
						logger.warn(e);
					}
				}

				// Invest in carbon reduction
				if (m_carbon[1] > 0) {
					try {
						carbonReductionHandler
								.investInCarbonReduction(m_carbon[1]);
					} catch (NotEnoughCarbonOutputException e) {
						logger.warn(e);
					} catch (NotEnoughCashException e) {
						logger.warn(e);
					}
				}
			}
		}

		// SELL PHASE ACTIONS

		// Our current net carbon output
		double s_netCarbonOutput = this.getCarbonOutput()
				- this.getCarbonAbsorption() - this.getCarbonOffset();

		// Shut down factories
		double s_shutDownCarbon = s_netCarbonOutput * optimal.sell.shutDownFrac;

		if (s_shutDownCarbon > 0) {
			try {
				energyUsageHandler.reduceEnergyOutput(s_shutDownCarbon);
			} catch (NotEnoughCarbonOutputException e) {
				logger.warn(e);
			}
		}

		// Invest in being clean
		double s_investCarbon = s_netCarbonOutput * optimal.sell.investFrac;

		if (s_investCarbon > 0) {

			// Calculate ratio of absorption to reduction
			double[] s_carbon = getAbsorbReduceCarbon(s_investCarbon);

			// Invest in carbon absorption
			if (s_carbon[0] > 0) {
				try {
					carbonAbsorptionHandler
							.investInCarbonAbsorption(s_carbon[0]);
				} catch (NotEnoughLandException e) {
					logger.warn(e);
				} catch (NotEnoughCashException e) {
					logger.warn(e);
				}
			}

			// Invest in carbon reduction
			if (s_carbon[1] > 0) {
				try {
					carbonReductionHandler.investInCarbonReduction(s_carbon[1]);
				} catch (NotEnoughCarbonOutputException e) {
					logger.warn(e);
				} catch (NotEnoughCashException e) {
					logger.warn(e);
				}
			}
		}
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

		double prevCost = this.carbonReductionHandler.getInvestmentRequired(
				carbonReduction, carbonOutput, energyOutput);

		double absorbFrac = 0.5;
		double reduceFrac = 0.5;

		double fracDiff = 0.25;

		double absorbCost = 9999999999999999.0;
		double reduceCost = 9999999999999999.0;

		// Attempt to minimise cost for a given amount of carbon
		for (int i = 0; i < 20; i++) {

			try {
				absorbCost = this.carbonAbsorptionHandler
						.getInvestmentRequired(absorbFrac * carbonReduction,
								arableLandArea);
			} catch (NotEnoughLandException e) {
				absorbCost = 99999999999999999999999.0;
			}

			reduceCost = this.carbonReductionHandler.getInvestmentRequired(
					reduceFrac * carbonReduction, carbonOutput, energyOutput);

			double totalCost = absorbCost + reduceCost;

			if (totalCost > prevCost) {
				reduceFrac += fracDiff;
				absorbFrac -= fracDiff;
			} else {
				reduceFrac -= fracDiff;
				absorbFrac += fracDiff;
			}
			prevCost = totalCost;
			fracDiff /= 2;
		}

		absorbCost = (double) Math.round(absorbCost);
		reduceCost = (double) Math.round(reduceCost);

		investments[0] = absorbCost;
		investments[1] = reduceCost;

		return (investments[0] + investments[1]);
	}

	/**
	 * Returns the amount of carbon we should put into absorption and reduction
	 * 
	 * @param carbonReduction
	 *            The amount of carbon we want to reduce
	 * @return double[0] = absorb carbon, double[1] = reduce carbon
	 */
	private double[] getAbsorbReduceCarbon(double carbonReduction) {

		double[] carbon = new double[2];

		double arableLandArea = getArableLandArea();
		double carbonOutput = getCarbonOutput();
		double energyOutput = getEnergyOutput();

		if (carbonReduction <= 0) {
			carbon[0] = 0;
			carbon[1] = 0;
			return carbon;
		}

		// Overestimate a bit
		carbonReduction *= 1.01;

		double prevCost = this.carbonReductionHandler.getInvestmentRequired(
				carbonReduction, carbonOutput, energyOutput);

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
			} catch (NotEnoughLandException e) {
				absorbCost = 99999999999999999999999.0;
			}

			reduceCost = this.carbonReductionHandler.getInvestmentRequired(
					reduceFrac * carbonReduction, carbonOutput, energyOutput);

			double totalCost = absorbCost + reduceCost;

			if (totalCost > prevCost) {
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

		carbon[0] = absorbFrac * carbonReduction;
		carbon[1] = reduceFrac * carbonReduction;

		return carbon;
	}

	/**
	 * Returns the amount of arable land taken up by invest a certain amount of
	 * money into absorption
	 * 
	 * @param absorbInvestment
	 * @param arableLandArea
	 * @return
	 */
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
		} catch (NotEnoughLandException e) {
			return 999999999999999999.0;
		}

		return cost;
	}

	/**
	 * Returns the increase in carbon absorption by investing a certain amount
	 * of money into absorption
	 * 
	 * @param investmentAmount
	 * @param arableLandArea
	 * @return
	 */
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
			return 0;
		}

		return change;
	}

	/**
	 * Returns the reduction in carbon output caused by investing in carbon
	 * reduction
	 * 
	 * @param reductionCost
	 * @param state
	 * @return
	 */
	public double getCarbonReduction(double reductionCost,
			CountrySimulator.CountryState state) {

		if (reductionCost <= 0) {
			return 0;
		}
		return carbonReductionHandler.getCarbonOutputChange(reductionCost,
				state.carbonOutput, state.energyOutput);
	}

	public double getCarbonEnergyIncrease(double industryInvestment) {

		if (industryInvestment <= 0) {
			return 0;
		}

		return energyUsageHandler
				.calculateCarbonIndustryGrowth(industryInvestment);
	}

	public double getNextEmissionTarget(double emissionsTarget) {

		// TODO currently returns old emissions target times the 10th root of
		// 0.95
		return emissionsTarget * 0.99488;
	}
}
