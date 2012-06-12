/**
 * 
 */
package uk.ac.ic.kyoto.singletonfactory;

import java.util.UUID;

import com.google.inject.Singleton;

import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * Class that implements Token interface
 * This generates a pseudo random UUID
 * in a thread safe method. This is a
 * singleton class so implementation needs
 * to be thread safe.
 * @author farhanrahman
 *
 */
@Singleton
public class TokenGenerator implements Token {

	private static UUID tradeID = Random.randomUUID();
	/* (non-Javadoc)
	 * @see uk.ac.ic.kyoto.trade.TradeToken#getToken()
	 */
	@Override
	public UUID generate() {
		synchronized(TokenGenerator.tradeID){
			TokenGenerator.tradeID = Random.randomUUID();
		}
		return TokenGenerator.tradeID;
	}
}
