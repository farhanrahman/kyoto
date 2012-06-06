/**
 * 
 */
package uk.ac.ic.kyoto.tokengen;

import java.util.UUID;

import uk.ac.ic.kyoto.trade.TradeToken;
import uk.ac.imperial.presage2.core.util.random.Random;

/**
 * @author farhanrahman
 *
 */
public class TradeTokenGenerator implements TradeToken {

	private static UUID tradeID = null;
	/* (non-Javadoc)
	 * @see uk.ac.ic.kyoto.trade.TradeToken#getToken()
	 */
	@Override
	public UUID getToken() {
		TradeTokenGenerator.tradeID = Random.randomUUID();
		return TradeTokenGenerator.tradeID;
	}
}
