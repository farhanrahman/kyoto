package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
			e.printStackTrace();
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
		
		assertTrue(s.getTick(0).equals(t1));		
		assertTrue(s.getTick(1).equals(t2));
		
	}

	@Test
	public void testHashCode() {
		SessionHistory		s1	= new SessionHistory(0);
		SessionHistory		s2	= new SessionHistory(0);
		SessionHistory		s3	= new SessionHistory(0);
		SessionHistory		s4	= new SessionHistory(1);
		
		Trade				m1	= new Trade(1, 1, TradeType.BUY);
		Trade				m2	= new Trade(1, 2, TradeType.BUY);
		Trade				m3	= new Trade(1, 3, TradeType.BUY);
		Trade				m4	= new Trade(1, 4, TradeType.BUY);
		
		try {
			s1.add(m1, 0);
			s1.add(m2, 0);
			s1.add(m3, 1);
			s1.add(m4, 1);
			
			s2.add(m1, 0);
			s2.add(m2, 0);
			s2.add(m3, 1);
			s2.add(m4, 1);
			
			s3.add(m1, 0);
			s3.add(m2, 0);			
			
		} catch (Exception e) {
			fail("Exception while adding Trades to SessionHistory");
		}
		
		assertTrue(s1.hashCode() == s2.hashCode());
		assertTrue(s1.hashCode() != s3.hashCode());
		assertTrue(s1.hashCode() != s4.hashCode());
	}
	
	@Test
	public void testEquals(){
		SessionHistory		s1	= new SessionHistory(0);
		SessionHistory		s2	= new SessionHistory(0);
		SessionHistory		s3	= new SessionHistory(0);
		SessionHistory		s4	= new SessionHistory(1);
		
		Trade				m1	= new Trade(1, 1, TradeType.BUY);
		Trade				m2	= new Trade(1, 2, TradeType.BUY);
		Trade				m3	= new Trade(1, 3, TradeType.BUY);
		Trade				m4	= new Trade(1, 4, TradeType.BUY);
		
		try {
			s1.add(m1, 0);
			s1.add(m2, 0);
			s1.add(m3, 1);
			s1.add(m4, 1);
			
			s2.add(m1, 0);
			s2.add(m2, 0);
			s2.add(m3, 1);
			s2.add(m4, 1);
			
			s3.add(m1, 0);
			s3.add(m2, 0);			
			
		} catch (Exception e) {
			fail("Exception while adding Trades to SessionHistory");
		}
		
		assertTrue(s1.equals(s2));
		assertTrue(s1.equals(s3) == false);
		assertTrue(s1.equals(s4) == false);
		
	}

}
