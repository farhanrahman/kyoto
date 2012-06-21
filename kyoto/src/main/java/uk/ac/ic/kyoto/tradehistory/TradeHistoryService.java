/**
 * 
 */
package uk.ac.ic.kyoto.tradehistory;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.singletonfactory.SingletonProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

/**
 * Environment Service that invokes
 * method in TradeHistoryImplementation
 * that dumps current tick trade histories
 * onto the database
 * @author farhanrahman
 *
 */
public class TradeHistoryService extends EnvironmentService {

	private TradeHistory tradeHistory = SingletonProvider.getTradeHistory();
	/**
	 * @param sharedState
	 */
	@Inject
	public TradeHistoryService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	@Inject
	public void setEventBus(EventBus eb){
		eb.subscribe(this);
	}
	
	@EventListener
	public void endOfTimeUpdate(EndOfTimeCycle e){
		tradeHistory.dumpData();
	}
	

}
