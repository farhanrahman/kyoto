package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * @author Nik
 *
 */
public class AnnexOneReduce extends AbstractCountry {
	
	private EU eu;
	
	public AnnexOneReduce(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP, double GDPRate, double energyOutput, 
			double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
	}

	@Override
	public void initialiseCountry(){
		/* Add the country to the EU service */
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
		//TODO
	}

	@Override
	public void yearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionFunction() {
		// TODO Auto-generated method stub
		
	}

}
