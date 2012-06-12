package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import java.util.UUID;

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
	
	protected long economy_threshold; //point where availableToSpend changes behaviour (to be decided)
	protected long environment_friendly_target; //our countries are environmentally friendly and set own emision target :P
	protected long normal_factory; //factory for increasing energyOutput but with normal carbon emisions
	protected long environment_friendly_factory; //like normal_factory but with less carbon emisions, however more expensive
	protected long war_industry; //invest in war industry for extreme measures ie arableArea=0 :p
	protected double GDP_aim; //aim of GDP for a year
	protected long tree_area; // number of trees
	
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
	
	
		
	private void buildIndustry(int factory_type)
	{
		switch(factory_type){
		case 1: //build normal factory 
			{
			normal_factory = normal_factory + 1;
			energyOutput = energyOutput + Country_constants.nf_en_output;
			availableToSpend = availableToSpend - Country_constants.NFactorycost;
			carbonOutput = carbonOutput + Country_constants.carbon_effect_nf;
			}
		
		case 2: //build environment friendly factory
			{
			environment_friendly_factory = environment_friendly_factory + 1;
			energyOutput = energyOutput + Country_constants.ef_en_output;
			availableToSpend = availableToSpend - Country_constants.EFactorycost;
			carbonOutput = carbonOutput + Country_constants.carbon_effect_ef;
			}
		
		case 3: //invest in creating armies!
			{
			war_industry = war_industry + 1;
			availableToSpend = availableToSpend - Country_constants.war_industry_unit_cost;
			}
		}
	}
		//Every round our countries check cash and GDP and invest in factories to grow their economy
	
		private void Economy_check()
		{
			if (availableToSpend > economy_threshold) //still have enough money
			{
				spend_on_industry(); // invest,expand,build
				buildIndustry(3); //armies! however not much is spend in this. more in economic growths
			}
			if (availableToSpend == economy_threshold) //on the limit
				listen_to_offers(); //to be implemented
				
			if (availableToSpend < economy_threshold) //less than critical state
				red_alert();
		}
		
		//invest in industry in order to grow economy by increasing the energyOutput by also considering the environment target(optional)
		private void spend_on_industry()
		{
			if (carbonOutput < environment_friendly_target)
				buildIndustry(1); //build normal factory
			
			if (carbonOutput == environment_friendly_target) 
				buildIndustry(2); //build environment friendly factory
			if (carbonOutput >= environment_friendly_target)
				tree_planting(); // more carbonOutput than required, try reducing it by planting forests (reduces arableLandArea)
		}
		
		//.......................................trading.CSM...............................................
		//basically search for potential investors in our lands through clean development mechanism (acquire cash!)
		private void listen_to_offers(){
			//to be implemented
		}
		
		//.........................................................................................
		
		
		//bad economic state, therefore either invade other nearby countries or listen_to_offers. Praying an option? :p
		private void red_alert(){
			if (war_industry >= Country_constants.enough_units_for_war)
			declare_war(); //to be implemented - if enough war units then declare war to expand and acquire more land
			else
			//no money, no tanks
			listen_to_offers(); //get money
		
		}
		
		private void tree_planting()
		{
			if (currentAvailableArea() == "Safe") //safe to plant
			{
				tree_area = tree_area + 1;
				availableToSpend = availableToSpend - Country_constants.tree_cost;
				carbonOutput = carbonOutput - Country_constants.tree_effect_on_carbon_output;
			}
			
							
		}
		
		private void declare_war()
		{
			
		}
		//to be continued ...
	
	
	
	

		
	
}