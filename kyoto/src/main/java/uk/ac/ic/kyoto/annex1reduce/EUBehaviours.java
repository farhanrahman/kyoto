package uk.ac.ic.kyoto.annex1reduce;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Prolog;
import alice.tuprolog.Theory;

/**
 * Uninstantiable final static class which will return Prolog theories and behaviour constants for EU countries.
 * Package protected.
 * @author Nik
 *
 */
final class EUBehaviours {
	
	private EUBehaviours() {}
	
	final private static String DEFAULT_THEORY_PATH = "src/main/resources/prolog/EUBehaviour/";
	
	final private static Theory DEFAULT_EU_BEHAVIOUR = loadDefaultBehaviour();
	

	public static Prolog getEngine(final String name) {
		
		
		Prolog engine = new Prolog();

		//TODO make this return something other than the default theory

		try {
			engine.setTheory(DEFAULT_EU_BEHAVIOUR);
		}catch (InvalidTheoryException e) {
			System.out.println("The prolog theory for " + name + " is invalid. Please correct it and try again.");
		}
		return engine;
		
	}

	private static Theory loadDefaultBehaviour() {
		Theory t = null;
		
		try {
			t = new Theory(new FileInputStream(DEFAULT_THEORY_PATH + "testtheory.pl"));
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return t;
	}
	
}
