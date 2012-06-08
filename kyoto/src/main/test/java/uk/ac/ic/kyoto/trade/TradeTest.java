/**
 * @author cmd08
 *
 */
package java.uk.ac.ic.kyoto.trade;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.ic.kyoto.trade.Trade;

public class TradeTest {

	@Test
	public void test() {
		/* Test the Trade class first*/
		int quantity = 10;
		int unitCost = 10;
		UUID id = UUID.randomUUID();
		Trade buy = new Trade(quantity, unitCost, TradeType.BUY, id);
		Trade sell = new Trade(quantity, unitCost, TradeType.SELL, id);
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
