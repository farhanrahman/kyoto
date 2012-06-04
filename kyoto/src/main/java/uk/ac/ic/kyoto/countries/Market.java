package uk.ac.ic.kyoto.countries;

import java.util.Random;
import org.apache.log4j.Logger;

/**
 * 
 * @author Adam Zysko
 */

enum EconomyState {
	GROWTH,
	STABLE,
	RECESSION
}

public class Market
{
	static Logger logger = Logger.getLogger(Market.class);
	
	private static EconomyState currentEconomyState;

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
				currentEconomyState = EconomyState.GROWTH;
			else if (randomNumber < stableNumberLimit)
				currentEconomyState = EconomyState.STABLE;
			else if (randomNumber < recessionNumberLimit)
				currentEconomyState = EconomyState.RECESSION;
			else
				logger.warn("Generated random number is out of bounds");
			
			/** Log the change in economy state */
			logger.info("The new state of economy is " + currentEconomyState.toString());
		}
		catch (Exception e){
			logger.warn(e);
		}
	}
	
	public static EconomyState getEconomyState() {
		return currentEconomyState;
	}
}
