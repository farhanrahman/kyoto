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
	 * 
	 * @param session
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public final static float sessionAverage(SessionHistory session, TradeType type){
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		for (Entry<Integer, TickHistory> ticks : session.getSession().entrySet()) {
			
			if (type == TradeType.TRADE){
				sumOfTrades += ticks.getValue().getTradeAverage();
			}else if (type == TradeType.INVESTMENT){
				sumOfTrades += ticks.getValue().getInvestmentAverage();
			}
			
			numberOfTrades++;
		}
		
		return sumOfTrades/numberOfTrades;
	}
	
	/**
	 * 
	 * @param N
	 * @param startTick
	 * @param sessions
	 * @param type
	 * @return
	 */
	public final static Range nPointRange(int N, int startTick, SessionHistory[] sessions, TradeType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		long low = Long.MAX_VALUE;
		long high = Long.MIN_VALUE;
		
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		System.out.print("Size before: " + history.size());
		
		history = history.headMap(startTick);
		
		System.out.println(" after: " + history.size());
		
		int topKey =history.lastKey();
		
		for (int i = 0; i < N; i++) {
			TickHistory temp = history.get(history.lastKey());
			
			if (type == TradeType.TRADE) {
				if (temp.getTradeHigh() > high) {
					high = temp.getTradeHigh();
				}
				
				if (temp.getTradeLow() < low) {
					low = temp.getTradeLow();
				}
			}else if (type == TradeType.INVESTMENT) {
				if (temp.getInvestmentHigh() > high) {
					high = temp.getInvestmentHigh();
				}
				
				if (temp.getInvestmentLow() < low) {
					low = temp.getInvestmentLow();
				}
			}
			
			System.out.println("Processing tick " + (topKey-i));
			
			history.remove(history.lastKey());
		}
		
		return new Range(type, N, low, high);
	}
	
	enum StdDevType {
		POPULATION, SAMPLE
	}
	
	public static class Range{
		public final TradeType type;
		public final int N;
		public final long low;
		public final long high;
		
		public Range(TradeType type, int N, long low, long high) {
			this.type = type;
			this.N = N;
			this.low = low;
			this.high = high;
		}
	}

}
