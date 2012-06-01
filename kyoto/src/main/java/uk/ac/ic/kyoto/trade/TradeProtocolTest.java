/**
 * 
 */
package uk.ac.ic.kyoto.trade;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author cmd08
 *
 */
public class TradeProtocolTest {

	@Test
	public void test() {
		int quantity = 10;
		int unitCost = 10;
		TradeProtocol.Trade buy = new TradeProtocol.Trade(quantity, unitCost, TradeType.BUY);
		TradeProtocol.Trade sell = new TradeProtocol.Trade(quantity, unitCost, TradeType.SELL);
		/* sanity check: are these actually the same instances!? */
		assertFalse(sell == buy);

		assertTrue(buy.reverse().reverse().equals(buy));
		assertTrue(sell.reverse().equals(buy));
		
		
	}

}
