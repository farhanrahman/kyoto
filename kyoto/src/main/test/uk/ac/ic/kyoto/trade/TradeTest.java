package uk.ac.ic.kyoto.trade;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ic.kyoto.countries.Offer;

public class TradeTest {

	@Test
	public void testHashCode() {
		Offer t1 = new Offer(1, 2, TradeType.BUY);
		Offer t2 = new Offer(1, 2, TradeType.BUY);
		Offer t3 = new Offer(1, 2, TradeType.SELL);
		Offer t4 = new Offer(1, 0, TradeType.BUY);
		Offer t5 = new Offer(0, 2, TradeType.BUY);
		
		assertTrue(t1.hashCode() == t2.hashCode());
		assertTrue(t1.hashCode() != t3.hashCode());
		assertTrue(t1.hashCode() != t4.hashCode());
		assertTrue(t1.hashCode() != t5.hashCode());
	}

	@Test
	public void testGetTotalCost() {
		Offer t = new Offer(10, 69, TradeType.BUY);
		assertTrue(t.getTotalCost() == 690);
	}

	@Test
	public void testReverse() {
		Offer t1 = new Offer(1, 2, TradeType.BUY);
		Offer t2 = new Offer(1, 2, TradeType.SELL);
		
		assertTrue(t1.reverse().equals(t2));
	}

	@Test
	public void testEqualsObject() {
		Offer t1 = new Offer(1, 2, TradeType.BUY);
		Offer t2 = new Offer(1, 2, TradeType.BUY);
		Offer t3 = new Offer(1, 2, TradeType.SELL);
		Offer t4 = new Offer(1, 0, TradeType.BUY);
		Offer t5 = new Offer(0, 2, TradeType.BUY);
		String s = new String();
		
		//Reflexive
		assertTrue(t1.equals(t1));
		
		//Symmetric
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));
		
		//Transitive
		
		//Test Failure
		assertTrue(t1.equals(t3) == false);
		assertTrue(t1.equals(t4) == false);
		assertTrue(t1.equals(t5) == false);
		assertTrue(t1.equals(s) == false);
		assertTrue(t1.equals(null) == false);
	}

}
