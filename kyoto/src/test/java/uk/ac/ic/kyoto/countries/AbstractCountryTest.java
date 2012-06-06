package uk.ac.ic.kyoto.countries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.TestCountry;
import uk.ac.imperial.presage2.core.util.random.Random;

public class AbstractCountryTest {
	
	public void test()
	{
		UUID randomID = Random.randomUUID();	// store a random UUID
		
		TestCountry tester = new TestCountry(randomID, "Poland", "ISO", 10000, 1000, 1000000, 10, 0, 0, 0, 0, 0);
	}
}
