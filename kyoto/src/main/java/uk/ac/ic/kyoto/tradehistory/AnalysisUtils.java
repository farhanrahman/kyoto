package uk.ac.ic.kyoto.tradehistory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;

/**
 * 
 * @author cs2309
 */
public class AnalysisUtils {
	
	private AnalysisUtils() {}
	
	//TODO Give enum a better name
	public enum TradeActionType {
		TRADE, INVESTMENT
	}
	
	/**
	 * Returns a Range object with the high and low values for the range: 
	 * endTick &#8805; Range &#8804; startTick
	 * @param startTick
	 * @param endTick
	 * @param sessions
	 * @param type
	 * @return
	 */
	public final static Range range(int startTick, int endTick, SessionHistory[] sessions, TradeActionType type){
		checkTickPreconditions(startTick, endTick);

		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		double low = Double.MAX_VALUE;
		double high = Double.MIN_VALUE;
		
		// Putting all sessions into a single SortedMap
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}

		history = history.headMap(startTick+1);
		history = history.tailMap(endTick);
		
		for (Entry<Integer, TickHistory> tickEntry : history.entrySet()) {
			TickHistory tick = tickEntry.getValue();
						
			if (type == TradeActionType.TRADE) {
				if (tick.getTradeHigh() > high) {
					high = tick.getTradeHigh();
				}
				
				if (tick.getTradeLow() < low) {
					low = tick.getTradeLow();
				}
				
			}else if (type == TradeActionType.INVESTMENT) {
				
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
	
	/**
	 * Calculates the weighted average of multiple sessions.
	 * @param sessions
	 * @param weightings
	 * @param type
	 * @return
	 */
	public final static float weightedAverage(SessionHistory[] sessions, Weighting[] weightings, TradeActionType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		float sumOfTrades = 0;
		float numberOfTrades = 0;
		
		// Putting all sessions into a single SortedMap
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
								
				if (type == TradeActionType.TRADE) {
					sumOfTrades += (e.getValue().getTradeAverage() * w.weight);
				}else if (type == TradeActionType.INVESTMENT) {
					sumOfTrades += (e.getValue().getInvestmentAverage() * w.weight);
				}
				
				numberOfTrades += w.weight;
			}
		}
		
		return sumOfTrades/numberOfTrades;
	}
	
	/**
	 * Calculates the average over an array of session.
	 * @param sessions
	 * @param type
	 * @return
	 */
	public final static float average(SessionHistory[] sessions, TradeActionType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		// Putting all sessions into a single SortedMap
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		for (Entry<Integer, TickHistory> ticks : history.entrySet()) {
			
			if (type == TradeActionType.TRADE){
				sumOfTrades += ticks.getValue().getTradeAverage();
			}else if (type == TradeActionType.INVESTMENT){
				sumOfTrades += ticks.getValue().getInvestmentAverage();
			}
			
			numberOfTrades++;
		}
		
		return sumOfTrades/numberOfTrades;
	}
	
	/**
	 * Calculates the average of a single session
	 * @param session
	 * @param type
	 * @return
	 */
	public final static float average(SessionHistory session, TradeActionType type){
		SessionHistory[] s = {session};
		return average(s, type);
	}
	
	/**
	 * Calculates the average of a single session for the range: endTick &#8805; Range &#8804; startTick
	 * @param session
	 * @param startTick
	 * @param endTick
	 * @param type
	 * @return
	 */
	public final static float average(SessionHistory session, int startTick, int endTick, TradeActionType type){
		checkTickPreconditions(startTick, endTick);
		
		Weighting[] w = {new Weighting(startTick, endTick, 1)};
		SessionHistory[] s = {session};
		return weightedAverage(s, w, type);
	}
	
	/**
	 * Calculates the average of multiple sessions in the range: endTick &#8805; Range &#8804; startTick
	 * @param sessions
	 * @param startTick
	 * @param endTick
	 * @param type
	 * @return
	 */
	public final static float average(SessionHistory[] sessions, int startTick, int endTick, TradeActionType type){
		checkTickPreconditions(startTick, endTick);
		
		Weighting[] w = {new Weighting(startTick, endTick, 1)};
		return weightedAverage(sessions, w, type);
	}
	
	/**
	 * Calculates the standard deviation of multiple sessions.<br/>
	 * This is a Beta function, and so for now will be marked Deprecated.
	 * @param sessions
	 * @param type
	 * @return
	 */
	@Deprecated
	public final static double stardardDeviation(SessionHistory[] sessions, TradeActionType type){
		SortedMap<Integer, TickHistory> history = new TreeMap<Integer, TickHistory>();
		SortedMap<Integer, Double> m = new TreeMap<Integer, Double>();
		
		double sumOfVars = 0;
		int numberOfVars = 0;
		
		double variance;
		
		// Pull all session histories into a single history
		for (SessionHistory s : sessions) {
			history.putAll(s.getSession());
		}
		
		double average = average(sessions, type);
		
		for (Entry<Integer, TickHistory> e : history.entrySet()) {
			if (type == TradeActionType.TRADE) {
				m.put(e.getKey(), (e.getValue().getTradeAverage() - average));
			}else if (type == TradeActionType.INVESTMENT) {
				m.put(e.getKey(), (e.getValue().getInvestmentAverage() - average));
			}
		}
		
		for (Entry<Integer, Double> e : m.entrySet()) {
			sumOfVars += (e.getValue() * e.getValue());
			numberOfVars++;
		}
		
		variance = sumOfVars/numberOfVars;
		
		return Math.sqrt(variance);		
	}
	
	/**
	 * Range object is returned when calling AnalysisUtils.range(...)
	 * @author cs2309
	 *
	 */
	public static class Range{
		public final TradeActionType type;
		public final int startTick;
		public final int endTick;
		public final double low;
		public final double high;
		
		public Range(TradeActionType type, int startTick, int endTick, double low, double high) {
			checkTickPreconditions(startTick, endTick);
			
			this.type = type;
			this.startTick = startTick;
			this.endTick = endTick;
			this.low = low;
			this.high = high;
		}
	}
	
	/**
	 * 
	 * @author cs2309
	 */
	public static class Weighting{
		public final int startTick;
		public final int endTick;
		public final int weight;
		
		/**
		 * NOTE: startTick must be &#62; endTick.<br\>
		 * weight must be a non-negative integer
		 * @param startTick
		 * @param endTick
		 * @param weight
		 */
		public Weighting(int startTick, int endTick, int weight) {
			checkTickPreconditions(startTick, endTick);
			
			if (weight < 0) {
				throw new InvalidParameterException("weight must be a non-negative integer");
			}
			
			this.startTick = startTick;
			this.endTick = endTick;
			this.weight = weight;
		}
	}
	
	/**
	 * Checks that end tick is not greater than startTick.
	 * Throws InvalidParameterException on error.
	 * @param startTick
	 * @param endTick
	 */
	private static void checkTickPreconditions(int startTick, int endTick){
		if(endTick > startTick){
			throw new InvalidParameterException("startTick must be > endTick");
		}
	}
	
	public static ArrayList<SessionHistory> toSessionHistoryArray(Map<Integer, Map<UUID, Offer>> tradeHistory) throws Exception{
		
		if(tradeHistory.isEmpty()){
				throw new Exception("Jizz all up my ass");
		}
		
		ArrayList<SessionHistory> s = new ArrayList<SessionHistory>();
		
		for (Entry<Integer, Map<UUID, Offer>> e : tradeHistory.entrySet()) {
			
			int year = e.getKey()/(GameConst.YEARS_IN_SESSION * GameConst.TICKS_IN_YEAR);
			
			for (Entry<UUID, Offer> offer : e.getValue().entrySet()) {
				try {
					s.get(year).add(offer.getValue(), e.getKey());
				} catch (Exception e1) {
					s.add(new SessionHistory(year));
					s.get(year).add(offer.getValue(), e.getKey());
				}
			}
		}
		
		return s;
	}

}
