/**
 * 
 */
package uk.ac.ic.kyoto.simulationagent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.singletonfactory.SingletonProvider;
import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.tradehistory.TradeHistory;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

/**
 * @author farhanrahman
 *
 */
public class SimulationAgent extends AbstractParticipant {

	Logger logger = Logger.getLogger(SimulationAgent.class);
	private TradeHistory tradeHistory = SingletonProvider.getTradeHistory();
	private final static String key = "TRADE_HISTORY";
	private Integer endTime;
	/**
	 * @param id
	 * @param name
	 */
	public SimulationAgent(UUID id, String name, Integer endTime) {
		super(id, name);
		this.endTime = endTime;
	}

	/**
	 * @param id
	 * @param name
	 * @param environment
	 * @param network
	 * @param time
	 */
	public SimulationAgent(UUID id, String name,
			EnvironmentConnector environment, NetworkAdaptor network, Time time) {
		super(id, name, environment, network, time);
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
	 */
	@Override
	protected void processInput(Input in) {}
	
	@Override
	public void execute(){
		Time currentTime = SimTime.get();
		if(tradeHistory == null){
			logger.warn("HUGE PROBLEM!!");
		}
		
		if(SimTime.get().intValue() == (this.endTime - 1)){
			Map<Integer, Map<UUID,Offer>> session = tradeHistory.getHistory();
			if(session != null){
	            String s = "\n";
	            for(Integer i : session.keySet()){
	                    Map<UUID,Offer> offers = session.get(i);
	                    s += "Time: " + i;
	                    if(offers != null){
		                    for(UUID id : offers.keySet()){
		                            s += "          id:" + id + " trade: " + offers.get(id).toString();
		                    }
	                    }
	                    s += "\n";
	            }
	            this.persist.setProperty(SimulationAgent.key, s);
			}
		}

/*			TradeHistory tradeHistory = SingletonProvider.getTradeHistory();
			Map<Integer,Map<UUID,Offer>> m = tradeHistory.getHistory();
			logger.info(m);
			logger.info("Test");*/
//		this.persist.setProperty(SimulationAgent.key, session.toString());
	}

}
