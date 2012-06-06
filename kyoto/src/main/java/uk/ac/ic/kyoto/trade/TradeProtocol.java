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
import uk.ac.imperial.presage2.util.fsm.Action;
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
import uk.ac.imperial.presage2.util.protocols.TimeoutCondition;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
/**
 * 
 * More sure about this: taken from https://github.com/Presage/HelloWorld/blob/master/src/main/java/uk/ac/imperial/presage2/helloworld/HelloWorldProtocol.java
 * https://github.com/sammacbeth/ColouredTrls/blob/master/src/main/java/uk/ac/imperial/colrdtrls/protocols/TokenExchangeProtocol.java
 * 
 * This is just a really simple implementation where one party can only ever accept a trade
 * 
 * @author cmd08 and farhanrahman and azyio
 *
 */
public abstract class TradeProtocol extends FSMProtocol {
	private final UUID id;
	private final UUID authkey;
	protected final EnvironmentConnector environment;
	private final Logger logger;

	@Inject protected TradeTokenFactory tradeFactory;
	protected TradeToken tradeToken;
	
	public enum ResponderReplies{
		ACCEPT,REJECT,WAIT
	};
	
	enum States {
		START, //Common start state
		/*Initiator States*/
		TRADE_PROPOSED,
		TIMED_OUT, //Timed out state for initiator
		TRADE_DONE //Common end state for both responder and initiator		
	};

	enum Transitions {
		/*Initiator transitions*/
		PROPOSE_TRADE, 
		TIMEOUT,
		TRADE_ACCEPTED,
		TRADE_REJECTED,
		
		/*Responder transitions*/
		RESPOND_TO_TRADE
	}

	@Inject
	public TradeProtocol(final UUID id, final UUID authkey, 
			final EnvironmentConnector environment, NetworkAdaptor network)
					throws FSMException {
		super("Trade Protocol", FSM.description(), network);

		this.id = id;
		this.authkey = authkey;
		this.environment = environment;


		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + id);

		Injector injector = Guice.createInjector(new TradeTokenModule());
		this.tradeFactory = injector.getInstance(TradeTokenFactory.class);
		this.tradeToken = this.tradeFactory.get();
		
		if(this.tradeFactory == null){
			logger.warn("HUGE PROBLEM");
		}
		
		/*for(int i = 0; i < 10; i++){
			logger.info(this.tradeToken.getToken());
		}*/
		
		try {
			this.description
			.addState(States.START, StateType.START)
			.addState(States.TRADE_PROPOSED)
			//.addState(States.RESPONSE_RECEIVED)
			.addState(States.TRADE_DONE, StateType.END)
			.addState(States.TIMED_OUT, StateType.END);

			
			/* Initiator FSM */
			this.description
			/*
			 * Transition: START -> TRADE_PROPOSED.
			 * Send a trade proposal to all other agents
			 * Responds to the Multicast message sent by
			 * and agent.
			 */
			.addTransition(Transitions.PROPOSE_TRADE,
					new EventTypeCondition(TradeSpawnEvent.class), 
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
			.addTransition(Transitions.TRADE_ACCEPTED,
						   new AndCondition(new MessageTypeCondition(ResponderReplies.ACCEPT.name()), new ConversationCondition()), 
						   States.TRADE_PROPOSED,
						   States.TRADE_DONE, 
						   new MessageAction(){

							@Override
							public void processMessage(Message<?> message,
									FSMConversation conv, Transition transition) {
								// TODO Add Trade Action to confirm the Trade
								logger.info("Trade was accepted");
								
							}
			})
			.addTransition(Transitions.TRADE_REJECTED,
						   new AndCondition(new MessageTypeCondition(ResponderReplies.REJECT.name()), new ConversationCondition()), 
						   States.TRADE_PROPOSED,
						   States.TRADE_DONE, 
						   new Action(){

							@Override
							public void execute(Object event, Object entity,
									Transition transition) {
								//Perform Trade action required?
								logger.info("Trade was rejected");
							}
			})			
			.addTransition(Transitions.TIMEOUT,
					new AndCondition(new TimeoutCondition(4), new ConversationCondition()),
					States.TRADE_PROPOSED,
					States.TIMED_OUT, 
					new Action(){

						@Override
						public void execute(Object event, Object entity,
								Transition transition) {
								logger.warn("Initiator timed out");
						}
				
			});
			
			
			
			/*Responder FSM*/
			
					/*
					 * Transitions: START -> RESPONSE_RECEIVED
					 * Message received by agent who sent the multicast message
					 */
			this.description
			/* Non-initiator FSM */
			.addTransition(Transitions.RESPOND_TO_TRADE, 
					new MessageTypeCondition(Transitions.PROPOSE_TRADE.name()),
					States.START,
					States.TRADE_DONE,
					new InitialiseConversationAction() {

				@Override
				public void processInitialMessage(Message<?> message,
						FSMConversation conv, Transition transition) {
					if (message.getData() instanceof Trade) {
						//EnvironmentConnector env = TradeProtocol.this.environment;
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
											ResponderReplies.ACCEPT.name(), t,
											from, to, trade));
							// TODO optionally surrender appropriate token
							/*if (surrenderResource(to, trade)) {
								try {
									environment.act(new TradeAction(), id, authkey);
								} catch (ActionHandlingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}*/
						} else {
							// send reject message
							logger.debug("Rejecting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Trade>(
											Performative.REJECT_PROPOSAL,
											ResponderReplies.REJECT.name(), t,
											from, to, trade));
						}
					} else {
						// TODO error transition
						logger.warn("Message type not equal to Trade");
					}
				}
			});

		} catch (FSMException e) {
			e.printStackTrace();
		}



	}

	class TradeSpawnEvent extends ConversationSpawnEvent {

		final Trade trade;

		public TradeSpawnEvent(NetworkAddress with, int quantity, int unitCost, TradeType type) {
			super(with);
			UUID id = TradeProtocol.this.tradeToken.getToken();
			this.trade = new Trade(quantity, unitCost, type, id);
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

