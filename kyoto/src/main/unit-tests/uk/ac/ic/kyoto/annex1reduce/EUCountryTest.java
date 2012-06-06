package uk.ac.ic.kyoto.annex1reduce;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

public class EUCountryTest {

	@Test
	public void testEUCountry() {
//		fail("Not yet implemented");
		EUCountry test  = new EUCountry(UUID.randomUUID(),"JunitTest", "JUT",
				100, 50, 1000, 10000, 100000, 100, 10, 10, 10);
	}

}
