package uk.ac.ic.kyoto.roguestates;

import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.NotEnoughCashException;
import uk.ac.ic.kyoto.countries.NotEnoughLandException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.messaging.Input;

public class TestAbsorptionHandlerAgent extends AbstractCountry {

	public TestAbsorptionHandlerAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		return super.getSharedState();
	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void SessionFunction() {

	}
	
	@Override
	public void initialiseCountry() {

	}
	
	
	/**
	 * Plant trees until you have money and land
	 */
	@Override
	public void behaviour() {
		long investmentAmount;
		
		try {
			investmentAmount = (long) (availableToSpend * 0.1);
			//carbonAbsorptionHandler.invest(investmentAmount);
		}
		catch (NotEnoughCashException e) {
			System.out.println("8======D : " + e);
			investmentAmount = 0;
		}
		catch (NotEnoughLandException e) {
			System.out.println("8======D : " + e);
			investmentAmount = 0;
		}
		catch (Exception e) {
			System.out.println("8======D : " + e);
			investmentAmount = 0;
		}
		
		System.out.println("* Investing " + investmentAmount + " in carbon absorption");
		System.out.println("* Money: " + availableToSpend);
		System.out.println("* arableLandArea: " + arableLandArea);
		System.out.println("* carbonAbsorption: " + carbonAbsorption);
	}

}