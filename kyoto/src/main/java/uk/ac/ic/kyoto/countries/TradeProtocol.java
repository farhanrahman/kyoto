package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.OfferMessage.OfferMessageType;
import uk.ac.ic.kyoto.singletonfactory.SingletonProvider;
import uk.ac.ic.kyoto.tokengen.Token;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.ic.kyoto.tradehistory.TradeHistory;
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
 * 
 * @author cmd08 and farhanrahman and azyzio
 *
 */
public abstract class TradeProtocol extends FSMProtocol {
	private final UUID id;
	private final UUID authkey;
	protected final EnvironmentConnector environment;
	private final Logger logger;

	Token tradeToken;

	private TradeHistory tradeHistory;

	private AbstractCountry participant;

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
			final EnvironmentConnector environment, NetworkAdaptor network, AbstractCountry participant)
					throws FSMException {
		super("Trade Protocol", FSM.description(), network);
		this.participant = participant;
		this.id = id;
		this.authkey = authkey;
		this.environment = environment;


		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + id);

		this.tradeToken = SingletonProvider.getToken();

		this.tradeHistory = SingletonProvider.getTradeHistory();

		if(this.tradeToken == null){
			logger.warn("Huge problem");
		}

		if(this.tradeHistory == null){
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
								OfferMessage offerMessage = ((OfferMessage) message.getData());
								Offer trade = new Offer(offerMessage.getOfferQuantity(), offerMessage.getOfferUnitCost(), offerMessage.getOfferType(), offerMessage.getOfferInvestmentType());
								handleTradeCompletion(trade.reverse());
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
								logger.info("Trade was rejected");
							}
			})			
			.addTransition(Transitions.TIMEOUT,
					new TimeoutCondition(4),
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
					if (message.getData() instanceof OfferMessage) {
						OfferMessage offerMessage = ((OfferMessage) message.getData());
						Offer trade = new Offer(offerMessage.getOfferQuantity(), offerMessage.getOfferUnitCost(), offerMessage.getOfferType(), offerMessage.getOfferInvestmentType());
						conv.setEntity(offerMessage);
						NetworkAddress from = conv.getNetwork()
								.getAddress();
						NetworkAddress to = message.getFrom();
						Time t = SimTime.get();
						if (acceptExchange(to, trade)) {
							// send accept message
							logger.debug("Accepting exchange proposal: "
									+ trade);
							if(!TradeProtocol.this.tradeHistory.tradeExists(offerMessage.getTradeID())){
								TradeProtocol.this.tradeHistory.addToHistory(
										SimTime.get(), offerMessage.getTradeID(), trade);
								handleTradeCompletion(trade);
								conv.getNetwork().sendMessage(
										new UnicastMessage<OfferMessage>(
												Performative.ACCEPT_PROPOSAL,
												ResponderReplies.ACCEPT.name(), t,
												from, to, offerMessage));								
							}else{
								logger.warn("Trade already happened");
							}
						} else {
							// send reject message
							logger.debug("Rejecting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Object>(
											Performative.REJECT_PROPOSAL,
											ResponderReplies.REJECT.name(), t,
											from, to, null));
						}
					} else {
						logger.warn("Message type not equal to OfferMessage");
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
	 * containing OfferMessage.class data types
	 * Moreover it is also checked whether the
	 * OfferMessge has a valid tradeID assigned
	 * to it.
	 */
	@Override
	public boolean canHandle(Input in){
		Message<?> m = (Message<?>) in;
		if(m.getData().getClass().equals(OfferMessage.class)){
			try{
				@SuppressWarnings("unchecked")
				Message<OfferMessage> message = (Message<OfferMessage>) in;
				if(message.getData().getOfferMessageType() == OfferMessageType.TRADE_PROTOCOL)
					return super.canHandle(in);			
			}
			catch(ClassCastException e){
				logger.warn(e);
			}
			return false;
		}else{
			return false;
		}		
	}

	class TradeSpawnEvent extends ConversationSpawnEvent {

		final OfferMessage offerMessage;

		public TradeSpawnEvent(NetworkAddress with, int quantity, double unitCost, TradeType type, InvestmentType itype, OfferMessage offerMessage) {
			super(with);
			this.offerMessage = new OfferMessage(new Offer(quantity, unitCost, type, itype), offerMessage.getTradeID(), OfferMessageType.TRADE_PROTOCOL);
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

	public void offer(NetworkAddress to, int quantity, double unitPrice, OfferMessage offerMessage)
			throws FSMException {
		this.spawnAsInititor(new TradeSpawnEvent(to, quantity, unitPrice, offerMessage.getOfferType(), offerMessage.getOfferInvestmentType(), offerMessage));
	}

	protected abstract boolean acceptExchange(NetworkAddress from,
			Offer trade);

	public void handleTradeCompletion(Offer trade){
		try{
			switch(trade.getType()){
			case BUY:	participant.receiveOffset(trade.getQuantity());
						participant.payMoney(trade.getTotalCost());
						logger.info("My name: " + this.participant.getName()+ ", I am buying: " + trade.getQuantity() + " and paying: " + trade.getTotalCost());
						break;

			case SELL:	participant.sellOffset(trade.getQuantity());
						participant.receiveMoney(trade.getTotalCost());
						logger.info("My name: " + this.participant.getName()+ ", I am selling: " + trade.getQuantity() + " and receiving: " + trade.getTotalCost());
						break;

			case INVEST:	participant.receiveOffset(trade.getQuantity());
							participant.payMoney(trade.getTotalCost());
							logger.info("My name: " + this.participant.getName()+ ", I am receiving: " + trade.getQuantity() + " for my investment of: " + trade.getTotalCost());
							break;

			case RECEIVE:	participant.receiveMoney(trade.getTotalCost());
				
							if (trade.itype.equals(InvestmentType.ABSORB)) {
								try {
									participant.carbonAbsorptionHandler.investInCarbonAbsorption(trade.getQuantity());
								} catch (NotEnoughCarbonOutputException e) {
									e.printStackTrace();
								} catch (NotEnoughCashException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								logger.info("My name: " + this.participant.getName()+ ", I am generating: " + trade.getQuantity() +
											" for an investment in absorption of: " + trade.getTotalCost() + ". My new absorption is " + participant.carbonAbsorption);
							}
			
							else if (trade.itype.equals(InvestmentType.REDUCE)) {
								try {
									participant.carbonReductionHandler.investInCarbonReduction(trade.getQuantity());
								} catch (NotEnoughCarbonOutputException e) {
									e.printStackTrace();
								} catch (NotEnoughCashException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								logger.info("My name: " + this.participant.getName()+ ", I am generating: " + trade.getQuantity() +
											" for an investment in reduction of: " + trade.getTotalCost() + ". My new output is " + participant.carbonOutput);
							}
							break;
			}
		}catch(NullPointerException e){
			logger.warn(e);
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getAuthkey() {
		return authkey;
	}
}