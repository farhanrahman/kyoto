package uk.ac.ic.kyoto.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.tokengen.Token;
import uk.ac.ic.kyoto.tokengen.TokenGenProvider;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Input;
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

	protected Token tradeToken;
	
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

	public TradeProtocol(final UUID id, final UUID authkey, 
			final EnvironmentConnector environment, NetworkAdaptor network)
					throws FSMException {
		super("Trade Protocol", FSM.description(), network);

		this.id = id;
		this.authkey = authkey;
		this.environment = environment;


		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + id);

		this.tradeToken = TokenGenProvider.get();
		
		if(this.tradeToken == null){
			logger.warn("Huge problem");
		}
		
		try {
			this.description
			.addState(States.START, StateType.START)
			.addState(States.TRADE_PROPOSED)
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
					logger.debug("Initiating: " + e.offerMessage);
					conv.entity = e.offerMessage;
					conv.getNetwork().sendMessage(
							new UnicastMessage<OfferMessage>(
									Performative.PROPOSE, 
									Transitions.PROPOSE_TRADE.name(),
									SimTime.get(), from,
									to, e.offerMessage));
				}
			})
			.addTransition(Transitions.TRADE_ACCEPTED,
						   new AndCondition(
								   new MessageTypeCondition(ResponderReplies.ACCEPT.name()), 
								   new ConversationCondition()), 
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
						   new AndCondition(
								   new MessageTypeCondition(ResponderReplies.REJECT.name()), 
								   new ConversationCondition()), 
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
					new AndCondition(
							new TimeoutCondition(4), 
							new ConversationCondition()),
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
					 * Transitions: START -> TRADE_DONE
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
					if (message.getData() instanceof Offer) {
						Offer trade = ((Offer) message.getData())
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
									new UnicastMessage<Offer>(
											Performative.ACCEPT_PROPOSAL,
											ResponderReplies.ACCEPT.name(), t,
											from, to, trade));
							/*
								try {
									environment.act(new TradeAction(), id, authkey);
								} catch (ActionHandlingException e) {
									logger.warn(e);
								}
							*/
						} else {
							// send reject message
							logger.debug("Rejecting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Offer>(
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
			logger.warn(e);
		}



	}
	
	/**
	 * canHandle method overriden in order
	 * to force this class to handle Message
	 * containing Trade data types
	 */
	@Override
	public boolean canHandle(Input in){
		Message<?> m = (Message<?>) in;
		if(m.getData().getClass().equals(Offer.class)){
			return super.canHandle(in);		
		}else{
			return false;
		}		
	}
	
	class TradeSpawnEvent extends ConversationSpawnEvent {

		final OfferMessage offerMessage;

		public TradeSpawnEvent(NetworkAddress with, int quantity, int unitCost, TradeType type) {
			super(with);
			UUID id = TradeProtocol.this.tradeToken.getToken();
			this.offerMessage = new OfferMessage(new Offer(quantity, unitCost, type), id);
		}

	}


	/**
	 * Method used to get agents which are not
	 * in an FSMProtocol conversation with this
	 * agent
	 * @return
	 */
	public List<NetworkAddress> getAgentsNotInConversation(){
		List<NetworkAddress> all = new ArrayList<NetworkAddress>(this.network.getConnectedNodes());
		all.removeAll(this.getActiveConversationMembers());
		return all;
	}

	public void offer(NetworkAddress to, int quantity, int unitPrice, TradeType type)
			throws FSMException {
		this.spawnAsInititor(new TradeSpawnEvent(to, quantity, unitPrice, type));
	}
	
	protected abstract boolean acceptExchange(NetworkAddress from,
			Offer trade);

	public UUID getId() {
		return id;
	}

	public UUID getAuthkey() {
		return authkey;
	}
}

