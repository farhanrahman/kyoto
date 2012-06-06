package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;

/**
 * Stores and makes available all given TickHistories for current Session
 * 
 * @author cs2309 & ov109
 */
public class SessionHistory {
	
	private final int sessionId;
	private TreeMap<Integer, TickHistory> session;
		
	public SessionHistory(int sessionId) {
		this.sessionId = sessionId;
		init();
	}
	
	private void init(){
		session = new TreeMap<Integer, TickHistory>();
	}
	
	/**
	 * Adds given message to current TickHistory. On next
	 * tick, a new TickHistory is created, and old TickHistory
	 * is added to session history Set.
	 * @param m
	 * @param currentTick
	 * @throws Exception 
	 */
	public void add(Trade m, int currentTick) throws Exception{
		
		if(session.isEmpty()){
			TickHistory t = new TickHistory(currentTick);
			t.addMessage(m);

			session.put(currentTick, t);
		}else{
			//TickHistory t = session.get(previousTickId);
			TickHistory t = session.lastEntry().getValue();

			if(t.getTickId() != currentTick){
				t = new TickHistory(currentTick);
				t.addMessage(m);
				session.put(currentTick, t);
			}else{
				t.addMessage(m);
				session.put(currentTick, t);
			}
		}
	}
	
	public TickHistory getTick(int tickId){
		return session.get(tickId);
	}
	
	public Map<Integer, TickHistory> getSession(){
		return Collections.unmodifiableMap(session);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}
		
		if (!(obj instanceof SessionHistory)){
			return false;
		}
		
		SessionHistory tickHistory = (SessionHistory) obj;

		boolean sessionTest = tickHistory.session.equals(this.session);
		
		return sessionTest && (tickHistory.sessionId == this.sessionId);
	}
	
	@Override
	public int hashCode() {
		int result = 43;
		
		result = 4 * result + this.sessionId;
		result = 4 * result + this.session.hashCode();
		
		return result;
	}
	
}
