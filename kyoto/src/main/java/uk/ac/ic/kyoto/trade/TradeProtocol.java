package uk.ac.ic.kyoto.trade;

import org.apache.log4j.Logger;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.MulticastMessage;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.FSMDescription;
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
 * @author cmd08
 *
 */
public class TradeProtocol extends FSMProtocol {
	
	private final String name;
	
	private final Logger logger;
	
	enum States {
		START, 
		PROPOSITION_PUBLISHED, OFFERS_RECEIVED, OFFER_ACCEPTED, 
		PROPOSITION_RECEIVED, OFFERS_PUBLISHED, TRADE_COMPLETE, 
		TIMED_OUT
	};
	
	enum Transitions {
		PUBLISH_PROPOSITION, RECEIVE_OFFERS, ACCEPT_OFFER, REJECT_OFFER, 
		RECEIVE_PROPOSITIONS, PUBLISH_OFFERS, COMPLETE_TRADE,
		TIMEOUT
	}

	public TradeProtocol(String agentName, NetworkAdaptor network) {
		super("Trade Protocol", FSM.description(), network);
		this.name = agentName;
		
		logger = Logger.getLogger(TradeProtocol.class.getName() + ", " + name);
		
		try{
			this.description.addState(States.START, StateType.START)
			
				/* Initiator FSM */
				.addState(States.PROPOSITION_PUBLISHED)
				.addState(States.OFFERS_RECEIVED)
				.addState(States.OFFER_ACCEPTED, StateType.END)
				.addState(States.TIMED_OUT, StateType.END)
				.addTransition(Transitions.PUBLISH_PROPOSITION,
						new EventTypeCondition(ConversationSpawnEvent.class), 
						States.START, States.PROPOSITION_PUBLISHED, new SpawnAction() {
							
							@Override
							public void processSpawn(ConversationSpawnEvent event,
									FSMConversation conv, Transition transition) {
								
								logger.info("Publishing proposition");
								conv.getNetwork().sendMessage(
									new MulticastMessage<Object>(Performative.PROPOSE, "PROPOSITION", SimTime.get(), conv.getNetwork().getAddress(), new TradeMessage()));			
									
									
									// TODO Auto-generated method stub
								
							}
						})
				.addTransition(Transitions.RECEIVE_OFFERS,
						new AndCondition(new MessageTypeCondition("OFFER"),
							new ConversationCondition()),
						States.PROPOSITION_PUBLISHED, States.OFFERS_RECEIVED,
						new MessageAction() {
							
							@Override
							public void processMessage(Message<?> message, FSMConversation conv,
									Transition transition) {
								// TODO Auto-generated method stub
								logger.info("Received an Offer");
						}
				})
				.addState(States.PROPOSITION_RECEIVED)
				.addState(States.OFFERS_PUBLISHED)
				.addState(States.TRADE_COMPLETE)
				.addTransition(Transitions.RECEIVE_PROPOSITIONS, 
						new MessageTypeCondition("PROPOSITION"), 
						States.START,
						States.PROPOSITION_RECEIVED,
						new InitialiseConversationAction() {
							
							@Override
							public void processInitialMessage(Message<?> message, FSMConversation conv,
									Transition transition) {
								logger.info("Propositions received")
								//Logic here to decide if offer made?
								conv.getNetwork().sendMessage(m)
								
							}
						})
				
				/* Receiver FSM */
				
		} catch {
			//all!
		} 
		
	}

}
