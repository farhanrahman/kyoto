/**
 * 
 */
package uk.ac.ic.kyoto.simulationagent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.trade.Offer;
import uk.ac.ic.kyoto.tradehistory.TradeHistory;
import uk.ac.ic.kyoto.tokengen.SingletonProvider;
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
	private Map<Integer, Map<UUID,Offer>> session = null;
	private final static String key = "TRADE_HISTORY";
	/**
	 * @param id
	 * @param name
	 */
	public SimulationAgent(UUID id, String name) {
		super(id, name);
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
		Map<UUID,Offer> currentTrade = tradeHistory.getHistoryForTime(currentTime);
		if(currentTrade != null){
			if(this.session == null){
				session = new HashMap<Integer,Map<UUID,Offer>>();
			}
			
			session.put(currentTime.intValue(), currentTrade);
			logger.info(session);
			logger.info("log break");
		}

/*			TradeHistory tradeHistory = SingletonProvider.getTradeHistory();
			Map<Integer,Map<UUID,Offer>> m = tradeHistory.getHistory();
			logger.info(m);
			logger.info("Test");*/
//		this.persist.setProperty(SimulationAgent.key, session.toString());
	}

}
