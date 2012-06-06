package uk.ac.ic.kyoto.countries;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.trade.Trade;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.StateTransformer;

public class TradeActionService extends EnvironmentService {

	Logger logger = Logger.getLogger(TradeActionService.class);
	
	@Inject
	protected TradeActionService(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
	}
	
	public void executeTrade(final Trade t, final Time time, final UUID initiator, final UUID receiver){
		this.sharedState.change("Trade-initiator side", initiator, new StateTransformer(){
			@Override
			public Serializable transform(Serializable state) {
				@SuppressWarnings("unchecked")
				Map<Integer,Map<UUID, Trade>> s = (Map<Integer,Map<UUID, Trade>>) state;
				Map<UUID, Trade> q = new HashMap<UUID, Trade>();
				q.put(initiator, t);
				s.put(time.intValue(), q);
				return (Serializable) s;
			}
		});
		this.sharedState.change("Trade-receiver side", receiver, new StateTransformer(){
			@Override
			public Serializable transform(Serializable state) {
				@SuppressWarnings("unchecked")
				Map<Integer,Map<UUID, Trade>> s = (Map<Integer,Map<UUID, Trade>>) state;
				Map<UUID, Trade> q = new HashMap<UUID, Trade>();
				q.put(receiver, t);
				s.put(time.intValue(), q);
				return (Serializable) s;
			}
		});
	}
}
