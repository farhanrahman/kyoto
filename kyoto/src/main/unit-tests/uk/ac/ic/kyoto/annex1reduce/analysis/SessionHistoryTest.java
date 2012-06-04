package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;
import uk.ac.ic.kyoto.trade.TradeType;

public class SessionHistoryTest {

	@Test
	public void testAdd() {
		SessionHistory	s	= new SessionHistory(0);
		
		Trade	m1	= new Trade(1, 1, TradeType.BUY);
		Trade	m2	= new Trade(1, 2, TradeType.BUY);
		Trade	m3	= new Trade(1, 3, TradeType.BUY);
		Trade	m4	= new Trade(1, 4, TradeType.BUY);
		
		try {
			s.add(m1, 0);
			s.add(m2, 0);
			s.add(m3, 1);
			s.add(m4, 1);
		} catch (Exception e) {
			fail("Exception while adding Trade");
		}
	}

	@Test
	public void testGetTick() {
		SessionHistory		s	= new SessionHistory(0);
		
		Trade				m1	= new Trade(1, 1, TradeType.BUY);
		Trade				m2	= new Trade(1, 2, TradeType.BUY);
		
		TickHistory			t1	= new TickHistory(0);
		TickHistory			t2	= new TickHistory(1);
		
		try {
			s.add(m1, 0);
			s.add(m2, 0);
			s.add(m1, 1);
			s.add(m2, 1);
		} catch (Exception e) {
			fail("Exception while adding Trades to SessionHistory");
		}
		
		try {
			t1.addMessage(m1);
			t1.addMessage(m2);
			t2.addMessage(m1);
			t2.addMessage(m2);
		} catch (Exception e) {
			fail("Exception while adding Trades to TickHistory");
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
