package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class AnalysisUtils {
	
	private AnalysisUtils() {	
		
	}
	
	enum TradeType {
		TRADE, INVESTMENT
	}
	
	/**
	 * Returns a Range object with the high and low values for the range<br/>
	 * endTick &#8805; Range &#8804; startTick
	 * @param startTick
	 * @param endTick
	 * @param sessions
	 * @param type
	 * @return
	 */
	public final static Range range(int startTick, int endTick, SessionHistory[] sessions, TradeType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		long low = Long.MAX_VALUE;
		long high = Long.MIN_VALUE;
		
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		//System.out.print("Size before: " + history.size());
		
		history = history.headMap(startTick+1);
		history = history.tailMap(endTick);
		
		//System.out.println(" after: " + history.size());
		
		for (Entry<Integer, TickHistory> tickEntry : history.entrySet()) {
			TickHistory tick = tickEntry.getValue();
			
			//System.out.println(tickEntry.getKey());
			
			if (type == TradeType.TRADE) {
				if (tick.getTradeHigh() > high) {
					high = tick.getTradeHigh();
				}
				
				if (tick.getTradeLow() < low) {
					low = tick.getTradeLow();
				}
			}else if (type == TradeType.INVESTMENT) {
				if (tick.getInvestmentHigh() > high) {
					high = tick.getInvestmentHigh();
				}
				
				if (tick.getInvestmentLow() < low) {
					low = tick.getInvestmentLow();
				}
			}
		}
		
		return new Range(type, startTick, endTick, low, high);
	}
	
	public final static float weightedAverage(SessionHistory[] sessions, Weighting[] weightings, TradeType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		SortedMap<Integer, Float> weightedHistory = new TreeMap<Integer, Float>();
		
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		// Pull all session histories into a single history
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		// Weight tick averages
		for (Weighting w : weightings) {
			SortedMap<Integer, TickHistory> tempHistory = new TreeMap<Integer, TickHistory>();
			tempHistory.putAll(history);
			tempHistory = tempHistory.headMap(w.startTick+1);
			tempHistory = tempHistory.tailMap(w.endTick);
			
			for ( Entry<Integer, TickHistory> e  : tempHistory.entrySet()) {
				if (type == TradeType.TRADE) {
					weightedHistory.put(e.getKey(), (e.getValue().getTradeAverage() * w.weight));
				}else if (type == TradeType.INVESTMENT) {
					weightedHistory.put(e.getKey(), (e.getValue().getInvestmentAverage() * w.weight));
				}
			}
		}
		
		for (Entry<Integer, Float> tickEntry : weightedHistory.entrySet()) {
			sumOfTrades += tickEntry.getValue();
			numberOfTrades++;
		}
		
		return sumOfTrades/numberOfTrades;
	}
	
	public final static float average(SessionHistory[] sessions, TradeType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		// Pull all session histories into a single history
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		// Weight tick averages
		for (Entry<Integer, TickHistory> ticks : history.entrySet()) {
			
			if (type == TradeType.TRADE){
				sumOfTrades += ticks.getValue().getTradeAverage();
			}else if (type == TradeType.INVESTMENT){
				sumOfTrades += ticks.getValue().getInvestmentAverage();
			}
			
			numberOfTrades++;
		}
		
		return sumOfTrades/numberOfTrades;
	}

	public final static float average(SessionHistory session, TradeType type){
		SessionHistory[] s = {session};
		return average(s, type);
	}
	
	public final static float average(SessionHistory session, int startTick, int endTick, TradeType type){
		Weighting[] w = {new Weighting(startTick, endTick, 1)};
		SessionHistory[] s = {session};
		return weightedAverage(s, w, type);
	}
	
	public final static float average(SessionHistory[] sessions, int startTick, int endTick, TradeType type){
		Weighting[] w = {new Weighting(startTick, endTick, 1)};
		return weightedAverage(sessions, w, type);
	}
	
	public final static double stardardDeviation(SessionHistory[] sessions, TradeType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		SortedMap<Integer, Float> m = new TreeMap<Integer, Float>();
		
		float sumOfVars = 0;
		int numberOfVars = 0;
		
		float variance;
		
		// Pull all session histories into a single history
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		float average = average(sessions, type);
		
		for (Entry<Integer, TickHistory> e : history.entrySet()) {
			if (type == TradeType.TRADE) {
				m.put(e.getKey(), (e.getValue().getTradeAverage() - average));
			}else if (type == TradeType.INVESTMENT) {
				m.put(e.getKey(), (e.getValue().getInvestmentAverage() - average));
			}
		}
		
		for (Entry<Integer, Float> e : m.entrySet()) {
			sumOfVars += (e.getValue() * e.getValue());
			numberOfVars++;
		}
		
		variance = sumOfVars/numberOfVars;
		
		return Math.sqrt(variance);		
	}
	
	public static class Range{
		public final TradeType type;
		public final int startTick;
		public final int endTick;
		public final long low;
		public final long high;
		
		public Range(TradeType type, int startTick, int endTick, long low, long high) {
			this.type = type;
			this.startTick = startTick;
			this.endTick = endTick;
			this.low = low;
			this.high = high;
		}
		
		//TODO Check startTick > endTick
	}
	
	public static class Weighting{
		public final int startTick;
		public final int endTick;
		public final float weight;
		
		public Weighting(int startTick, int endTick, float weight) {
			this.startTick = startTick;
			this.endTick = endTick;
			this.weight = weight;
		}
		
		//TODO Check startTick > endTick
	}

}
