package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

public class SessionHistoryTest {

	@Test
	public void testAdd() {
		SessionHistory		s	= new SessionHistory(0);
		Message				m1	= new Message(1, 1);
		TradeMessage		m2	= new TradeMessage(1, 1);
		InvestmentMessage	m3	= new InvestmentMessage(1, 1);
		
		try {
			s.add(m1, 0);
		} catch (Exception e) {
			e.printStackTrace();
			//fail("Exception while adding Message");
		}
		
		try {
			s.add(m2, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding TradeMessage");
		}	
		
		try {
			s.add(m3, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding InvestmentMessage");
		}
		
		try {
			s.add(m1, 1);
		} catch (Exception e) {
			e.printStackTrace();
			//fail("Exception while adding Message");
		}
		
		try {
			s.add(m2, 1);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding TradeMessage");
		}	
		
		try {
			s.add(m3, 1);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while adding InvestmentMessage");
		}	
	}

	@Test
	public void testGetTick() {
		SessionHistory		s	= new SessionHistory(0);
		
		TradeMessage		m1	= new TradeMessage(1, 1);
		InvestmentMessage	m2	= new InvestmentMessage(1, 1);
		
		TickHistory t1 = new TickHistory(0);
		TickHistory t2 = new TickHistory(1);
		
		try {
			s.add(m1, 0);
			s.add(m2, 0);
			s.add(m1, 1);
			s.add(m2, 2);
		} catch (Exception e) {
			fail("Exception while adding messages to session");
		}
		
		try {
			t1.addMessage(m1);
			t1.addMessage(m2);
			t2.addMessage(m1);
			t2.addMessage(m2);
		} catch (Exception e) {
			fail("Exception while adding messages to tick");
		}
		
		//TODO Add equals method to TickHistory to enable correct testing
		assertTrue(s.getTick(0).equals(t1));
		assertTrue(s.getTick(1).equals(t2));
		
	}

	@Test
	public void testGetSession() {
		//TODO Add equals method to SessionHistory to enable correct testing
		fail("Not yet implemented. Requires implementation of equals()");
	}

}
