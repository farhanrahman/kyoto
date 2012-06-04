package uk.ac.ic.kyoto.annex1reduce.analysis;

import java.util.Random;

public class AnalyiticsTest {
	
	public static void main(String[] args) throws Exception {
		SessionHistory s1 = new SessionHistory(0);
		SessionHistory s2 = new SessionHistory(1);
		SessionHistory s3 = new SessionHistory(2);
		
		Random r = new Random();
		
		for (int i = 0; i < 120; i++) {
			s1.add(new TradeMessage(1, i), i);
			System.out.print(i + ",");
		}
		
		System.out.println();
		
		for (int i = 0; i < 120; i++) {
			float temp = r.nextFloat()*120;
			s1.add(new TradeMessage(1, temp), i);
			System.out.print(temp + ",");
		}
		
		System.out.println();
		
		System.out.println("DONE");
	}

}
