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
	protected boolean green_care = true ; // does the country care about the environment?
	
	protected boolean green_lands = false; // variable to check if country met environment target or not. 
	
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
		clean_development_mechanism(); //awaiting protocol!
		
	}
	
	@Override
	public void YearlyFunction() {
		// TODO implement
		//functions that are implemented every year
				
		
				try {
					economy();
				} catch (IllegalArgumentException e) {
				e.printStackTrace();
				} catch (Exception e) {
				e.printStackTrace();
				} 
				//calculate carbon output every year
				yearly_emissions();
				
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
		energy_aim = energyOutput + 30000 ; //initialise an aim (to be decided)
		environment_friendly_target = 0; //initialise a target (to be decided)
		}
	//.......................................................................................
	//........................................................................................
	
	
	
	/************************Functions executed every year
	 * @throws Exception 
	 * @throws IllegalArgumentException **************************************/
	
	//Every round our countries check current energy output and make decisions
	
	private void economy() throws IllegalArgumentException, Exception
	{
		double difference;
		boolean aim_success = false; 
		
		difference = energy_aim - energyOutput; //difference in energy aim and current energy output.
		double invest_money;
		invest_money = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(difference) ;
				if (energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(difference) < availableToSpend)
				{
					buildIndustry(invest_money); 
					aim_success = true;
					logger.info("Country met its yearly energy output goal");
				}
		else{
			logger.info("Country has insufficient funds to meet its energy output goal");
			generate_income(); //out of money
			}
		update_energy_aim(energy_aim,aim_success); //update the energy aim for the next year.	
		
		}
		
	
			
	
	/*function that uses EnergyUsageHandler to create factories and increase energy output
	 * however carbon output also increases   
	*
	*/
	
	private void buildIndustry(double invest) throws IllegalArgumentException, Exception 
	{
		double carbon_difference; //the difference between environmentally friendly target and actual carbon emission.
	
		if (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest) < environment_friendly_target)
		{ //invest but also check if we meet our environment friendly target.
			try{
				energyUsageHandler.investInCarbonIndustry(invest);
				logger.info("Invest in carbon industry successful");
				logger.info("Country meets its environment friendy target");
				green_lands = true;
			} 
			catch (Exception e) {
				logger.warn("Invest in carbon industry not successful");
			}
		}	
		
		else
		{
			logger.info("Country exceeded its environment friendly goal, invest in carbon industry but also invest in carbon absorption");
			green_lands = false;
			try{
				energyUsageHandler.investInCarbonIndustry(invest);
			} 
			catch (Exception e){
				logger.warn("Invest in carbon industry not successful");
			}
			
			try{ //also since country exceeds its own carbon target, invests in carbon absorption in order to get carbon offset.
				carbon_difference = environment_friendly_target - (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest));
				if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < availableToSpend ) && (currentAvailableArea() == "Safe"))
					{
					carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionHandler.getInvestmentRequired(carbon_difference));
					logger.info("Country invests in carbon absorption to reduce carbon output");
					}
				else if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < availableToSpend ) && (currentAvailableArea() == "Danger"))
					{
					logger.info("Country reach limit of available pre-set land, does not meet its environment friendly target");
					}
				else 
					{
					logger.info("Country has insufficient funds to reach environment friendly target");
					}
					
			}
			catch (Exception e){
				logger.warn("Problem with investing in carbon absorption: " + e);
			}
		}
		
		change_emission_target(environment_friendly_target,green_lands);
		
	}
	
	
	//Function that updates the energy goal each year.
	
	private void update_energy_aim(double previous_aim,boolean success){
		if (success){ // country met goal, change goal
			energy_aim = previous_aim + previous_aim/8; //change aim every year
		}
	}
	
	private void generate_income()
	{
		
	}
	//calculates carbon output every year in order to check environment friendly target.
	private void yearly_emissions()
	{
		carbonOutput = carbonOutput - (carbonAbsorption + carbonOffset);  
	}
	
	//change the emission target every year
	private void change_emission_target(double previous_target,boolean succeed)
	{
		if (succeed) //country met environment target goal, change goal.
			
		environment_friendly_target = previous_target + previous_target/8;
		
	}
	  //.......................................trading.CSM...............................................
		//basically search for potential investors in our lands through clean development mechanism (acquire cash!)
		private void clean_development_mechanism()
		{
			//to be implemented
		}
		
		//Check available area  in order to choose decision accordingly for accepting to sell credits or plant trees for own sake.
		private String currentAvailableArea(){
			
			if (getArableLandArea() > getLandArea()/16)
				return "Safe";
			else 
				return "Danger";
		
		}
		
		
}		
		
	
	
	

		
	

