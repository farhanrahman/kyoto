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
		
		Iterator<Trade> i = t.getMessages();
		//TODO This would be better if Trade implemented equals()

		Trade temp = i.next();
		System.out.println("m1:" + temp.getQuantity() + temp.getUnitCost());
		assertTrue(temp.getQuantity() == 1);
		assertTrue(temp.getUnitCost() == 2);
		
		temp = i.next();
		System.out.println("m2:" + temp.getQuantity() + temp.getUnitCost());
		assertTrue(temp.getQuantity() == 3);
		assertTrue(temp.getUnitCost() == 4);
		
		assertTrue(i.hasNext() == false);
		
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
		TickHistory			t	= new TickHistory(0);
		
		Trade				m1	= new Trade(1, 4, TradeType.BUY);
		Trade				m2	= new Trade(1, 5, TradeType.BUY);
		Trade				m3	= new Trade(1, 6, TradeType.BUY);
		
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

}
