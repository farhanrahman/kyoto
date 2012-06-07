package uk.ac.ic.kyoto.countries;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.imperial.presage2.core.util.random.Random;

public class AbstractCountryTest {
	
	// private TestCountry tester;
	// The line above is useless
	
	@Test
	public void getCashTest()
	{
		UUID randomID = Random.randomUUID();	// store a random UUID
		
		TestCountry tester = new TestCountry(randomID, "Poland", "ISO", 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000);

		assertTrue(tester.getCash() == 1000*GameConst.PERCENTAGE_OF_GDP);
		//assertTrue(tester.calculateCostOfInvestingInCarbonIndustry(10) == 10 * GameConst.CARBON_INVESTMENT_PRICE );
		//The line above is deprecated - this method is now in EnergyUsageHandler.java
	}
	
	//@Test
	//public void carbonRe/ductionHandlerTest()
	//{
		//assertTrue(tester.carbonReductionHandler.getCarbonOutputChange(1000) == 1000*1000/GameConst.CARBON_REDUCTION_COEFF);
//}
		
}
