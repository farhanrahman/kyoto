package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Prolog;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * @author Nik
 *
 */
public class EUCountry extends AbstractCountry {
	
	final Prolog engine;
	
	public EUCountry(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, float availiableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput) {
		
		super(id, name, ISO, landArea, arableLandArea, GDP,
					GDPRate, availiableToSpend, emissionsTarget, carbonOffset,
					energyOutput, carbonOutput);
		
		EU.addMemberState(this);
		
		engine = new Prolog();
		
		try {
			engine.setTheory(EUBehaviours.getTheory(name));
		}catch (InvalidTheoryException e) {
			System.out.println("The prolog theory for " + name + " is invalid. Please correct it and try again.");
			
		}
		
	}

	/**
	 * Take an input and process the data.
	 * May or may not be used
	 */
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void execute() {
		super.execute();
		
		//TODO perform analysis
		
		//TODO call prolog functions
		
		
	}

}
