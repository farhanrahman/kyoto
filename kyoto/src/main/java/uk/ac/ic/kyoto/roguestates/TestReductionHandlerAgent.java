package uk.ac.ic.kyoto.roguestates;

import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.messaging.Input;

public class TestReductionHandlerAgent extends AbstractCountry {

	public TestReductionHandlerAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
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
	public void yearlyFunction() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void sessionFunction() {

	}
	
	@Override
	public void initialiseCountry() {

	}
	
	
	/**
	 * Decrease carbon output as long as you have land area and money
	 */
	private void investMax() {
		double carbonReductionChange = 500;
		
		try {
			double investmentRequired = carbonReductionHandler.getInvestmentRequired(carbonReductionChange);
			double reverseCarbonReduction = carbonReductionHandler.getCarbonOutputChange(investmentRequired);
			
			System.out.println("****************************************");
			
			System.out.println("* " + this.getName() + ": Carbon Output before: " + carbonOutput);
			System.out.println("* " + this.getName() + ": Money before: " + availableToSpend);
			
			System.out.println("* " + this.getName() + ": I want to invest in " + carbonReductionChange + " carbon reduction");
			System.out.println("* " + this.getName() + ": It will cost me: " + investmentRequired);
			
			System.out.println("* " + this.getName() + ": For that investment, I should get at least " + reverseCarbonReduction + " carbon reduction");

			carbonReductionHandler.investInCarbonReduction(carbonReductionChange);
			
			System.out.println("* " + this.getName() + ": Success!");
			
			System.out.println("* " + this.getName() + ": Carbon Output after: " + carbonOutput);
			System.out.println("* " + this.getName() + ": Money after: " + availableToSpend);
			
			System.out.println("****************************************");
		}
		catch (NotEnoughCashException e) {
			System.out.println("! " + this.getName() + ": I have run out of money");
		}
		catch (NotEnoughCarbonOutputException e) {
			System.out.println("! " + this.getName() + ": I have run out of carbon output");
		}
		catch (Exception e) {
			System.out.println("! " + this.getName() + ": 8======D " + e.getMessage());
		}
	}
	
	@Override
	public void behaviour() {
		investMax();
	}

}