package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;
import uk.ac.ic.kyoto.trade.TradeType;

public class TickHistoryTest {

	@Test
	public void testTickHistory() {
		TickHistory t = new TickHistory(0);
		
		assertTrue(t.getTradeHigh() == Integer.MIN_VALUE);
		assertTrue(t.getTradeLow() == Integer.MAX_VALUE);
		
		//assertTrue(t.getInvestmentLow() == Integer.MAX_VALUE);
		//assertTrue(t.getInvestmentHigh() == Integer.MIN_VALUE);
		
		assertTrue(t.getTickId() == 0);
	}

	@Test
	public void testAddMessage() {
		TickHistory			t	= new TickHistory(0);
		Trade				m1	= new Trade(1, 1, TradeType.BUY);
		
		try {
			t.addMessage(m1);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding Trade");
		}
	}

	@Test
	public void testGetMessages() {
		TickHistory	t	= new TickHistory(0);
		
		Trade		m1	= new Trade(1, 2, TradeType.BUY);
		Trade		m2	= new Trade(3, 4, TradeType.BUY);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		Iterator<Trade> messages = t.getMessages().iterator();
		
		assertTrue(messages.next().equals(m1));
		assertTrue(messages.next().equals(m2));		
		assertTrue(messages.hasNext() == false);
		
	}

	@Test
	public void testGetTradeHigh() {
		TickHistory			t	= new TickHistory(0);
		
		Trade				m1	= new Trade(1, 1, TradeType.BUY);
		Trade				m2	= new Trade(1, 100, TradeType.SELL);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue("Incorrect TradeHigh value", t.getTradeHigh() == (float) 100);
		
	}

	@Test
	public void testGetTradeLow() {
		TickHistory	t	= new TickHistory(0);
		
		Trade		m1	= new Trade(1, 2, TradeType.BUY);
		Trade		m2	= new Trade(1, 100, TradeType.BUY);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue("Incorrect TradeLow value", t.getTradeLow() == (float) 2);
	}

	@Test
	public void testGetInvestmentHigh() {
		fail("Investment message object not yet implemented: Cannot test.");
	}

	@Test
	public void testGetInvestmentLow() {
		fail("Investment message object not yet implemented: Cannot test.");
	}

	@Test
	public void testGetTickId() {
		TickHistory	t	= new TickHistory(0);
		assertTrue(t.getTickId() == 0);
	}

	@Test
	public void testGetTradeAverage() {
		TickHistory	t	= new TickHistory(0);
		
		Trade		m1	= new Trade(1, 4, TradeType.BUY);
		Trade		m2	= new Trade(1, 5, TradeType.BUY);
		Trade		m3	= new Trade(1, 6, TradeType.BUY);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue(t.getTradeAverage() == (float) 5);
	}

	@Test
	public void testGetInvestmentAverage() {
		fail("Investment message object not yet implemented: Cannot test.");
	}
	
	@Test
	public void testEquals(){
		TickHistory t1 = new TickHistory(0);
		TickHistory t2 = new TickHistory(0);
		TickHistory t3 = new TickHistory(0);
		
		Trade		m1	= new Trade(1, 4, TradeType.BUY);
		Trade		m2	= new Trade(1, 5, TradeType.BUY);
		Trade		m3	= new Trade(1, 6, TradeType.BUY);
		
		try {
			t1.addMessage(m1);
			t1.addMessage(m2);
			t1.addMessage(m3);
			
			t2.addMessage(m1);
			t2.addMessage(m2);
			t2.addMessage(m3);
			
			t3.addMessage(m1);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue(t1.equals(t2));
		assertTrue(t1.equals(t3) == false);
		
	}
	
	@Test
	public void testHashCode(){
		TickHistory t1 = new TickHistory(0);
		TickHistory t2 = new TickHistory(0);
		TickHistory t3 = new TickHistory(0);
		
		Trade		m1	= new Trade(1, 4, TradeType.BUY);
		Trade		m2	= new Trade(1, 5, TradeType.BUY);
		Trade		m3	= new Trade(1, 6, TradeType.BUY);
		
		try {
			t1.addMessage(m1);
			t1.addMessage(m2);
			t1.addMessage(m3);
			
			t2.addMessage(m1);
			t2.addMessage(m2);
			t2.addMessage(m3);
			
			t3.addMessage(m1);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue(t1.hashCode() == t2.hashCode());
		assertTrue(t1.hashCode() != t3.hashCode());
		
	}

}
