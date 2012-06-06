package uk.ac.ic.kyoto.annex1reduce;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import alice.tuprolog.Theory;

/**
 * Abstract static class which will return Prolog theories and behaviour constants for EU countries.
 * Package protected.
 * @author Nik
 *
 */
abstract class EUBehaviours {
	
	final private static Theory DEFAULT_EU_BEHAVIOUR = loadDefaultBehaviour();
	
	final private static String DEFAULT_THEORY_PATH = "src/main/resources/prolog/";

	public static Theory getTheory(final String name) {

		return DEFAULT_EU_BEHAVIOUR;
	}

	private static Theory loadDefaultBehaviour() {
		Theory t = null;
		
		try {
			t = new Theory(new FileInputStream(DEFAULT_THEORY_PATH + "testtheory.pl"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		return t;
	}
	
}
