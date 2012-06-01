package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ic.kyoto.trade.TradeMessage;
import uk.ac.imperial.presage2.core.network.Message;

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
	
	//TODO Message object does not yet exist. Needs to be specified.
	private HashSet<Message> messages;
	private float tradeHigh;
	private float tradeLow;
	private float tradeAverage;
	private float investmentHigh;
	private float investmentLow;
	private float investmentAverage;

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
		investmentHigh = Float.MIN_VALUE;
		investmentLow = Float.MAX_VALUE;
		tradeLow = Float.MAX_VALUE;
	}
	
	/**
	 * Calculates unit price, updates High/Low values and stores to Set
	 * @param m
	 */
	public void addMessage(Message m){
		
		int quantity = m.getQuantity();
		float price = m.getPrice();
		
		//TODO What if price is zero
		float unitPrice = quantity/price;
		float tempSum = 0;
		
		if (m instanceof TradeMessage){
			
			if (unitPrice > tradeHigh){
				tradeHigh = unitPrice;
			} else if (unitPrice < tradeLow){
				tradeLow = unitPrice;
			}
			
			//TODO Make more efficient
			for (Message m1 : messages) {
				tempSum += (m1.getQuantity())/(m1.getPrice());
			}
			
			tradeAverage = tempSum/messages.size();
			
		}else if (m instanceof InvestmentMessage) {
			
			if (unitPrice > investmentHigh){
				investmentHigh = unitPrice;
			} else if (unitPrice < investmentLow){
				investmentLow = unitPrice;
			}
			
			//TODO Make more efficient
			for (Message m1 : messages) {
				tempSum += (m1.getQuantity())/(m1.getPrice());
			}
			
			investmentAverage = tempSum/messages.size();
			
		}else{
			throw new Exception("Jizz all up my arse");
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
		return tradeAverage;
	}

	public float getInvestmentAverage() {
		return investmentAverage;
	}
	
}
