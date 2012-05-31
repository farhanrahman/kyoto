package uk.ac.ic.kyoto.trade;

import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
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
	
	private final UUID id;
	private final UUID authkey;
	
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
		TIMEOUT
	}

	public TradeProtocol(final UUID id, final UUID authkey, NetworkAdaptor network) {
		super("Trade Protocol", FSM.description(), network);
		this.id = id;
		this.authkey = authkey;
		
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
								
								//Send message stating intention of trading
								TradeSpawnEvent e = (TradeSpawnEvent) event;
								NetworkAddress from = conv.getNetwork().getAddress();
								List<NetworkAddress> to = conv.recipients;
								logger.info("Publishing proposition");
								conv.getNetwork().sendMessage(
									new MulticastMessage<Object>(
											Performative.PROPOSE, 
											"TRADE", 
											SimTime.get(), 
											conv.getNetwork().getAddress(), 
											new TradeMessage()
									)
								);			
							}
						}
					)
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
								logger.info("Trade was accepted");
								
							}
						}
					)
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
								logger.info("Trade was rejected");
							}
						}
					)
					
					/* Non-initiator FSM */
					.addTransition(Transitions.RECEIVE_TRADE, 
						new AndCondition(new MessageTypeCondition("TRADE")),
						States.START,
						States.TRADE_ACCEPTED,
						new InitialiseConversationAction() {
							
							@Override
							public void processInitialMessage(Message<?> message, FSMConversation conv,
									Transition transition) {
								logger.info("Propositions received");								//Logic here to decide if offer made?
								conv.getNetwork().sendMessage(
									new UnicastMessage<Object>(
											Performative.AGREE,
											"ACCEPT", 
											SimTime.get(),
											conv.getNetwork().getAddress(),
											message.getFrom()
											));
							}
						}
					)	
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
		
		@Override
		public String toString() {
			return "Trade: "+quantity+" @ "+unitCost; 
		}
	}
	
	public void offer(NetworkAddress to, int quantity, int unitPrice, TradeType type)
			throws FSMException {
		this.spawnAsInititor(new TradeSpawnEvent(to, quantity, unitPrice, type));
	}

	protected abstract boolean acceptExchange(NetworkAddress from,
			Trade trade);

	protected boolean surrenderToken(NetworkAddress to, Trade trade) {
		return true;
	}
	
}

