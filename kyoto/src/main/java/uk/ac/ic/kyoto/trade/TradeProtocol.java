package uk.ac.ic.kyoto.trade;

import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.ic.kyoto.countries.TradeAction;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.fsm.StateType;
import uk.ac.imperial.presage2.util.fsm.Transition;
import uk.ac.imperial.presage2.util.protocols.ConversationCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMConversation;
import uk.ac.imperial.presage2.util.protocols.FSMProtocol;
import uk.ac.imperial.presage2.util.protocols.InitialiseConversationAction;
import uk.ac.imperial.presage2.util.protocols.MessageAction;
import uk.ac.imperial.presage2.util.protocols.MessageTypeCondition;
import uk.ac.imperial.presage2.util.protocols.SpawnAction;


/**
 * 
 * More sure about this: taken from https://github.com/Presage/HelloWorld/blob/master/src/main/java/uk/ac/imperial/presage2/helloworld/HelloWorldProtocol.java
 * https://github.com/sammacbeth/ColouredTrls/blob/master/src/main/java/uk/ac/imperial/colrdtrls/protocols/TokenExchangeProtocol.java
 * 
 * This is just a really simple implementation where one party can only ever accept a trade
 * 
 * @author cmd08
 *
 */
public abstract class TradeProtocol extends FSMProtocol {

	public final static class Trade{
		final int quantity;
		final int unitCost;
		final TradeType type;

		public Trade(int quantity, int unitCost, TradeType type) {
			this.quantity = quantity;
			this.unitCost = unitCost;
			this.type = type;
		}

		public int getQuantity() {
			return quantity;
		}

		public int getUnitCost() {
			return unitCost;
		}

		public int getTotalCost() {
			return unitCost * quantity;
		}
		
		public TradeType getType(){
			return this.type;
		}

		@Override
		public String toString() {
			return "Trade: "+quantity+" @ "+unitCost; 
		}
		
		public Trade reverse(){
			TradeType t = this.type.equals(TradeType.BUY)?TradeType.SELL:TradeType.BUY;
			return new Trade(this.quantity, this.unitCost, t);
		}
		
		@Override
		//cs2309: Written using Effective Java (Josh Bloch) as reference
		public boolean equals(Object obj) {
			if (obj == this){
				return true;
			}
			
			if (!(obj instanceof Trade)){
				return false;
			}
			
			Trade trade = (Trade) obj;
			return (trade.quantity == this.quantity) &&
					(trade.unitCost == this.unitCost) &&
					(trade.type == this.type);
		}
		
		@Override
		//cs2309: Written using Effective Java (Josh Bloch) as reference
		public int hashCode() {
			int result = 42;
			result = 69 * result + this.quantity;
			result = 69 * result + this.unitCost;
			
			switch (this.type) {
			case BUY:
				result = 69 * result + 1;
				break;
			case SELL:
				result = 69 * result + 2;
				break;
			}
			
			return result;
		}

	}


	private final UUID id;
	private final UUID authkey;
	protected final EnvironmentConnector environment;
	private final Logger logger;

	enum States {
		START, TRADE_PROPOSED, RESPONSE_RECEIVED, TRADE_RECEIVED, TRADE_ACCEPTED, TRADE_REJECTED,
		//PROPOSITION_PUBLISHED, OFFERS_RECEIVED, OFFER_ACCEPTED, 
		//PROPOSITION_RECEIVED, OFFERS_PUBLISHED, TRADE_COMPLETE, 
		TIMED_OUT
	};

	enum Transitions {
		PROPOSE_TRADE, RECEIVE_RESPONSE, RECEIVE_TRADE, ACCEPT_TRADE, REJECT_TRADE,
		//PUBLISH_PROPOSITION, RECEIVE_OFFERS, ACCEPT_OFFER, REJECT_OFFER, 
		//RECEIVE_PROPOSITIONS, PUBLISH_OFFERS, COMPLETE_TRADE,
		TIMEOUT, ERROR
	}

	public TradeProtocol(final UUID id, final UUID authkey, 
			final EnvironmentConnector environment, NetworkAdaptor network)
					throws FSMException {
		super("Trade Protocol", FSM.description(), network);

		this.id = id;
		this.authkey = authkey;
		this.environment = environment;


		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + id);

		try {
			this.description

			/* Initiator FSM */
			.addState(States.START, StateType.START)
			.addState(States.TRADE_PROPOSED)
			.addState(States.RESPONSE_RECEIVED)
			.addState(States.TRADE_ACCEPTED, StateType.END)
			.addState(States.TRADE_REJECTED, StateType.END)
			.addState(States.TIMED_OUT, StateType.END);

			this.description
			/*
			 * Transition: START -> TRADE_PROPOSED.
			 * Send a trade proposal to all other agents
			 * Signals intention to trade (either buy or sell).
			 */
			.addTransition(Transitions.PROPOSE_TRADE,
					new EventTypeCondition(ConversationSpawnEvent.class), 
					States.START,
					States.TRADE_PROPOSED, 
					new SpawnAction() {

				@Override
				public void processSpawn(ConversationSpawnEvent event,
						FSMConversation conv, Transition transition) {
					// send message offering the Exchange of tokens
					// described in the ExchangeSpawnEvent.
					TradeSpawnEvent e = (TradeSpawnEvent) event;
					NetworkAddress from = conv.getNetwork().getAddress();
					NetworkAddress to = conv.recipients.get(0);
					logger.debug("Initiating: " + e.trade);
					conv.entity = e.trade;
					conv.getNetwork().sendMessage(
							new UnicastMessage<Trade>(
									Performative.PROPOSE, 
									Transitions.PROPOSE_TRADE.name(),
									SimTime.get(), from,
									to, e.trade));
				}
			})
			/*
			 * Transitions: TRADE_PROPOSED -> TRADE_ACCEPTED
			 * Trade proposal has been accepted by someone
			 */
			.addTransition(Transitions.RECEIVE_RESPONSE,
					new AndCondition(new MessageTypeCondition("ACCEPT"),
							new ConversationCondition()),
							States.TRADE_PROPOSED, 
							States.TRADE_ACCEPTED,
							new MessageAction() {

				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO Auto-generated method stub
					// On Success callback fired?
					logger.info("Trade was accepted");

					if (surrenderResource(message.getFrom(), (Trade) message.getData())){
						try {
							environment.act(new TradeAction(), id, authkey);
						} catch (ActionHandlingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			})
			/*
			 * Transitions: TRADE_PROPOSED -> TRADE_REJECTED
			 * Trade proposal has been rejected by someone else.
			 */
			.addTransition(Transitions.RECEIVE_RESPONSE,
					new AndCondition(new MessageTypeCondition("REJECT"),
							new ConversationCondition()),
							States.TRADE_PROPOSED,
							States.TRADE_REJECTED,
							new MessageAction() {
	
				@Override
				public void processMessage(Message<?> message, FSMConversation conv,
						Transition transition) {
					// TODO Auto-generated method stub
					// On rejection callback?
					logger.info("Trade was rejected");
	
				}
			})

			/* Non-initiator FSM */
			.addTransition(Transitions.RECEIVE_TRADE, 
					new AndCondition(new MessageTypeCondition("TRADE")),
					States.START,
					States.TRADE_ACCEPTED,

					new InitialiseConversationAction() {

				@Override
				public void processInitialMessage(Message<?> message,
						FSMConversation conv, Transition transition) {
					if (message.getData() instanceof Trade) {
						EnvironmentConnector env = TradeProtocol.this.environment;
						Trade trade = ((Trade) message.getData())
								.reverse();
						conv.setEntity(trade);
						NetworkAddress from = conv.getNetwork()
								.getAddress();
						NetworkAddress to = message.getFrom();
						Time t = SimTime.get();
						if (acceptExchange(to, trade)) {
							// send accept message
							logger.debug("Accepting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Trade>(
											Performative.ACCEPT_PROPOSAL,
											Transitions.ACCEPT_TRADE.name(), t,
											from, to, trade));
							// TODO optionally surrender appropriate token
							if (surrenderResource(to, trade)) {
								try {
									environment.act(new TradeAction(), id, authkey);
								} catch (ActionHandlingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else {
							// send reject message
							logger.debug("Rejecting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Trade>(
											Performative.REJECT_PROPOSAL,
											Transitions.REJECT_TRADE.name(), t,
											from, to, trade));
						}
					} else {
						// TODO error transition
					}
				}
			})	
			.build();

		} catch (FSMException e) {
			e.printStackTrace();
		}



	}

	class TradeSpawnEvent extends ConversationSpawnEvent {

		final Trade trade;

		public TradeSpawnEvent(NetworkAddress with, int quantity, int unitCost, TradeType type) {
			super(with);
			this.trade = new Trade(quantity, unitCost, type);
		}

	}



	public void offer(NetworkAddress to, int quantity, int unitPrice, TradeType type)
			throws FSMException {
		this.spawnAsInititor(new TradeSpawnEvent(to, quantity, unitPrice, type));
	}

	protected abstract boolean acceptExchange(NetworkAddress from,
			Trade trade);

	protected boolean surrenderResource(NetworkAddress to, Trade trade){
		return true;
	}
}

