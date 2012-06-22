package uk.ac.ic.kyoto.countries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.OfferMessage.OfferMessageType;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
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
import uk.ac.imperial.presage2.util.fsm.OrCondition;
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
		ACCEPT,REJECT
	};
	
	public enum InitiatorReplies{
		REVERT_NOW, TRADE_SUCCESSFUL,TRADE_UNSUCCESSFUL
	}

	enum States {
		START, //Common start state

		/*Initiator States*/
		TRADE_PROPOSED,
		TIMED_OUT, //Timed out state for initiator
		TRADE_DONE,
		
		/*Responder states*/
		WAIT_FOR_CONFIRMATION,
		REVERT,
		RESPONDER_TIMEDOUT
	};

	enum Transitions {
		/*Initiator transitions*/
		PROPOSE_TRADE, 
		TIMEOUT,
		TRADE_ACCEPTED,
		TRADE_REJECTED,

		/*Responder transitions*/
		RESPOND_TO_TRADE,
		REVERTING,
		CONFIRMATION,
		RESPONDER_TIME_OUT
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

		try {
			this.description
			.addState(States.START, StateType.START)
			.addState(States.TRADE_PROPOSED)
			.addState(States.WAIT_FOR_CONFIRMATION)
			.addState(States.REVERT, StateType.END)
			.addState(States.RESPONDER_TIMEDOUT, StateType.END)
			.addState(States.TRADE_DONE, StateType.END)
			.addState(States.TIMED_OUT, StateType.END);


			/*==========================Initiator FSM==========================*/
			this.description
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
								if(handleTradeCompletion(trade.reverse())){
									/*Everything worked out well. Its okay to end this FSM*/
									logger.info("Trade was accepted");
									conv.setEntity(offerMessage);
									NetworkAddress from = conv.getNetwork()
											.getAddress();
									NetworkAddress to = message.getFrom();
									Time t = SimTime.get();
									tradeSuccessful(from, offerMessage); /*Inform initiator that trade was successful*/
									conv.getNetwork().sendMessage(
											new UnicastMessage<OfferMessage>(
													Performative.CONFIRM,
													InitiatorReplies.TRADE_SUCCESSFUL.name(), t,
													from, to, offerMessage));
								}else{
									/*handleTradeCompletion for initiator did not work. Revert back
									 * both initiator and responder.*/
									logger.warn("Revert changes now");
									//TODO
									/*
									 * 1) Send message back to responder that initiator did not handle trade completion with success
									 * 2) Remove history from trade history that was recorded
									 * 3) Revert change for myself (initiator)
									 * 4) Message sent will revert change for responder*/
									NetworkAddress from = conv.getNetwork()
											.getAddress();
									NetworkAddress to = message.getFrom();
									Time t = SimTime.get();
									conv.setEntity(offerMessage);
									revertInitiator(trade.reverse());
									tradeFailed(from,offerMessage); /*Inform initiator that trade failed*/
									conv.getNetwork().sendMessage(
											new UnicastMessage<OfferMessage>(
													Performative.FAILURE,
													InitiatorReplies.REVERT_NOW.name(), t,
													from, to, offerMessage));
								}
							}
			})
			.addTransition(Transitions.TRADE_REJECTED,
						   new AndCondition(
								   new MessageTypeCondition(ResponderReplies.REJECT.name()), 
								   new ConversationCondition()), 
						   States.TRADE_PROPOSED,
						   States.TRADE_DONE, 
						   new MessageAction(){

							@Override
							public void processMessage(Message<?> message,
									FSMConversation conv, Transition transition) {
								OfferMessage offerMessage = ((OfferMessage) message.getData());
								NetworkAddress from = conv.getNetwork()
										.getAddress();
								NetworkAddress to = message.getFrom();
								Time t = SimTime.get();
								conv.setEntity(offerMessage);
								//Offer trade = new Offer(offerMessage.getOfferQuantity(), offerMessage.getOfferUnitCost(), offerMessage.getOfferType(), offerMessage.getOfferInvestmentType());
								tradeRejected(from,offerMessage);
								conv.getNetwork().sendMessage(
										new UnicastMessage<Object>(
												Performative.INFORM,
												InitiatorReplies.TRADE_UNSUCCESSFUL.name(), t,
												from, to, offerMessage));
								
							}
			})			
			.addTransition(Transitions.TIMEOUT,
					new TimeoutCondition(3),
					States.TRADE_PROPOSED,
					States.TIMED_OUT, 
					new Action(){

						@Override
						public void execute(Object event, Object entity,
								Transition transition) {
								logger.warn("Initiator timed out");
								
						}

			});



			/*==========================Responder FSM==========================*/
			this.description
			.addTransition(Transitions.RESPOND_TO_TRADE, 
					new MessageTypeCondition(Transitions.PROPOSE_TRADE.name()),
					States.START,
					States.WAIT_FOR_CONFIRMATION,
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
							if(!TradeProtocol.this.tradeHistory.tradeExists(offerMessage.getTradeID())){
								if(handleTradeCompletion(trade)){
									conv.getNetwork().sendMessage(
											new UnicastMessage<OfferMessage>(
													Performative.ACCEPT_PROPOSAL,
													ResponderReplies.ACCEPT.name(), t,
													from, to, offerMessage));
								TradeProtocol.this.tradeHistory.addToHistory(
										SimTime.get(), offerMessage.getTradeID(), offerMessage); /*Add to trade history*/									
								logger.debug("Accepting exchange proposal: "
										+ trade);
								}else{
									/* 1) Revert changes for responder*/
									revertResponder(trade);
									tradeFailed(from,offerMessage); /*Inform responder that trade failed*/
									conv.setEntity(offerMessage);
									conv.getNetwork().sendMessage(
											new UnicastMessage<Object>(
													Performative.REJECT_PROPOSAL,
													ResponderReplies.REJECT.name(), t,
													from, to, offerMessage));
								}							
							}else{
								logger.warn("Trade already happened");
								conv.getNetwork().sendMessage(
										new UnicastMessage<Object>(
												Performative.REJECT_PROPOSAL,
												ResponderReplies.REJECT.name(), t,
												from, to, offerMessage));
							}
						} else {
							// send reject message
							logger.debug("Rejecting exchange proposal: "
									+ trade);
							conv.getNetwork().sendMessage(
									new UnicastMessage<Object>(
											Performative.REJECT_PROPOSAL,
											ResponderReplies.REJECT.name(), t,
											from, to, offerMessage));
						}
					} else {
						logger.warn("Message type not equal to OfferMessage");
					}
				}
			})
			.addTransition(Transitions.REVERTING,
						   new AndCondition(
								   new MessageTypeCondition(InitiatorReplies.REVERT_NOW.name()), 
								   new ConversationCondition()), 
						   States.WAIT_FOR_CONFIRMATION,
						   States.REVERT, 
						   new MessageAction(){

							@Override
							public void processMessage(Message<?> message,
									FSMConversation conv, Transition transition) {
								//TODO
								/*Revert changes*/
								OfferMessage offerMessage = ((OfferMessage) message.getData());
								Offer trade = new Offer(offerMessage.getOfferQuantity(), offerMessage.getOfferUnitCost(), offerMessage.getOfferType(), offerMessage.getOfferInvestmentType());
								revertResponderFromInitiatorFailure(trade);
								TradeProtocol.this.tradeHistory.removeTradeHistoryWithID(offerMessage.getTradeID());
								tradeFailed(message.getFrom(),offerMessage); /*Inform responder that trade failed*/
							}
			})
			.addTransition(Transitions.CONFIRMATION,
						   new AndCondition(
								   new OrCondition(
										new MessageTypeCondition(InitiatorReplies.TRADE_UNSUCCESSFUL.name()),
										new MessageTypeCondition(InitiatorReplies.TRADE_SUCCESSFUL.name())
										), 
								   new ConversationCondition()), 
						   States.WAIT_FOR_CONFIRMATION,
						   States.TRADE_DONE, 
						   new MessageAction(){

							@Override
							public void processMessage(Message<?> message,
									FSMConversation conv, Transition transition) {
								if(message.getPerformative().equals(Performative.CONFIRM)){
									/*Inform responder that trade was successful*/
									tradeSuccessful(message.getFrom(),(OfferMessage) message.getData());
								}
								logger.info("got confirmation");
							}
			})
			.addTransition(Transitions.RESPONDER_TIME_OUT,
					new TimeoutCondition(3),
					States.WAIT_FOR_CONFIRMATION,
					States.RESPONDER_TIMEDOUT, 
					new Action(){

						@Override
						public void execute(Object event, Object entity,
								Transition transition) {
								logger.warn("Responder timed out");
								
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

		public TradeSpawnEvent(NetworkAddress with, double quantity, double unitCost, TradeType type, InvestmentType itype, OfferMessage offerMessage) {
			super(with);
			this.offerMessage = new OfferMessage(new Offer(quantity, unitCost, type, itype), offerMessage.getTradeID(), OfferMessageType.TRADE_PROTOCOL, offerMessage.getBroadCaster());
			this.offerMessage.setInitiator(TradeProtocol.this.getId());/*Set the initiator id*/
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

	public boolean offer(NetworkAddress to, double quantity, OfferMessage offerMessage)
			throws FSMException {
		if(offerMessage.getOfferMessageType().equals(OfferMessageType.BROADCAST_MESSAGE)){
			/*Start an offer if the message type is BROADCAST_MESSAGE i.e. not part of the 
			 * message passing in the TradeProtocol states*/
			this.spawnAsInititor(
					new TradeSpawnEvent(
							to, 
							quantity, offerMessage.getOfferUnitCost(), 
							offerMessage.getOfferType(), offerMessage.getOfferInvestmentType(), 
							offerMessage));
			return true;
		}else{
			/*Else let the participant know that the offer didn't go*/
			return false;
		}
	}

	protected abstract boolean acceptExchange(NetworkAddress from,
			Offer trade);
	
	protected abstract void tradeSuccessful(NetworkAddress from,
			OfferMessage offerMessage);
	
	protected abstract void tradeRejected(NetworkAddress from,
			OfferMessage offerMessage);
	
	protected abstract void tradeFailed(NetworkAddress from,
			OfferMessage offerMessage);

	/**
	 * Handle trade completion. Return false if something fucks up
	 * If things fuck up then force revert the changes for both
	 * initiator and responder. This is the case when countries are
	 * stupid enough to fuck things up!!
	 * @param trade
	 * @return
	 */
	private boolean handleTradeCompletion(Offer trade){
		try{
			switch(trade.getType()){
			case BUY:	if(this.participant.getAvailableToSpend() < trade.getTotalCost()){
							throw new NotEnoughCashException(this.participant.getAvailableToSpend(), trade.getTotalCost());
						}
						participant.receiveOffset(trade.getQuantity());
						participant.payMoney(trade.getTotalCost());
						logger.info("My name: " 
								+ this.participant.getName()
								+ ", I am buying: " 
								+ trade.getQuantity() 
								+ " and paying: " 
								+ trade.getTotalCost());
						break;

			case SELL:	participant.sellOffset(trade.getQuantity());
						participant.receiveMoney(trade.getTotalCost());
						logger.info("My name: " 
								+ this.participant.getName()
								+ ", I am selling: " 
								+ trade.getQuantity() 
								+ " and receiving: " 
								+ trade.getTotalCost());
						break;

			case INVEST:	
						if(this.participant.getAvailableToSpend() < trade.getTotalCost()){
							throw new NotEnoughCashException(this.participant.getAvailableToSpend(), trade.getTotalCost());
						}
							participant.receiveOffset(trade.getQuantity());
							participant.payMoney(trade.getTotalCost());
							logger.info("My name: " 
									+ this.participant.getName()
									+ ", I am receiving: " 
									+ trade.getQuantity() 
									+ " for my investment of: " 
									+ trade.getTotalCost());
							break;

			case RECEIVE:	participant.receiveMoney(trade.getTotalCost());

							if (trade.itype.equals(InvestmentType.ABSORB)) {
								try {
									participant.carbonAbsorptionHandler.investInCarbonAbsorption(trade.getQuantity());
								} catch (NotEnoughCashException e) {
									logger.warn(e);
									logger.warn("Country: " + participant.ISO +
											"\nAvailable to spend: " + e.getAvailableToSpend() +
											"\nInvestment required: "  + e.getInvestmentRequired());
									return false; /*Trade must fail*/
								} catch (NotEnoughLandException e) {
									logger.warn(e);
									return false; /*Trade must fail*/
								} catch (Exception e) {
									logger.warn(e);
									return false; /*Trade must fail*/
								}

								logger.info("My name: " 
										+ this.participant.getName()
										+ ", I am generating: " 
										+ trade.getQuantity() +
										" for an investment in absorption of: " 
										+ trade.getTotalCost() 
										+ ". My new absorption is " 
										+ participant.carbonAbsorption);
							}

							else if (trade.itype.equals(InvestmentType.REDUCE)) {
								try {
									participant.carbonReductionHandler.investInCarbonReduction(trade.getQuantity());
								} catch (NotEnoughCarbonOutputException e) {
									logger.warn(e);
									return false; /*Trade must fail*/
								} catch (NotEnoughCashException e) {
									logger.warn(e);
									logger.warn("Country: " + participant.ISO +
											"\nAvailable to spend: " + e.getAvailableToSpend() +
											"\nInvestment required: "  + e.getInvestmentRequired());
									return false; /*Trade must fail*/
								} catch (Exception e) {
									logger.warn(e);
									return false; /*Trade must fail*/
								}

								logger.info("My name: " 
										+ this.participant.getName()
										+ ", I am generating: " 
										+ trade.getQuantity() +
										" for an investment in reduction of: " 
										+ trade.getTotalCost() 
										+ ". My new output is " 
										+ participant.carbonOutput);
							}
							break;
			}
		}catch(NotEnoughCashException e){
			/*Trade MUST FAIL*/
			logger.warn(e);
			logger.warn("Country: " + participant.ISO +
					"\nAvailable to spend: " + e.getAvailableToSpend() +
					"\nInvestment required: "  + e.getInvestmentRequired());
			return false;
		}
		
		return true; /*Trade Passes*/
	}
	
	/**
	 * Revert responder if Responder fails to handle completion
	 * @param trade
	 */
	private void revertResponder(Offer trade){
		if(trade.getType().equals(TradeType.RECEIVE)){
			participant.payMoney(trade.getTotalCost());
		}
		
		/*handle none of the other TradeTypes because they
		 * are handled in the handleTradeCompletion at this stage at least.*/
	}
	
	/**
	 * Revert responder if initiator responds that it fucked up!
	 * @param trade
	 */
	private void revertResponderFromInitiatorFailure(Offer trade){
		switch(trade.getType()){
		case BUY:
			/*If the responder trade type was BUY then
			 * 1) sellOffset gained
			 * 2) receive money that was payed*/
				participant.sellOffset(trade.getQuantity());
				participant.receiveMoney(trade.getTotalCost());
				break;

		case SELL:
			/*If the responder trade type was SELL then
			 * 1) receive the offset given away
			 * 2) give the money back that was owned*/
				participant.receiveOffset(trade.getQuantity());
				participant.payMoney(trade.getTotalCost());
				break;
		case INVEST:
			/*If the responder trade type was INVEST then
			 * 1) sell offset that was bought
			 * 2) receive money that was given away*/
				participant.sellOffset(trade.getQuantity());
				participant.receiveMoney(trade.getTotalCost());
				break;
		case RECEIVE:
			/*If the responder trade type was RECEIVE then
			 * 1) pay the money back
			 * 2) Revert absorbtion if InvestmentType was ABSORB
			 * 3) Revert reduction if InvestmentType was REDUCE*/
				participant.payMoney(trade.getTotalCost());
				if (trade.itype.equals(InvestmentType.ABSORB)){
					try {
						double investmentAmount = participant.carbonAbsorptionHandler.getInvestmentRequired(trade.getQuantity());
						double areaRequired = participant.carbonAbsorptionHandler.getForestAreaRequired(trade.getQuantity());
						this.participant.availableToSpend += investmentAmount;
						this.participant.carbonAbsorption -= trade.getQuantity();
						this.participant.arableLandArea += areaRequired;
					} catch (Exception e) {
						/*NOT SUPPOSED TO REACH HERE*/
						logger.warn(e);
					}
				}else if (trade.itype.equals(InvestmentType.REDUCE)){
					double investmentAmount;
					try {
						investmentAmount = participant.carbonReductionHandler.getInvestmentRequired(trade.getQuantity());
						this.participant.availableToSpend += investmentAmount;
						this.participant.carbonOutput += trade.getQuantity();
					} catch (Exception e) {
						/*NOT SUPPOSED TO REACH HERE*/
						logger.warn(e);
					}
				}
		}
		
	}
	
	/**
	 * Revert initiator when initiator fucked up!
	 * @param trade
	 */
	private void revertInitiator(Offer trade){
		if(trade.getType().equals(TradeType.RECEIVE)){
			/*Pay the money back that the initiator just took*/
			participant.payMoney(trade.getTotalCost());
		}
		
		/*handle none of the other TradeTypes because they
		 * are handled in the handleTradeCompletion at this stage at least.*/
	}

	public UUID getId() {
		return id;
	}

	public UUID getAuthkey() {
		return authkey;
	}
	
	/*UTILITY FUNCTIONS*/
	/**
	 * Static function that decodes an object of type
	 * Input to type OfferMessage
	 * @param in
	 * @return a decoded Input in to OfferMessage
	 * @throws ClassCastException
	 * @throws Exception
	 */
	public OfferMessage decodeInput(Input in) throws ClassCastException, IllegalArgumentException{
		if(in instanceof Message){
				@SuppressWarnings("unchecked")
				Message<OfferMessage> m = (Message<OfferMessage>) in;
				OfferMessage o = m.getData();
				return o;

		}else{
			throw new IllegalArgumentException("Input not instanceof Message");
		}
	}
	
	/**
	 * Function allowing participants to respond to offers without
	 * doing all the internal conversions
	 * @param from
	 * @param quantity
	 * @param unitcost
	 * @param o
	 */
	public void respondToOffer(NetworkAddress from, double quantity, OfferMessage o) throws FSMException, IllegalArgumentException{
		if(this.getActiveConversationMembers().contains(from)){
			throw new IllegalArgumentException("A conversation with this agent already exists");
		} else {
			this.offer(
					from, 
					quantity, 
					o);
		}
	}
	
	public NetworkAddress extractNetworkAddress(Input in) throws IllegalArgumentException{
		if(in instanceof Message){
			Message<?> message = (Message<?>) in;
			return message.getFrom();
		}else{
			throw new IllegalArgumentException("Argument not an instance of Message");
		}
	}
}