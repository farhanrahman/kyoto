package uk.ac.ic.kyoto.market;

import java.util.Random;
import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.countries.GameConst;

/**
 * 
 * @author Adam Zysko
 */


public class Economy
{
	public enum State {
		GROWTH,
		STABLE,
		RECESSION
	}
	private static Logger logger = Logger.getLogger(Economy.class);
	
	private static State currentEconomyState;

	// Values used for determining which State is defined by a given random number.
	private final static int growthNumberLimit = (int) (100 * GameConst.GROWTH_MARKET_CHANCE);
	private final static int stableNumberLimit = (int) (growthNumberLimit + 100 * GameConst.STABLE_MARKET_CHANCE);
	private final static int recessionNumberLimit = (int) (stableNumberLimit + 100 * GameConst.RECESSION_MARKET_CHANCE);
	
	/**
	 * Function changes the State of Economy for a given year 
	 * using the probabilities defined in Game Constants
	 */
	public static void updateEconomyState()
	{
		try
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
		catch (Exception e){
			logger.warn(e);
		}
	}
	
	public static State getEconomyState() {
		return currentEconomyState;
	}
}
