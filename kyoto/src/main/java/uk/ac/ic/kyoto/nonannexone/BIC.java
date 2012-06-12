package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.countries.NotEnoughCashException;
import uk.ac.ic.kyoto.countries.NotEnoughLandException;

/** author George
 * 
 *  
 *  Updated file soon to be added that uses CarbonAbsorptionHandler,CarbonReductionHandler 
 *  and EnergyUsageHandler that replaces the below strategy
 *  
 *  
 *  **/

public class BIC extends AbstractCountry {
	
	//Variables........................................................................
	
	protected double economy_threshold; //point where availableToSpend changes behaviour (to be decided)
	protected double environment_friendly_target; //our countries are environmentally friendly and set own emision target :P
	protected double energy_aim; // the energy output aim of a country for each year.
	
	
	//............................................................................................ 
	
	public BIC(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput)
	{
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
		//the functions that are implemented every year
				//1)GDP growth
		economy();
				//2)Grow GDP
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
		
	}
	//.......................................................................................
	//........................................................................................
	
	
	
	//General functions
	
	//Check available area  in order to choose decision accordingly for accepting to sell credits or plant trees for own sake.
	private String currentAvailableArea(){
		
		if (this.getArableLandArea() > this.getLandArea()/16)
			return "Safe";
		else if ((this.getArableLandArea() == this.getLandArea()/16))
			return "Limit";
		else if (this.getArableLandArea() < this.getLandArea()/16)
			return "Danger";
		return "";		
	}
	
	
	/*function that uses EnergyUsageHandler to create factories and increase energy output
	 * however carbon output also increases   
	*
	*/
	
	private void buildIndustry(long invest) 
	{
	double carbon_difference; //the difference between environmentally friendly target and actual carbon emission.
	
	if (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest) < environment_friendly_target) //invest but also check if we meet our environment friendly target.
		{
		try{
	energyUsageHandler.investInCarbonIndustry(invest);
			} 
		catch (Exception e) {
			logger.warn("Invest in carbon industry not successful");
		}
		}	
	if (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest) > environment_friendly_target)
	{
		try{
			energyUsageHandler.investInCarbonIndustry(invest);
			} 
				catch (Exception e)
				{
					logger.warn("Invest in carbon industry not successful");
				}
		try{ //also since country exceeds its own carbon target, invests in carbon absorption in order to get carbon offset.
			carbon_difference = environment_friendly_target - (carbonOutput + energyUsageHandler.calculateCarbonIndustryGrowth(invest));
			if (carbonAbsorptionHandler.getCost(carbon_difference) < availableToSpend )
				carbonAbsorptionHandler.invest(carbonAbsorptionHandler.getCost(carbon_difference));
			}
		catch (Exception e)
			{
			logger.warn("Problem with investing in carbon absorption: " + e);
		
			}
	}
	
		
	
	}
		//Every round our countries check current energy output and make decisions
	
		private void economy()
		{
		long difference;
		difference = energy_aim - energyOutput; //difference in energy aim and current energy output.
		
		if (energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(difference) < availableToSpend)	
			buildIndustry(difference); 
			
		else 
			clean_development_mechanism(); //to be implemented
				
		}
		
	  //.......................................trading.CSM...............................................
		//basically search for potential investors in our lands through clean development mechanism (acquire cash!)
		private void clean_development_mechanism(){
			//to be implemented
		}
		
}		
		
	
	
	

		
	

