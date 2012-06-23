package uk.ac.ic.kyoto.annex1reduce;

import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.ic.kyoto.tradehistory.OfferHistory;
import uk.ac.imperial.presage2.core.simulator.SimTime;

public class MarketData {

	private OfferHistory offerHistory;

	final private static int TRADE_DATA_TICKS = 10;
	
	/**
	 * Struct with info about how much other people are buying for
	 */
	private BuyStruct[] buyArray = new BuyStruct[TRADE_DATA_TICKS];
	
	/**
	 * Struct with info about how much other people are selling for
	 */
	private SellStruct[] sellArray = new SellStruct[TRADE_DATA_TICKS];

	public MarketData() {
		offerHistory = new OfferHistory();

		for (int i = 0; i < TRADE_DATA_TICKS; i++) {
			buyArray[i] = new BuyStruct(0, 0, 0);
			sellArray[i] = new SellStruct(999999999, 999999999, 999999999);
		}
	}

	private void addBuyStruct(BuyStruct buy) {
		for (int i = 0; i < TRADE_DATA_TICKS - 1; i++) {
			buyArray[i] = buyArray[i + 1];
		}
		buyArray[TRADE_DATA_TICKS - 1] = buy;
	}

	private void addSellStruct(SellStruct sell) {
		for (int i = 0; i < TRADE_DATA_TICKS - 1; i++) {
			sellArray[i] = sellArray[i + 1];
		}
		sellArray[TRADE_DATA_TICKS - 1] = sell;
	}

	/**
	 * Called once per tick. Will look at the previous ticks buying and selling
	 * prices and recalculate a new price that we can effectively buy and sell
	 * at. These can be grabbed using the getters.
	 */
	public void update() {

		// Grab data from the last tick
		Map<UUID, OfferMessage> lastTickHistory = offerHistory
				.getHistoryForTime(SimTime.get().intValue() - 1);

		if (lastTickHistory == null) {
			return;
		}

		int numBuys = 0;
		double minimumBuy = Double.MAX_VALUE;
		double averageBuy = 0;
		double maximumBuy = 0;

		int numSells = 0;
		double minimumSell = Double.MAX_VALUE;
		double averageSell = 0;
		double maximumSell = 0;

		// Iterate through all the map values
		for (Map.Entry<UUID, OfferMessage> entry : lastTickHistory.entrySet()) {

			TradeType type = entry.getValue().getOfferType();

			double price = entry.getValue().getOfferUnitCost();

			// If the offer is to buy credits from us
			if (type == TradeType.BUY) {
				if (minimumBuy > price) {
					minimumBuy = price;
				}
				if (maximumBuy < price) {
					maximumBuy = price;
				}
				averageBuy += price;
				numBuys++;
			}

			// If the offer is to sell credits or CDM to us
			else if (type == TradeType.SELL || type == TradeType.INVEST) {
				if (minimumSell > price) {
					minimumSell = price;
				}
				if (maximumSell < price) {
					maximumSell = price;
				}
				averageSell += price;
				numSells++;
			}
		}

		if (numBuys != 0) {
			averageBuy /= numBuys;
			addBuyStruct(new BuyStruct(minimumBuy, averageBuy, maximumBuy));
			calculateBuyingPrice();
		}

		if (numSells != 0) {
			averageSell /= numSells;
			addSellStruct(new SellStruct(minimumSell, averageSell, maximumSell));
			calculateSellingPrice();
		}

		/*
		 * If the price we can buy at is lower than the price we can sell at,
		 * something has gone horribly wrong (as it means people are buying at a
		 * price higher than the price things are being sold at).
		 * 
		 * Lets exploit these poor souls. Raise the price we can sell at to the
		 * price we can buy at
		 */

		if (sellingPrice > buyingPrice) {
			sellingPrice = buyingPrice;
		}

	}

	/**
	 * The price WE can sell at. Calculated depending on what others want to buy
	 * at. The higher the better.
	 */
	private double sellingPrice = 0;

	/**
	 * The price WE can buy at. Calculated depending on what other want to sell
	 * at. The lower the better.
	 */
	private double buyingPrice = 10000000000000.0;

	/**
	 * Calculate price WE can sell at, according to what others want to buy at.
	 * The higher the better, so go at an average of the most recent maximum buy
	 * offers from OTHER people
	 */
	private void calculateSellingPrice() {
		double averageSell = 0;
		for (int i = 0; i < TRADE_DATA_TICKS; i++) {
			averageSell += buyArray[i].maximumBuyUnitPrice;
		}
		sellingPrice = averageSell / TRADE_DATA_TICKS;
	}

	/**
	 * Calculate price WE can buy at, according to what others want to sell at.
	 * The lower the better, so minimum of selling prices last turn from OTHER
	 * people
	 */
	private void calculateBuyingPrice() {
		double averageBuy = 0;
		for (int i = 0; i < TRADE_DATA_TICKS; i++) {
			averageBuy += sellArray[i].minimumSellUnitPrice;
		}
		buyingPrice = averageBuy / TRADE_DATA_TICKS;
	}

	/**
	 * Get the price we can sell at
	 */
	public double getSellingPrice() {
		return sellingPrice;
	}

	/**
	 * Get the price we can buy at
	 */
	public double getBuyingPrice() {
		return buyingPrice;
	}

	@SuppressWarnings("unused")
	private static class BuyStruct {
		final double minimumBuyUnitPrice;
		final double averageBuyUnitPrice;
		final double maximumBuyUnitPrice;

		private BuyStruct(double minB, double aveB, double maxB) {
			minimumBuyUnitPrice = minB;
			averageBuyUnitPrice = aveB;
			maximumBuyUnitPrice = maxB;
		}
	}

	@SuppressWarnings("unused")
	private static class SellStruct {
		final double minimumSellUnitPrice;
		final double averageSellUnitPrice;
		final double maximumSellUnitPrice;

		private SellStruct(double minS, double aveS, double maxS) {
			minimumSellUnitPrice = minS;
			averageSellUnitPrice = aveS;
			maximumSellUnitPrice = maxS;
		}
	}

}
