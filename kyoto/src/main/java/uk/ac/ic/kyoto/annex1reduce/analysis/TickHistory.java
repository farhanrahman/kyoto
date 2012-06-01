package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.HashSet;
import java.util.Iterator;

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
	
	private HashSet<Message> messages;
	
	private float tradeHigh;
	private float tradeLow;
	private float tradeNo;
	private float tradeSum;
	
	private float investmentHigh;
	private float investmentLow;
	private float investmentNo;
	private float investmentSum;

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
		messages = new HashSet<Message>();
		
		tradeHigh = Float.MIN_VALUE;
		tradeLow = Float.MAX_VALUE;
		tradeNo = 0;
		tradeSum = 0;
		
		investmentHigh = Float.MIN_VALUE;
		investmentLow = Float.MAX_VALUE;
		investmentNo = 0;
		investmentSum = 0;
	}
	
	/**
	 * Calculates unit price, updates High/Low values and stores to Set
	 * @param m
	 * @return 
	 * @throws Exception 
	 */
	public void addMessage(Message m) throws Exception{
		
		int quantity = m.getQuantity();
		float price = m.getPrice();
		
		//TODO What if quantity is zero
		float unitPrice = price/quantity;
		float tempSum = 0;
		
		if (m instanceof TradeMessage){
			
			if (unitPrice > tradeHigh){
				tradeHigh = unitPrice;
			} else if (unitPrice < tradeLow){
				tradeLow = unitPrice;
			}
			
			tradeNo++;
			tradeSum += unitPrice;
			
		}else if (m instanceof InvestmentMessage) {
			
			if (unitPrice > investmentHigh){
				investmentHigh = unitPrice;
			} else if (unitPrice < investmentLow){
				investmentLow = unitPrice;
			}
			
			investmentNo++;
			investmentSum += unitPrice;
			
		}else{
			//TODO Should be be able to accept Message objects?
			throw new Exception("Input arg must extend Message");
		}
		
		messages.add(m);
	}
	
	public Iterator<Message> getMessages() {
		return messages.iterator();
	}

	public float getTradeHigh() {
		return tradeHigh;
	}

	public float getTradeLow() {
		return tradeLow;
	}

	public float getInvestmentHigh() {
		return investmentHigh;
	}

	public float getInvestmentLow() {
		return investmentLow;
	}

	public long getTickId() {
		return tickId;
	}

	public float getTradeAverage() {
		return tradeSum/tradeNo;
	}

	public float getInvestmentAverage() {
		return investmentSum/investmentNo;
	}
	
}
