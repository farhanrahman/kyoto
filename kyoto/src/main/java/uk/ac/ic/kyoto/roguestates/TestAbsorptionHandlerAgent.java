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
	 * Increase carbon absorption as long as you have land area and money
	 */
	private void investMax() {
		double carbonAbsorptionChange = (carbonAbsorption * 2) + 1; // Increases exponentially from 0
		
		try {
			double investmentRequired = carbonAbsorptionHandler.getInvestmentRequired(carbonAbsorptionChange);
			double areaRequired = carbonAbsorptionHandler.getForestAreaRequired(carbonAbsorptionChange);
			double reverseCarbonAbsorption = carbonAbsorptionHandler.getCarbonAbsorptionChange(investmentRequired);
			
			System.out.println("****************************************");
			
			System.out.println("* Carbon Absorption before: " + carbonAbsorption);
			System.out.println("* Money before: " + availableToSpend);
			System.out.println("* Arable land before: " + arableLandArea);
			
			System.out.println("* I want to invest in " + carbonAbsorptionChange + " carbon absorption");
			System.out.println("* It will cost me: " + investmentRequired);
			System.out.println("* It will reduce my land by: " + areaRequired);
			
			System.out.println("* For that investment, I should get at least " + reverseCarbonAbsorption + " carbon absorption");

			carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionChange);
			
			System.out.println("* Success!");
			
			System.out.println("* Carbon Absorption after: " + carbonAbsorption);
			System.out.println("* Money after: " + availableToSpend);
			System.out.println("* Arable land after: " + arableLandArea);
			
			System.out.println("****************************************");
		}
		catch (NotEnoughCashException e) {
			System.out.println("! I have run out of money");
		}
		catch (NotEnoughLandException e) {
			System.out.println("! I have run out of land");
		}
		catch (Exception e) {
			System.out.println("! 8======D " + e.getMessage());
		}
	}
	
	@Override
	public void behaviour() {
		investMax();
	}

}