package uk.ac.ic.kyoto.market;

import java.util.Random;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;

/**
 * 
 * @author Adam Zysko
 */
public class Economy extends EnvironmentService
{
	@Inject
	public Economy(EnvironmentSharedStateAccess sharedState) {
		super(sharedState);
		currentEconomyState=State.GROWTH;
	}

	public enum State {
		GROWTH,
		STABLE,
		RECESSION
	}
	private Logger logger = Logger.getLogger(Economy.class);
	
	private State currentEconomyState;

	// Values used for determining which State is defined by a given random number.
	private final int growthNumberLimit = (int) (100 * GameConst.GROWTH_MARKET_CHANCE);
	private final int stableNumberLimit = (int) (growthNumberLimit + 100 * GameConst.STABLE_MARKET_CHANCE);
	private final int recessionNumberLimit = (int) (stableNumberLimit + 100 * GameConst.RECESSION_MARKET_CHANCE);
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		// TODO Auto-generated method stub
		super.registerParticipant(req);
	}
	
	/**
	 * Function changes the State of Economy for a given year 
	 * using the probabilities defined in Game Constants
	 */
	public void updateEconomyState()
	{
		/** Generate a random number between 0 and 99*/
		Random randGenerator = new Random();
		int randomNumber = randGenerator.nextInt(100);
		
		/** Change the currentEconomyState according to the rules */
		if (randomNumber < growthNumberLimit)
			currentEconomyState = State.GROWTH;
		else if (randomNumber < stableNumberLimit)
			currentEconomyState = State.STABLE;
		else if (randomNumber < recessionNumberLimit)
			currentEconomyState = State.RECESSION;
		else
			logger.warn("Generated random number is out of bounds");
		
		/** Log the change in economy state */
		logger.info("The new state of economy is " + currentEconomyState.toString());
	}
	
	public State getEconomyState() {
		return currentEconomyState;
	}
}
