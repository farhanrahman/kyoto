package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stores and makes available all given TickHistories for current Session<br>
 * <br>
 * <i>NOTE: This is based on a yet to be implemented TradeMessage and
 * InvestmentMessage classes (which should extend Message).</i> 
 * 
 * @author cs2309 & ov109
 */
public class SessionHistory {
	
	private final int sessionId;
	
	private ArrayList<TickHistory> session;
	private TickHistory tick;
	
	public SessionHistory(int sessionId) {
		this.sessionId = sessionId;
		init();
	}
	
	/*
	 * Since the TickHistory constructor requires a current tick,
	 * its creation is left until a message needs to be added.
	 * Consequently, tick made to point to null until its first
	 * creation.
	 */
	private void init(){
		session = new ArrayList<TickHistory>();
		tick = null;
	}
	
	/**
	 * Adds given message to current TickHistory. On next
	 * tick, a new TickHistory is created, and old TickHistory
	 * is added to session history Set.
	 * @param m
	 * @param currentTick
	 * @throws Exception 
	 */
	public void add(Message m, int currentTick) throws Exception{
		
		if(tick == null){
			tick = new TickHistory(currentTick);
		}
		
		if (tick.getTickId() != currentTick){
			session.add(tick);
			tick = new TickHistory(currentTick);
		}
		
		tick.addMessage(m);
	}
	
	public TickHistory getTick(int tickId){
		return session.get(tickId);
	}
	
	public Iterator<TickHistory> getSession(){
		return session.iterator();
	}
	
}
