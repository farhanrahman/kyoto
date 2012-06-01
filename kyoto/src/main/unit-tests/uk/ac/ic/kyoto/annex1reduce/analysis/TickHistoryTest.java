package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

public class TickHistoryTest {

	@Test
	public void testTickHistory() {
		TickHistory t = new TickHistory(0);
		assertTrue(t.getInvestmentHigh() == Float.MIN_VALUE);
		assertTrue(t.getTradeHigh() == Float.MIN_VALUE);
		assertTrue(t.getInvestmentLow() == Float.MAX_VALUE);
		assertTrue(t.getTradeLow() == Float.MAX_VALUE);
		assertTrue(t.getTickId() == 0);
	}

	@Test
	public void testAddMessage() {
		TickHistory			t	= new TickHistory(0);
		Message				m1	= new Message(1, 1);
		TradeMessage		m2	= new TradeMessage(1, 1);
		InvestmentMessage	m3	= new InvestmentMessage(1, 1);
		
		try {
			t.addMessage(m1);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding Message");
		}
		
		try {
			t.addMessage(m2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding TradeMessage");
		}	
		
		try {
			t.addMessage(m3);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding InvestmentMessage");
		}	
	}

	@Test
	public void testGetMessages() {
		TickHistory			t	= new TickHistory(0);

		TradeMessage		m2	= new TradeMessage(1, 1);
		InvestmentMessage	m3	= new InvestmentMessage(1, 1);
		
		try {
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		Iterator<Message> i = t.getMessages();
		assertTrue("Could not get TradeMessage", i.next().equals(m2));
		assertTrue("Could not get InvestmentMessage", i.next().equals(m3));
		
	}

	@Test
	public void testGetTradeHigh() {
		TickHistory			t	= new TickHistory(0);

		TradeMessage		m1	= new TradeMessage(1, 1);		
		TradeMessage		m2 = new TradeMessage(1, 10);
		
		// InvestmentMessage added to ensure it does not alter correct results
		InvestmentMessage	m3 = new InvestmentMessage(1, 100);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue("Incorrect TradeHigh value", t.getTradeHigh() == (float) 10);
		
	}

	@Test
	public void testGetTradeLow() {
		TickHistory			t	= new TickHistory(0);

		TradeMessage		m1	= new TradeMessage(1, 1);
		TradeMessage		m2	= new TradeMessage(10, 1);
		
		// InvestmentMessage added to ensure it does not alter correct results
		InvestmentMessage	m3	= new InvestmentMessage(100, 1);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
				
		assertTrue("Incorrect TradeLow value", t.getTradeLow() == (float) 0.1);
	}

	@Test
	public void testGetInvestmentHigh() {
		TickHistory			t	= new TickHistory(0);

		InvestmentMessage	m1	= new InvestmentMessage(1, 1);
		InvestmentMessage	m2 = new InvestmentMessage(1, 10);
		
		// TradeMessage added to ensure it does not alter correct results
		TradeMessage		m3 = new TradeMessage(1, 100);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue("Incorrect TradeHigh value", t.getInvestmentHigh() == (float) 10);
	}

	@Test
	public void testGetInvestmentLow() {
		TickHistory			t	= new TickHistory(0);
		
		InvestmentMessage	m1	= new InvestmentMessage(1, 1);
		InvestmentMessage	m2	= new InvestmentMessage(10, 1);
		
		// TradeMessage added to ensure it does not alter correct results
		TradeMessage		m3	= new TradeMessage(100, 1);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue("Incorrect TradeLow value", t.getInvestmentLow() == (float) 0.1);
	}

	@Test
	public void testGetTickId() {
		TickHistory	t	= new TickHistory(0);
		assertTrue(t.getTickId() == 0);
	}

	@Test
	public void testGetTradeAverage() {
		TickHistory			t	= new TickHistory(0);
		
		TradeMessage		m1	= new TradeMessage(1, 4);
		TradeMessage		m2	= new TradeMessage(1, 5);
		TradeMessage		m3	= new TradeMessage(1, 6);
		
		// InvestmentMessage added to ensure it does not alter correct results
		InvestmentMessage	m4	= new InvestmentMessage(1, 100);
		
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
		TickHistory			t	= new TickHistory(0);
		
		InvestmentMessage	m1	= new InvestmentMessage(1, 4);
		InvestmentMessage	m2	= new InvestmentMessage(1, 5);
		InvestmentMessage	m3	= new InvestmentMessage(1, 6);
		
		// TradeMessage added to ensure it does not alter correct results
		TradeMessage		m4	= new TradeMessage(1, 100);
		
		try {
			t.addMessage(m1);
			t.addMessage(m2);
			t.addMessage(m3);
			t.addMessage(m4);
		} catch (Exception e) {
			fail("Exception while adding messages");
		}
		
		assertTrue(t.getInvestmentAverage() == (float) 5);
	}

}
