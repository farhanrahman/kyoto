package uk.ac.ic.kyoto.tradehistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.junit.Test;

import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.ic.kyoto.tradehistory.AnalysisUtils;
import uk.ac.ic.kyoto.tradehistory.AnalysisUtils.TradeActionType;
import uk.ac.ic.kyoto.tradehistory.SessionHistory;
import uk.ac.ic.kyoto.tradehistory.AnalysisUtils.Range;
import uk.ac.ic.kyoto.tradehistory.AnalysisUtils.Weighting;

/**
 * NOTE: This test is not yet considered complete, as some boundary
 * conditions have not been thoroughly tested.
 * @author cs2309
 */
public class AnalysisUtilsTest {	
	
	@Test
	public void testAverage_singleSession(){
		SessionHistory session = new SessionHistory(0);
		long sumOfTrades = 0;
		long numberOfTrades = 0;
		
		try{
			for (int i = 1; i <= 100; i++) {
				session.add(new Offer(1, 2*i, TradeType.BUY), i);
				session.add(new Offer(1, 3*i, TradeType.BUY), i);
				session.add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += 2*i + 3*i + 4*i;
				numberOfTrades += 3;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.average(session, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}
	
	@Test
	public void testAverage_singleSessionRange(){
		SessionHistory session = new SessionHistory(0);
		long sumOfTrades = 0;
		long numberOfTrades = 0;
		
		try{
			for (int i = 1; i <= 100; i++) {
				session.add(new Offer(1, 2*i, TradeType.BUY), i);
				session.add(new Offer(1, 3*i, TradeType.BUY), i);
				session.add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if (i >= 50 && i <= 60) {
					sumOfTrades += 2*i + 3*i + 4*i;
					numberOfTrades += 3;
				}
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.average(session, 60, 50, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}
	
	@Test
	public void testAverage_multipleSessions(){
		SessionHistory[] session = {new SessionHistory(0), new SessionHistory(1)};
		long sumOfTrades = 0;
		long numberOfTrades = 0;
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += 2*i + 3*i + 4*i;
				numberOfTrades += 3;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 101; i <= 200; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += 2*i + 3*i + 4*i;
				numberOfTrades += 3;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.average(session, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}
	
	@Test
	public void testAverage_multipleSessionsRange(){
		SessionHistory[] session = {new SessionHistory(0), new SessionHistory(1)};
		long sumOfTrades = 0;
		long numberOfTrades = 0;
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if (i>=90) {
					sumOfTrades += 2*i + 3*i + 4*i;
					numberOfTrades += 3;
				}
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 101; i <= 200; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if (i<=110) {
					sumOfTrades += 2*i + 3*i + 4*i;
					numberOfTrades += 3;
				}

			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.average(session, 110, 90, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}

	@Test
	public void testRange_singleSession() {
		SessionHistory[] session = {new SessionHistory(0)};
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		Range r1 = AnalysisUtils.range(1, 1, session, TradeActionType.TRADE);
		Range r2 = AnalysisUtils.range(100, 0, session, TradeActionType.TRADE);
		Range r3 = AnalysisUtils.range(50, 40, session, TradeActionType.TRADE);
		Range r4 = AnalysisUtils.range(55, 40, session, TradeActionType.TRADE);
		
		//System.out.println(r1.high + " " + r1.low);
		
		assertTrue(r1.high == 4);
		assertTrue(r1.low == 2);
		
		//System.out.println(r2.high + " " + r2.low);
		
		assertTrue(r2.high == 400);
		assertTrue(r2.low == 2);
		
		//System.out.println(r3.high + " " + r3.low);
		
		assertTrue(r3.high == 200);
		assertTrue(r3.low == 40*2);
		
		//System.out.println(r4.high + " " + r4.low);
		
		assertTrue(r4.high == 220);
		assertTrue(r4.low == 40*2);
	}
	
	@Test
	public void testRange_multipleSessions() {
		SessionHistory[] session = {new SessionHistory(0), new SessionHistory(1), new SessionHistory(3)};
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 101; i <= 200; i++) {
				session[1].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 4*i, TradeType.BUY), i);				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 201; i <= 300; i++) {
				session[2].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[2].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[2].add(new Offer(1, 4*i, TradeType.BUY), i);				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		Range r1 = AnalysisUtils.range(1, 1, session, TradeActionType.TRADE);
		Range r2 = AnalysisUtils.range(100, 0, session, TradeActionType.TRADE);
		Range r3 = AnalysisUtils.range(50, 40, session, TradeActionType.TRADE);
		Range r4 = AnalysisUtils.range(55, 40, session, TradeActionType.TRADE);
		Range r5 = AnalysisUtils.range(155, 140, session, TradeActionType.TRADE);
		Range r6 = AnalysisUtils.range(155, 40, session, TradeActionType.TRADE);
				
		assertTrue(r1.high == 4);
		assertTrue(r1.low == 2);
				
		assertTrue(r2.high == 400);
		assertTrue(r2.low == 2);
				
		assertTrue(r3.high == 200);
		assertTrue(r3.low == 40*2);
				
		assertTrue(r4.high == 220);
		assertTrue(r4.low == 40*2);
		
		assertTrue(r5.high == 155 * 4);
		assertTrue(r5.low == 140*2);
		
		assertTrue(r6.high == 155 * 4);
		assertTrue(r6.low == 40*2);
	}
	
	@Test
	public void TestWeightedAverage_oneSession(){
		SessionHistory[] session = {new SessionHistory(0)};
		int weight1 = 1;
		int weight2 =  4;
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		Weighting[] weightings = {new Weighting(10, 1, weight1), new Weighting(20, 11, weight2)};
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if(i <= 10){
					sumOfTrades += (2*i + 3*i + 4*i) * weight1;
					numberOfTrades += 3 * weight1;
				}
				
				if(i > 10 && i <= 20){
					sumOfTrades += (2*i + 3*i + 4*i) * weight2;
					numberOfTrades += 3 * weight2;
				}
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.weightedAverage(session, weightings, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}
	
	@Test
	public void TestWeightedAverage_multipleSession(){
		SessionHistory[] session = {new SessionHistory(0), new SessionHistory(1)};
		int weight1 = 1;
		int weight2 = 2;
		int weight3 = 5;
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		Weighting[] weightings = {new Weighting(10, 1, weight1), new Weighting(20, 11, weight2), new Weighting(125, 120, weight3)};
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if(i <= 10){
					sumOfTrades += (2*i + 3*i + 4*i) * weight1;
					numberOfTrades += 3 * weight1;
				}
				
				if(i > 10 && i <= 20){
					sumOfTrades += (2*i + 3*i + 4*i) * weight2;
					numberOfTrades += 3 * weight2;
				}
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 101; i <= 200; i++) {
				session[1].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				if(i >= 120 && i <= 125){
					sumOfTrades += (2*i + 3*i + 4*i) * weight3;
					numberOfTrades += 3 * weight3;
				}
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.weightedAverage(session, weightings, TradeActionType.TRADE);
		float result2 = (float) sumOfTrades/numberOfTrades;
		
		assertTrue("Calculated: " + result1 + " - Expected: " + result2, result1 == result2);
	}
	
	@Test
	public void TestWeightedAverage_sessionWeights(){
		SessionHistory[] session = {new SessionHistory(0), new SessionHistory(1), new SessionHistory(2)};
		int weight1 = 1;
		int weight2 = 2;
		int weight3 = 5;
		float sumOfTrades = 0;
		long numberOfTrades = 0;
		
		Weighting[] weightings = {new Weighting(100, 1, weight1), new Weighting(200, 101, weight2), new Weighting(300, 201, weight3)};
		
		try{
			for (int i = 1; i <= 100; i++) {
				session[0].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[0].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += (2*i + 3*i + 4*i) * weight1;
				numberOfTrades += 3 * weight1;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 101; i <= 200; i++) {
				session[1].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[1].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += (2*i + 3*i + 4*i) * weight2;
				numberOfTrades += 3 * weight2;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		try{
			for (int i = 201; i <= 300; i++) {
				session[2].add(new Offer(1, 2*i, TradeType.BUY), i);
				session[2].add(new Offer(1, 3*i, TradeType.BUY), i);
				session[2].add(new Offer(1, 4*i, TradeType.BUY), i);
				
				sumOfTrades += (2*i + 3*i + 4*i) * weight3;
				numberOfTrades += 3 * weight3;
				
			}
		} catch (Exception e) {
			fail("Exception during SessionHistory.add(...)");
		}
		
		float result1 = AnalysisUtils.weightedAverage(session, weightings, TradeActionType.TRADE);
		float result2 = sumOfTrades/numberOfTrades;
		
		assertEquals("Calculated: " + result1 + " - Expected: " + result2, result1, result2, 0.1);
	}
	
	@Test
	public void testStandardDeviation(){
		fail("Test not yet implemented");
	}
	
	@SuppressWarnings("unused")
	@Test(expected = InvalidParameterException.class) 
	public void testRangeClass_tickException(){
		Range r = new Range(TradeActionType.TRADE, 1, 2, 1, 1);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = InvalidParameterException.class) 
	public void testWeightingClass_tickException(){
		Weighting w = new Weighting(1, 2, 1);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = InvalidParameterException.class) 
	public void testWeightingClass_negativeException(){
		Weighting w = new Weighting(2, 1, -1);
	}

}
