/**
 * 
 */
package uk.ac.ic.kyoto.tokengen;

import java.util.UUID;

import com.google.inject.Singleton;

import uk.ac.imperial.presage2.core.util.random.Random;

/**
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
	public UUID getToken() {
		synchronized(TokenGenerator.tradeID){
			TokenGenerator.tradeID = Random.randomUUID();
		}
		return TokenGenerator.tradeID;
	}
}
