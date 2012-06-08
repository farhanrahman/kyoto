package uk.ac.ic.kyoto.annex1reduce.analysis;

import static org.junit.Assert.*;

import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.Range;
import uk.ac.ic.kyoto.trade.TradeProtocol.Trade;
import uk.ac.ic.kyoto.trade.TradeType;

public class AnalysisUtilsTest {

	@Test
	public void testSessionAverage() {
		SessionHistory session = new SessionHistory(0);
		long sumOfTrades = 0;
		long numberOfTrades = 0;
		
		try{
			for (int i = 1; i <= 100; i++) {
				session.add(new Trade(1, 2*i, TradeType.BUY), i);
				session.add(new Trade(1, 3*i, TradeType.BUY), i);
				session.add(new Trade(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += 2*i + 3*i + 4*i;
				numberOfTrades += 3;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.sessionAverage(session, uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.TradeType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue(result1 ==result2);
	}

	@Test
	public void testNPointRange() {
		SessionHistory[] session = {new SessionHistory(0)};
		
		try{
			for (int i = 0; i < 100; i++) {
				session[0].add(new Trade(1, 2*i, TradeType.BUY), i);
				session[0].add(new Trade(1, 3*i, TradeType.BUY), i);
				session[0].add(new Trade(1, 4*i, TradeType.BUY), i);				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		Range r1 = AnalysisUtils.nPointRange(1, 1, session, uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.TradeType.TRADE);
		Range r2 = AnalysisUtils.nPointRange(100, 100, session, uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.TradeType.TRADE);
		Range r3 = AnalysisUtils.nPointRange(10, 50, session, uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.TradeType.TRADE);
		Range r4 = AnalysisUtils.nPointRange(15, 55, session, uk.ac.ic.kyoto.annex1reduce.analysis.AnalysisUtils.TradeType.TRADE);
		
		assertTrue(r1.high == 4);
		assertTrue(r1.low == 2);
		
		assertTrue(r2.high == 400);
		assertTrue(r2.low == 2);
		
		System.out.println(r3.high + " " + r3.low);
		
		assertTrue(r3.high == 50*4);
		assertTrue(r3.low == 40*2);
		
		System.out.println(r4.high + " " + r4.low);
		
		assertTrue(r4.high == 55*4);
		assertTrue(r4.low == 40*2);
	}

}
