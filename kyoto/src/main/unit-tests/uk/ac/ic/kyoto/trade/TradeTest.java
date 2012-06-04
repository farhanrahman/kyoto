package uk.ac.ic.kyoto.trade;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;

public class TradeTest {

	@Test
	public void testHashCode() {
		Trade t1 = new Trade(1, 2, TradeType.BUY);
		Trade t2 = new Trade(1, 2, TradeType.BUY);
		Trade t3 = new Trade(1, 2, TradeType.SELL);
		Trade t4 = new Trade(1, 0, TradeType.BUY);
		Trade t5 = new Trade(0, 2, TradeType.BUY);
		
		assertTrue(t1.hashCode() == t2.hashCode());
		assertTrue(t1.hashCode() != t3.hashCode());
		assertTrue(t1.hashCode() != t4.hashCode());
		assertTrue(t1.hashCode() != t5.hashCode());
	}

	@Test
	public void testGetTotalCost() {
		Trade t = new Trade(10, 69, TradeType.BUY);
		assertTrue(t.getTotalCost() == 690);
	}

	@Test
	public void testReverse() {
		Trade t1 = new Trade(1, 2, TradeType.BUY);
		Trade t2 = new Trade(1, 2, TradeType.SELL);
		
		assertTrue(t1.reverse().equals(t2));
	}

	@Test
	public void testEqualsObject() {
		Trade t1 = new Trade(1, 2, TradeType.BUY);
		Trade t2 = new Trade(1, 2, TradeType.BUY);
		Trade t3 = new Trade(1, 2, TradeType.SELL);
		Trade t4 = new Trade(1, 0, TradeType.BUY);
		Trade t5 = new Trade(0, 2, TradeType.BUY);
		String s = new String();
		
		assertTrue(t1.equals(t2));
		assertTrue(t1.equals(t3) == false);
		assertTrue(t1.equals(t4) == false);
		assertTrue(t1.equals(t5) == false);
		assertTrue(t1.equals(s) == false);
	}

}
