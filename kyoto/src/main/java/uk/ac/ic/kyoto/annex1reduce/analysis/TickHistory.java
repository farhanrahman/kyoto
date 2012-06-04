package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;

/**
 * Stores all messages (trade and investment), and automagically
 * computes high, low & average.<br>
 * <br>
 * <i>NOTE: This is based on a yet to be implemented TradeMessage and
 * InvestmentMessage classes (which should extend Message).</i>
 * 
 * @author cs2309 & ovg109
 */
public class TickHistory{
	
	private final int tickId;
	
	private ArrayList<Trade> messages;
	
	private int tradeHigh;
	private int tradeLow;
	private int tradeNo;
	private int tradeSum;
	
	private int investmentHigh;
	private int investmentLow;
	private int investmentNo;
	private int investmentSum;

	/**
	 * Constructs a TickHistory with ID of currentTick
	 * @param currentTick
	 */
	public TickHistory(int currentTick) {
		this.tickId = currentTick;
		init();
	}
	
	/**
	 * Creates Set to store all messages.<br>
	 * Sets High/Low to Float.{MIN/MAX}_VALUE
	 */
	private void init(){
		messages = new ArrayList<Trade>();
		
		tradeHigh = Integer.MIN_VALUE;
		tradeLow = Integer.MAX_VALUE;
		tradeNo = 0;
		tradeSum = 0;
		
		investmentHigh = Integer.MIN_VALUE;
		investmentLow = Integer.MAX_VALUE;
		investmentNo = 0;
		investmentSum = 0;
	}
	
	/**
	 * Calculates unit price, updates High/Low values and stores to Set
	 * @param m
	 * @return 
	 * @throws Exception 
	 */
	public void addMessage(Trade m) throws Exception{
	
		int unitCost = m.getUnitCost();
		
		if (unitCost > tradeHigh){
			tradeHigh = unitCost;
		}
		
		if (unitCost < tradeLow){
			tradeLow = unitCost;
		}
		
		tradeNo++;
		tradeSum += unitCost;
		
//		if (m instanceof TradeMessage){
//			
//			if (unitPrice > tradeHigh){
//				tradeHigh = unitPrice;
//			} else if (unitPrice < tradeLow){
//				tradeLow = unitPrice;
//			}
//			
//			tradeNo++;
//			tradeSum += unitPrice;
//			
//		}else if (m instanceof InvestmentMessage) {
//			
//			if (unitPrice > investmentHigh){
//				investmentHigh = unitPrice;
//			} else if (unitPrice < investmentLow){
//				investmentLow = unitPrice;
//			}
//			
//			investmentNo++;
//			investmentSum += unitPrice;
//			
//		}else{
//			//TODO Should be be able to accept Message objects?
//			throw new Exception("Input arg must extend Message");
//		}
		
		messages.add(m);
	}
	
	public Iterator<Trade> getMessages() {
		return messages.iterator();
	}

	public int getTradeHigh() {
		return tradeHigh;
	}

	public int getTradeLow() {
		return tradeLow;
	}

	public int getInvestmentHigh() {
		throw new UnsupportedOperationException("Method not yet implemented");
		//return investmentHigh;
	}

	public int getInvestmentLow() {
		throw new UnsupportedOperationException("Method not yet implemented");
		//return investmentLow;
	}

	public long getTickId() {
		return tickId;
	}

	public float getTradeAverage() {
		return tradeSum/tradeNo;
	}

	public float getInvestmentAverage() {
		throw new UnsupportedOperationException("Method not yet implemented");
		//return investmentSum/investmentNo;
	}
	
}
