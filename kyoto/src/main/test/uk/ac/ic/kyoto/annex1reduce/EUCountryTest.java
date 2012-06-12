package uk.ac.ic.kyoto.annex1reduce;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.UUID;

/**
 * Tests Prolog integration
 * @author Nik
 *
 */
public class EUCountryTest {

	static private EUCountry test;
	
	@BeforeClass
	public static void initEUCountry() {
		System.out.println("Initialising EUCountry");
		test  = new EUCountry(UUID.randomUUID(),"JunitTest", "JUT",
				100, 50, 1000, 10000, 100000, 100, 10, 10, 10);
	}
	
	@Test
	public void testbehaviour() {
		test.behaviour();
	}

}
