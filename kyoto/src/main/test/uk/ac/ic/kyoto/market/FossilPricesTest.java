package uk.ac.ic.kyoto.market;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.junit.Test;

/** @author Nicolas Khadivi */

/**
 * This test unit takes the same file as FossilPrices as an input. Then, it
 * retrieves the data that was parsed by FossilPrices via the getters. It
 * reformats this data that was obtained via getters, in CSV form (adding commas
 * etc.).
 * Finally, it compares the contents of the file, to those of the
 * reformatted String line-by-line. Thus, it tests the file parsing, the storing
 * of data into variables, and the getters.
 * */

public class FossilPricesTest {

	FossilPrices testObject = new FossilPrices(null);

	private static final String FOSSIL_FUEL_PRICES_PATH = "src/main/resources/FossilFuelPrices.csv";
	private static final long START_YEAR = 1990;
	private static final long END_YEAR = 2010;

	NumberFormat twoDecimals = new DecimalFormat("#0.00");

	@Test
	public void testFossilPrices() throws Exception {
		File file = new File(FOSSIL_FUEL_PRICES_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = reader.readLine(); // drop title line

		for (long year = START_YEAR; year <= END_YEAR; year++) {
			line = reader.readLine();
			assertTrue(line.equals(year + ","
					+ twoDecimals.format(testObject.getOilPrice(year)) + ","
					+ twoDecimals.format(testObject.getGasPrice(year))));
		}
		reader.close();
	}

}
