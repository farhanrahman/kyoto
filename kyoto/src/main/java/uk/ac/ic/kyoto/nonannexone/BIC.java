package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import java.util.UUID;

/** author George
 * 
 **/

public class BIC extends AbstractCountry {
	
	//Variables........................................................................
	
	protected double environment_friendly_target; //country environmentally friendly target
	protected double energy_aim ; // the energy output aim of a country each year.
	protected boolean green_care = true ; // does the country care about the environment?
	protected boolean green_lands = false; // variable to check if country met environment target or not. 
	int times_aim_met = 0; //the consecutive times the energy aim is met.
	//............................................................................................ 
	
	public BIC(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);

	}
	
	//Inherited functions......................................................................
	//.........................................................................................
/*****************************************************************************************/
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
/*****************************************************************************************/
	@EventListener
	public void TickFunction(EndOfTimeCycle e){
		//TODO implement functions that are done every tick
				
	}
/*****************************************************************************************/
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
										
	}
/*****************************************************************************************/
	
	@Override
	public void SessionFunction() {
		// TODO implement 
		// carbonAbsorption to carbonOffset
	}

/*****************************************************************************************/
	
	protected void behaviour() {
		// TODO Auto-generated method stub
		
	}

/************************************************************************************************/
	
	protected void initialiseCountry() {
		// TODO Auto-generated method stub
		energy_aim = getEnergyOutput() + CountryConstants.INITIAL_ENERGY_THRESHOLD ; //initialise an aim (to be decided)
		environment_friendly_target = 0; //initialise a target (to be decided)
		}
	//.......................................................................................
	//........................................................................................
	
	
	
/************************Functions executed every year *******************************************/
	
	//Every round our countries check current energy output and make decisions
	
	private void economy() throws IllegalArgumentException, Exception
	{
		double energy_difference;
		double financial_difference;
		boolean aim_success = false;
		double invest_money;
		
		energy_difference = energy_aim - getEnergyOutput(); //difference in energy aim and current energy output.
		invest_money = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(energy_difference) ;
		
		if (invest_money <= getAvailableToSpend())
				{
					buildIndustry(invest_money); //!!!!!!!
					aim_success = true; // energy target met
					times_aim_met +=1; //how many consecutive times the target was met.
					logger.info("Country met its yearly energy output goal");
				}
		else{
			times_aim_met = 0; //reset the counter.
			logger.info("Country has insufficient funds to meet its energy output goal");
			}
		update_energy_aim(energy_aim , aim_success,times_aim_met); //update the energy aim for the next year.	
		
		//clean development mechanism only if country cares for environment
		if (green_care)
		{
		financial_difference = invest_money - getAvailableToSpend();
		clean_development_mechanism(financial_difference);
		}
		
	}
		
	
		/*function that uses EnergyUsageHandler to create factories and increase energy output
	 * however carbon output also increases   
	*
	*/
/************************************************************************************************/
	
	private void buildIndustry(double invest) throws IllegalArgumentException, Exception 
	{
		 //the difference between environmentally friendly target and actual carbon emission.
		if (green_care == true)
			energy_increase_with_care(invest);
		else
			energy_increase_without_care(invest);
		
	}
	
/*******************************************************************************************************/	
	
	private void energy_increase_with_care(double money_invest) throws IllegalArgumentException, Exception
	{
		double carbon_difference; 
		
		if (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest) < environment_friendly_target)
		{ //invest but also check if we meet our environment friendly target.
			try{
				energyUsageHandler.investInCarbonIndustry(money_invest);
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
				energyUsageHandler.investInCarbonIndustry(money_invest);
			} 
			catch (Exception e){
				logger.warn("Invest in carbon industry not successful");
			}
			
			try{ //also since country exceeds its own carbon target, invests in carbon absorption in order to get carbon offset.
				carbon_difference = environment_friendly_target - (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest));
				if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend() ) && (currentAvailableArea() == "Safe"))
					{
					carbonAbsorptionHandler.investInCarbonAbsorption(carbonAbsorptionHandler.getInvestmentRequired(carbon_difference));
					logger.info("Country invests in carbon absorption to reduce carbon output");
					}
				else if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend() ) && (currentAvailableArea() == "Danger"))
					{
					logger.info("Country reach limit of available pre-set land, does not meet its environment friendly target");
					green_care = false;
					}
				else 
					{
					logger.info("Country has insufficient funds to reach environment friendly target");
					green_care = false;
					}
					
			}
			catch (Exception e){
				logger.warn("Problem with investing in carbon absorption: " + e);
			}
		}
		
		change_emission_target(environment_friendly_target,green_lands);
		
		
	}
	
/*****************************************************************************************************/	
	
	private void energy_increase_without_care(double money)
	{
		try{
			energyUsageHandler.investInCarbonIndustry(money);
			logger.info("Invest in carbon industry successful");
		} 
		catch (Exception e) {
			logger.warn("Invest in carbon industry not successful");
		}
	}
	
		
	
	//Function that updates the energy goal each year.
/*****************************************************************************************************/
		
		private void update_energy_aim(double previous_aim,boolean success,int counter)
		{
			if (success)
			{ // country met goal, change goal
				
					switch (counter)
					{
					case 0:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.NORMAL_ENERGY_AIM_GROWTH; //change aim every year	
						logger.info("Country in NORMAL state, normal energy growth");
						break;
					}
					case 1:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.GROW_ENERGY_AIM_GROWTH; //change aim every year	
						logger.info("Country in GROW state, grow energy growth");	
						break;
					}
					case 2:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.BIG_ENERGY_AIM_GROWTH; //change aim every year	
						logger.info("Country in BIG state, big energy growth");	
						break;
					}
					case 3:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.HUGE_ENERGY_AIM_GROWTH; //change aim every year	
						logger.info("Country in HUGE state, huge energy growth");	
						break;
					}
					default:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.FREE_ENERGY_AIM_GROWTH; //change aim every year	
						logger.info("Country in FREE state, energy growth");	
						break;
					}	
					
					}
			}
				
			
		}
/**
 * @throws Exception *****************************************************************************************************/
	//uses the Clean Development Mechanism in order to sell carbon credits
		
	private void clean_development_mechanism(double money_to_invest) throws Exception
	{

		CDM_absorption(money_to_invest);
		CDM_reduction(money_to_invest);
		
	}
/*******************************************************************************************************/
	
	//calculates carbon output every year in order to check environment friendly target.
	private void yearly_emissions()
	{
		//this.carbonOutput = getCarbonOutput() - (get.carbonAbsorption() + get.carbonOffset());  
	}
	
/*******************************************************************************************************/
	//change the emission target every year
	private void change_emission_target(double previous_target,boolean succeed)
	{
		if (succeed) //country met environment target goal, change goal.
			
		environment_friendly_target = previous_target + previous_target * CountryConstants.TARGET_AIM_GROWTH;
		
	}
	
		
/**
 * @throws Exception *****************************************************************************************************/

private void CDM_absorption(double acquire_cash) throws Exception
{

double change_required; // change in carbon absorption in order to acquire the amount of money specified.

change_required = carbonAbsorptionHandler.getCarbonAbsorptionChange(acquire_cash);


broadcastInvesteeOffer(change_required,InvestmentType.ABSORB);



}
		
		
/**
 * @throws Exception *****************************************************************************************************/
private void CDM_reduction(double acquire_cash) throws Exception
{
	double change_required; // change in carbon absorption in order to acquire the amount of money specified.

	change_required = carbonReductionHandler.getCarbonOutputChange(acquire_cash, getCarbonOutput(), getEnergyOutput());


broadcastInvesteeOffer(change_required,InvestmentType.REDUCE);	
}		
		
/*******************************************************************************************************/

//Check available area  in order to choose decision accordingly for accepting to sell credits or plant trees for own sake.
		private String currentAvailableArea(){
			
			if (getArableLandArea() > getLandArea()/(CountryConstants.AREA_LIMIT))
				return "Safe";
			else 
				return "Danger";
		
		}
		
		
}		
		
	
	
	

		
	

