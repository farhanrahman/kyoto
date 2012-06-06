package uk.ac.ic.kyoto.monitor;

import java.util.ArrayList;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.trade.TradeProtocol;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class Monitor extends AbstractParticipant {

	static final private ArrayList<AbstractCountry> memberStates = new ArrayList<AbstractCountry>();
	private static final double MONITORING_PRICE = 0; //Decide on a price for monitoring
	private TradeProtocol tradeProtocol;
	static private double cash;
	
	public Monitor(UUID id, String name) {
		super(id, name);
	}
	
	@Override 
	public void initialise() {
		super.initialise();
		try {
			 tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from, Trade trade) {
					// TODO Auto-generated method stub
					return false;
				}
			};
		} catch (FSMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Called at every new tick in the simulation - define Monitor behaviour here
	 */
	@Override
	public void execute() {
		super.execute();
		
		// Generate a randomInt and if divisible by 30 then monitor all countries.
		// TODO monitor only individual countries.
		if (Random.randomInt() % 30 == 0 && cash >= MONITORING_PRICE) {
			cash -= MONITORING_PRICE;
			for (AbstractCountry a : memberStates) {
				a.getMonitored();
			}
		}
	}
	
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub
		// Keep empty!
	}
	
	/**
	 * Add member states to the Monitor. Allows operation of sanctions, 
	 * credits, etc.
	 * @param state 
	 */
	static public void addMemberState(AbstractCountry state) {
		memberStates.add(state);
	}
	
	/**
	 * Give a pre-determined amount for monitoring
	 * @param tax
	 */
	// TODO: synchronised??
	static synchronized public void taxForMonitor (double tax) {
		cash += tax;
	}

}
