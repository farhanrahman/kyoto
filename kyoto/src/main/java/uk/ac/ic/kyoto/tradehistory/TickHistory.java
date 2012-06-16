package uk.ac.ic.kyoto.tradehistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ic.kyoto.countries.Offer;

/**
 * Stores all messages (trade and investment), and automagically
 * computes high, low & average.
 * 
 * @author cs2309 & ovg109
 */
public class TickHistory{
	
	private final int tickId;
	
	private ArrayList<Offer> messages;
	
	private double tradeHigh;
	private double tradeLow;
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
		messages = new ArrayList<Offer>();
		
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
	public void addMessage(Offer m) throws Exception{
	
		double unitCost = m.getUnitCost();
		
		if (unitCost > tradeHigh){
			tradeHigh = unitCost;
		}
		
		if (unitCost < tradeLow){
			tradeLow = unitCost;
		}
		
		tradeNo++;
		tradeSum += unitCost;
		
		messages.add(m);
	}
	
	public List<Offer> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public double getTradeHigh() {
		return tradeHigh;
	}

	public double getTradeLow() {
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

	public int getTickId() {
		return tickId;
	}

	public float getTradeAverage() {
		return tradeSum/tradeNo;
	}

	public float getInvestmentAverage() {
		throw new UnsupportedOperationException("Method not yet implemented");
		//return investmentSum/investmentNo;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}
		
		if (!(obj instanceof TickHistory)){
			return false;
		}
		
		TickHistory tick = (TickHistory) obj;
		boolean investmentTest = (tick.investmentHigh == this.investmentHigh) &&
									(tick.investmentLow == this.investmentLow) &&
									(tick.investmentNo == this.investmentNo) &&
									(tick.investmentSum == this.investmentSum);
		boolean tradeTest = (tick.tradeHigh == this.tradeHigh) &&
								(tick.tradeLow == this.tradeLow) &&
								(tick.tradeNo == this.tradeNo) &&
								(tick.tradeSum == this.tradeSum);
		boolean messagesTest = tick.messages.equals(this.messages);
		
		return investmentTest && tradeTest && messagesTest && (tick.tickId == this.tickId);
	}
	
	@Override
	public int hashCode() {
		double result = 73;
		
		result = 57 * result + this.investmentHigh;
		result = 57 * result + this.investmentLow;
		result = 57 * result + this.investmentNo;
		result = 57 * result + this.investmentSum;
		
		result = 57 * result + this.tradeHigh;
		result = 57 * result + this.tradeLow;
		result = 57 * result + this.tradeNo;
		result = 57 * result + this.tradeSum;
		
		result = 57 * result + this.tickId;
		
		result = 57 * result + this.messages.hashCode();
		
		return (int) result;
	}
	
}
