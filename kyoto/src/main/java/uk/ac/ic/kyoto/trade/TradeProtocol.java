package uk.ac.ic.kyoto.trade;

import org.apache.log4j.Logger;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
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
 * Really unsure about this: taken from https://github.com/Presage/HelloWorld/blob/master/src/main/java/uk/ac/imperial/presage2/helloworld/HelloWorldProtocol.java
 * 
 * This is just a really simple implementation where one party can only ever accept a trade
 * 
 * @author cmd08
 *
 */
public abstract class TradeProtocol extends FSMProtocol {
	
	private final String name;
	
	private final Logger logger;
	
	enum States {
		START, TRADE_PROPOSED, RESPONSE_RECEIVED, TRADE_RECEIVED, TRADE_ACCEPTED, TRADE_REJECTED,
		//PROPOSITION_PUBLISHED, OFFERS_RECEIVED, OFFER_ACCEPTED, 
		//PROPOSITION_RECEIVED, OFFERS_PUBLISHED, TRADE_COMPLETE, 
		TIMED_OUT
	};
	
	enum Transitions {
		PROPOSE_TRADE, RECEIVE_RESPONSE, RECIEVE_TRADE, ACCEPT_TRADE, REJECT_TRADE,
		//PUBLISH_PROPOSITION, RECEIVE_OFFERS, ACCEPT_OFFER, REJECT_OFFER, 
		//RECEIVE_PROPOSITIONS, PUBLISH_OFFERS, COMPLETE_TRADE,
		TIMEOUT
	}

	public TradeProtocol(String agentName, NetworkAdaptor network) {
		super("Trade Protocol", FSM.description(), network);
		this.name = agentName;
		
		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + name);
		
			try {
				this.description.addState(States.START, StateType.START)
				
					/* Initiator FSM */
					.addState(States.TRADE_PROPOSED)
					.addState(States.RESPONSE_RECEIVED)
					.addState(States.TRADE_ACCEPTED, StateType.END)
					.addState(States.TRADE_REJECTED, StateType.END)
					.addState(States.TIMED_OUT, StateType.END)
					.addTransition(Transitions.PROPOSE_TRADE,
							new EventTypeCondition(ConversationSpawnEvent.class), 
							States.START, States.TRADE_PROPOSED, 
							new SpawnAction() {

								@Override
								public void processSpawn(ConversationSpawnEvent event,
										FSMConversation conv, Transition transition) {
									
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
							})
					.addTransition(Transitions.RECEIVE_RESPONSE,
							new AndCondition(new MessageTypeCondition("ACCEPT"),
								new ConversationCondition()),
							States.TRADE_PROPOSED, States.TRADE_ACCEPTED,
							new MessageAction() {
								
								@Override
								public void processMessage(Message<?> message, FSMConversation conv,
										Transition transition) {
									// TODO Auto-generated method stub
									logger.info("Trade was accepted");
							}
					})
					.addTransition(Transitions.RECEIVE_RESPONSE,
							new AndCondition(new MessageTypeCondition("REJECT"),
								new ConversationCondition()),
							States.TRADE_PROPOSED, States.TRADE_REJECTED,
							new MessageAction() {
								
								@Override
								public void processMessage(Message<?> message, FSMConversation conv,
										Transition transition) {
									// TODO Auto-generated method stub
									logger.info("Trade was accepted");
							}
					})
					/* Non-initiator FSM */
					.addTransition(Transitions.RECIEVE_TRADE, 
							new AndCondition(new MessageTypeCondition("TRADE")),
							States.START,
							States.TRADE_ACCEPTED, new InitialiseConversationAction() {
								
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
							}).build();
			} catch (FSMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		
		
	}

}
