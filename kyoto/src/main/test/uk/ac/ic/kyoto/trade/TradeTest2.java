/**
 * @author cmd08
 *
 */
package uk.ac.ic.kyoto.trade;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.trade.TradeType;

public class TradeTest2 {

	@Test
	public void test() {
		/* Test the Trade class first*/
		int quantity = 10;
		int unitCost = 10;
		Offer buy = new Offer(quantity, unitCost, TradeType.BUY);
		Offer sell = new Offer(quantity, unitCost, TradeType.SELL);
		/* sanity check: are these actually the same instances!? */
		assertFalse(sell == buy);

		assertTrue(buy.reverse().reverse().equals(buy));
		assertTrue(sell.reverse().equals(buy));
		
		
		/* 
		 * Test the protocol - Need an agent, environment and network to test this class :(
		 */
		
//		TradeProtocol protocol;
//		
//		try {
//			protocol = new TradeProtocol(null, null, null, null) {
//				
//				@Override
//				protected boolean acceptExchange(NetworkAddress from, Trade trade) {
//					// TODO Auto-generated method stub
//					return false;
//				}
//			};
//		} catch (FSMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

}
