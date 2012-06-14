package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.UnknownVarException;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * @author Nik
 *
 */
public class AnnexOneReduce extends AbstractCountry {
	
	final private Prolog engine;
	private EU eu;
	
	public AnnexOneReduce(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput) {

		
		super(id, name, ISO, landArea, arableLandArea, GDP,
					GDPRate, energyOutput, carbonOutput);

		engine = EUBehaviours.getEngine(name);
	}

	@Override
	public void initialiseCountry(){
		
		// Add the country to the EU service
		try {
			this.eu = this.getEnvironmentService(EU.class);
			this.eu.addMemberState(this);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach EU service.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Take an input and process the data.
	 */
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void behaviour() {
		
		//TODO perform analysis
		
		//TODO call prolog functions
		String term = evaluateString("basicTest(test,X).","X");
		System.out.println(term);
	}
	
	/**
	 * Pass in a string for the loaded Prolog engine to evaulate and the term
	 * we want returned. Will return a maximum of one term containing the action
	 * that is to be taken by the country
	 * 
	 * @param input The string to evaluate
	 * @param term The term to return
	 * @return A string containing the returned term
	 */
	private String evaluateString(String input, String term){
		
		SolveInfo info = null;
		try {
			info = engine.solve(input);
		} catch (MalformedGoalException e) {
			e.printStackTrace();
		}
		
		String output;
		
		if (info.isSuccess()) {
			try {
				output = info.getTerm(term).toString();
			} catch (NoSolutionException e) {
				e.printStackTrace();
				output = null;
			} catch (UnknownVarException e) {
				e.printStackTrace();
				output = null;
			}
		}
		else {
			output = "false";
		}
		
		return output;
		
	}

	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
	}

}
