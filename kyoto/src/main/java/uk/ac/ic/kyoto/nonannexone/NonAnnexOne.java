package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.trade.TradeType;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.util.fsm.FSMException;

import java.util.UUID;

/** @author George
 *
 **/

public class NonAnnexOne extends AbstractCountry {
	
	/**************************************Variable Declaration***************************************************/
	
	protected double environment_friendly_target; //country environmentally friendly target
	protected double energy_aim ; // the energy output aim of a country each tick
	protected boolean green_care = true ; // does the country care about the environment?
	protected boolean green_lands = false; // variable to check if country met environment target or not. 
	int times_aim_met = 0; //the consecutive times the energy aim is met.
	boolean aim_success = false; // variable that controls whether the energy aim was met or not
	int ticks_in_a_year; //how many ticks are in a year
	int current_tick; //tick currently operating
	int current_year; //year currently operating
	int imaginary_tick; //current tick modulo imaginary tick
	int ticks_in_a_year_threshold; //the number of ticks that change the behaviour in how the energy aim is updated

	/********************************************Constructor*******************************************************/
	
	public NonAnnexOne(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput){
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		setKyotoMemberLevel(KyotoMember.NONANNEXONE);
	}
	
/*********************************************ProcessInput-Function*******************************************************/
	@Override
	protected void processInput(Input in) {
		if (this.tradeProtocol.canHandle(in)) {
			this.tradeProtocol.handle(in);
		}
		else {

			if(in instanceof Message){
				try{
					@SuppressWarnings("unchecked")
					Message<OfferMessage> m = (Message<OfferMessage>) in;
					OfferMessage o = m.getData();
					if(!this.tradeProtocol
							.getActiveConversationMembers()
								.contains(m.getFrom())){
						try {
							if (o.getOfferType().equals(TradeType.INVEST))
							{
							this.tradeProtocol.offer(
									m.getFrom(), 
									o.getOfferQuantity(), 
									o);
							}
							} catch (FSMException e) {
							logger.warn(e);
						}
					}
					
				}catch(ClassCastException e){
					logger.warn("Class cast exception");
					logger.warn(e);
				}
			}
		}		
	}
/*******************************************************************************************************************/
	@EventListener
	public void TickFunction(EndOfTimeCycle e){
				
	}
/********************************************************************************************************************/
	public void yearlyFunction() {							
	}
/********************************************************************************************************************/
	
	@Override
	public void sessionFunction() {
	}

/*********************************************************************************************************************/
	//Function executed every tick that defines the behaviour of the country
	
	protected void behaviour() {
			
		try {
			economy();
		} catch (IllegalArgumentException e) {
			logger.warn(e);
		} catch (Exception e) {
			logger.warn(e);
		} 
		
		
	}
/********************************************Initialise-variables/behaviour****************************************************/
	
	protected void initialiseCountry() 
	{
		energy_aim = getEnergyOutput() + CountryConstants.INITIAL_ENERGY_THRESHOLD ; //initialise energy aim.
		environment_friendly_target = getCarbonOutput() + CountryConstants.INITIAL_CARBON_TARGET; //initialise a target 
			
	}
		
/***********************************************Accept CDM offers********************************************************************/
	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		
		if (trade.getInvestmentType() == InvestmentType.ABSORB)
		{
			if (currentAvailableArea()=="Safe")
				return true;
			else
				return false;
		}
		else
			return true;
	}
	
/************************Functions executed every tick ***************************************************************/
	
	//Every round country checks current energy output and decides whether to invest in carbon industry or not.
	
	private void economy() throws IllegalArgumentException, Exception
	{
		double energy_difference;
		double invest_money;
		double money_available;
		
		energy_difference = energy_aim - getEnergyOutput(); //difference in energy aim and current energy output.
		invest_money = energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(energy_difference) ; //find the cost of investment in carbon industry
		money_available= getAvailableToSpend();
		
		if (invest_money <= money_available) //check whether country has enough money available to invest
				{
					buildIndustry(invest_money); //calls the function to invest in industry
					aim_success = true; // energy target met
					times_aim_met +=1; //how many consecutive times the target was met.
					logger.info("Country met its energy output goal");
				}
		else 
		{
			times_aim_met = 0; //reset the counter.
			aim_success = false; //energy target not met
			logger.info("Country has insufficient funds to meet its energy output goal");	
		}
		update_energy_aim(energy_aim , aim_success,times_aim_met); //update the energy aim for the next year.	
		
		//clean development mechanism only if country cares for environment
		
		if (green_care)
		{
			clean_development_mechanism(invest_money);
		}
		
		
	}
			
/****************************************Invest in carbon industry with care or not for the environment ********************************************************/
	//function that uses EnergyUsageHandler to create factories 
	//and increase energy output however carbon output also increases 
	
	private void buildIndustry(double invest) throws IllegalArgumentException, Exception 
	{
		if (green_care == true)
			energy_increase_with_care(invest);
		else
			energy_increase_without_care(invest);
		
	}
	
/******************************************Invest in carbon industry and care for environment*************************************************************/	
	
	private void energy_increase_with_care(double money_invest) throws IllegalArgumentException, Exception
	{
		double carbon_difference; 
		double available_area;
		available_area = getArableLandArea();
		
		
		if (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest) <= environment_friendly_target)
		{ //invest but also check if the environment friendly target is met.
			try{
				energyUsageHandler.investInCarbonIndustry(money_invest);
				logger.info("Invest in carbon industry successful");
				logger.info("Country meets its environment friendy target");
				green_lands = true; //set the variable to true, to be used when updating the environment friendly target
			} 
			catch (Exception e) {
				logger.warn("Invest in carbon industry not successful");
			}
		}	
		
		else
		{ 
			//country does not meet its own target, therefore it has to invest in carbon absorption or carbon reduction
			
			logger.info("Country exceeded its environment friendly goal, invest in carbon industry but also invest in carbon absorption");
			green_lands = false;
			try{
				energyUsageHandler.investInCarbonIndustry(money_invest);
			} 
			catch (Exception e){
				logger.warn("Invest in carbon industry not successful");
			}
			
			try
			{ 
				carbon_difference = (getCarbonOutput() + energyUsageHandler.calculateCarbonIndustryGrowth(money_invest)) - environment_friendly_target;
				
				if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend()) && ((carbonAbsorptionHandler.getForestAreaRequired(carbon_difference) < available_area)))
					{
					carbonAbsorptionHandler.investInCarbonAbsorption(carbon_difference);
					logger.info("Country invests in carbon absorption to increase carbon absorption and thus reach environment target carbon output");
					}
				
				else if ((carbonAbsorptionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend() ) && (carbonAbsorptionHandler.getForestAreaRequired(carbon_difference) >= available_area))
				
					{ 
					// when country does not have enough arable land area to invest in carbon absorption,
					//invests in carbon reduction
				
					logger.info("Country reach limit of available pre-set land, not possible to invest in carbon absorption, try invest in carbon reduction");
					if (carbonReductionHandler.getInvestmentRequired(carbon_difference) < getAvailableToSpend())
						{
						carbonReductionHandler.investInCarbonReduction(carbon_difference);
						logger.info("Country has enough cash to invest in carbon reduction, invests");
						}
					}
				else 
					//when the country does not have enough available to spend
					//then it does not care anymore about the environment.
					{
					logger.info("Country has insufficient funds to reach environment friendly target");
					green_care = false;
					
					}
					
			}
			catch (Exception e){
				logger.warn("Problem with investing in carbon absorption: " + e);
			}
		}
		//every tick, it updates the "environment" carbon output target
		
		change_emission_target(environment_friendly_target,green_lands);
		
		
	}
	
/************************************Invest in carbon industry but does not care about environment*****************************************************************/	
	//function that invests in carbon industry without caring about meeting any environmental target
	
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
	
		
	
	//Function that updates the energy goal every tick
/*******************************************Update the energy aim every tick **********************************************************/
		
		private void update_energy_aim(double previous_aim,boolean success,int counter)
		{
			ticks_in_a_year_threshold =  GameConst.getTicksInYear() - 10;
			current_tick = timeService.getCurrentTick();
			imaginary_tick = current_tick % GameConst.getTicksInYear() ;
			
			if (success)
			{ // country met goal, change goal
				if ((imaginary_tick < ticks_in_a_year_threshold)) //steady increase every tick
				{
					energy_aim = previous_aim + CountryConstants.STEADY_TICK_ENERGY_INCREASE;
					
				}
				if (imaginary_tick == ticks_in_a_year_threshold)
				{
				
				times_aim_met = 0; //reset counter, wait a tick to operate and boost investment.
				energy_aim= previous_aim + 1;
				}
				if (imaginary_tick > ticks_in_a_year_threshold )
				{
					
					switch (counter)
					{
					case 0:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.NORMAL_ENERGY_AIM_GROWTH;	
						logger.info("Country in NORMAL state, normal energy growth");
						break;
					}
					case 1:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.GROW_ENERGY_AIM_GROWTH;	
						logger.info("Country in GROW state, grow energy growth");	
						break;
					}
					case 2:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.BIG_ENERGY_AIM_GROWTH; 	
						logger.info("Country in BIG state, big energy growth");	
						break;
					}
					case 3:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.HUGE_ENERGY_AIM_GROWTH; 
						logger.info("Country in HUGE state, huge energy growth");	
						break;
					}
					default:
					{
						energy_aim = previous_aim + previous_aim * CountryConstants.FREE_ENERGY_AIM_GROWTH;	
						logger.info("Country in FREE state, energy growth");	
						break;
					}	
					
					}
				}
			}
				
			
		}
/**
 * @throws Exception *****************************************************************************************************/
	//Clean Development Mechanism 
	private void clean_development_mechanism(double money_to_invest) throws Exception
	{
		CDM_absorption(money_to_invest);
		CDM_reduction(money_to_invest);
		
	}

/*******************************************************************************************************/
	//change the target for carbon output(environmentally friendly) every tick.
	private void change_emission_target(double previous_target,boolean succeed)
	{
		if (succeed) //country met environment target goal, decrease goal.
			
		environment_friendly_target = previous_target - CountryConstants.DECREASING_CARBON_TARGET;
		
		if (succeed == false) //country did not meet environment target goal, increase target
		
		environment_friendly_target = previous_target + CountryConstants.DECREASING_CARBON_TARGET;
		
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
		
/****************************************************************************************************************/

//Check available area  in order to choose decision accordingly for accepting to sell credits or plant trees for own sake.
		private String currentAvailableArea(){
			
			if (getArableLandArea() > getLandArea()*(CountryConstants.AREA_LIMIT))
				return "Safe";
			else 
				return "Danger";
		
		}

/***************************************************************************************************************/

		
}		