package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import java.util.UUID;

/** author George
 * 
 **/

public class BIC extends AbstractCountry {
	
	//Variables........................................................................
	
	protected double environment_friendly_target; //country environmentally friendly
	protected double energy_aim ; // the energy output aim of a country each year.
	
	
	//............................................................................................ 
	
	public BIC(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);

	}
	
	//Inherited functions......................................................................
	//.........................................................................................
	
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
	
	@EventListener
	public void TickFunction(EndOfTimeCycle e){
		//TODO implement functions that are done every tick
		//trades are done every tick ( CSM offers) 
		
	}
	
	@Override
	public void YearlyFunction() {
		// TODO implement
		//functions that are implemented every year
				try {
					economy();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				//3)Calculate availabletoSpend				
				//4)Recalculate carbonOffset
		
	}

	@Override
	public void SessionFunction() {
		// TODO implement 
		// carbonAbsorption to carbonOffset
	}

	
	protected void behaviour() {
		// TODO Auto-generated method stub
		
	}

	
	protected void initialiseCountry() {
		// TODO Auto-generated method stub
		energy_aim = 0 ; //initialise an aim (to be decided)
		environment_friendly_target = 0; //initialise a target (to be decided)
	}
	//.......................................................................................
	//........................................................................................
	
	
	
	/************************Functions executed every year
	 * @throws Exception 
	 * @throws IllegalArgumentException **************************************/
	
	//Every round our countries check current energy output and make decisions
	
	private void economy() throws IllegalArgumentException, Exception{
		double difference;
		boolean aim_success = false; 
		difference = energy_aim - energyOutput; //difference in energy aim and current energy output.
		
		if (energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(difference) < availableToSpend){
			buildIndustry(difference); 
			aim_success = true;
			logger.info("Country met its yearly energy output goal");
		}
		else{
			clean_development_mechanism(); //to be implemented
			aim_success = false;
			logger.info("Country failed to met its yearly energy output goal");
		}
		update_energy_aim(energy_aim,aim_success); //update the energy aim for the next year.		
			
	}
			
	
	/*function that uses EnergyUsageHandler to create factories and increase energy output
	 * however carbon output also increases   
	*
	*/
	
	private void buildIndustry(double invest) throws IllegalArgumentException, Exception {
		double carbon_difference; //the difference between environmentally friendly target and actual carbon emission.
	
		if (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest) < environment_friendly_target){ //invest but also check if we meet our environment friendly target.
			try{
				energyUsageHandler.investInCarbonIndustry(invest);
			} 
			catch (Exception e) {
				logger.warn("Invest in carbon industry not successful");
			}
		}	
		
		if (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest) >= environment_friendly_target)
		{
			logger.info("Country exceeded its environment friendly goal");
			
			try{
				energyUsageHandler.investInCarbonIndustry(invest);
			} 
			catch (Exception e){
				logger.warn("Invest in carbon industry not successful");
			}
			
			try{ //also since country exceeds its own carbon target, invests in carbon absorption in order to get carbon offset.
				carbon_difference = environment_friendly_target - (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest));
				if (carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < availableToSpend )
					carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionHandler.getInvestmentRequired(carbon_difference));
			}
			catch (Exception e){
				logger.warn("Problem with investing in carbon absorption: " + e);
			}
		}
	}
	
	//Function that updates the energy goal each year.
	
	private void update_energy_aim(double previous_aim,boolean success){
		if (success){ // country met goal, change goal
			energy_aim = previous_aim + previous_aim/16; //double aim every year
		}
	}
	
			
	  //.......................................trading.CSM...............................................
		//basically search for potential investors in our lands through clean development mechanism (acquire cash!)
		private void clean_development_mechanism(){
			//to be implemented
		}
		
		//Check available area  in order to choose decision accordingly for accepting to sell credits or plant trees for own sake.
	/*	private String currentAvailableArea(){
			
			if (this.getArableLandArea() > this.getLandArea()/16)
				return "Safe";
			else if ((this.getArableLandArea() == this.getLandArea()/16))
				return "Limit";
			else if (this.getArableLandArea() < this.getLandArea()/16)
				return "Danger";
			return "";		
		}
		*/
		
}		
		
	
	
	

		
	

