package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

import java.util.UUID;

public class BIC extends AbstractCountry {
	
	//Variables........................................................................
	
	protected char currentAvailableArea; // area available for planting trees/industry
	protected long economy_threshold; //point where availableToSpend changes behaviour (to be decided)
	protected long environment_friendly_target; //our countries are environmentally friendly and set own emision target :P
	protected long normal_factory; //factory for increasing energyOutput but with normal carbon emisions
	protected long environment_friendly_factory; //like normal_factory but with less carbon emisions, however more expensive
	protected long war_industry; //invest in war industry for extreme measures ie arableArea=0 :p
	protected double GDP_aim; //aim of GDP for a year
	
	
	//Constants.............................................................................
	
	public static final long  nf_en_output = 0; //effect on energy output of normal factory(to be decided)
	public static final long  ef_en_output=0; //environment friendly factory's effect(to be decided)
	public static final long  NFactorycost=0; //cost of normal factory
	public static final long EFactorycost=0; //cost of environmental factory (expensive)
	public static final long carbon_effect_nf = 0; //effect on carbon output
	public static final long carbon_effect_ef = 0; //effect on carbon output - less than normal factory
	public static final long war_industry_unit_cost = 0; // cost of creating a unit of the greatest empire ever in the known world
	
	
	
	//............................................................................................ 
	
	public BIC(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long emissionsTarget, long energyOutput, long carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, emissionsTarget,
				energyOutput, carbonOutput);

}
	
	//Inherited functions......................................................................
	//.........................................................................................
	
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
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
	
	//Check if the available area is safe=S in order to choose decision accordingly for accepting to sell credits.
	private void currentAvailableArea(){
		if (this.getArableLandArea() > this.getLandArea()/4)
			{
			currentAvailableArea = 'S';
			}
		else if ((this.getArableLandArea() == this.getLandArea()/4))
			currentAvailableArea = 'C';
		else if (((this.getArableLandArea() < this.getLandArea()/4)))
			currentAvailableArea = 'N';
	}
	
	
		
	private void buildIndustry(int factory_type)
	{
		switch(factory_type){
		case 1: //build normal factory 
			{
			normal_factory = normal_factory + 1;
			this.energyOutput = this.energyOutput + nf_en_output;
			this.availableToSpend = this.availableToSpend - NFactorycost;
			this.carbonOutput = this.carbonOutput + carbon_effect_nf;
			}
		
		case 2: //build environment friendly factory
			{
			environment_friendly_factory = environment_friendly_factory + 1;
			this.energyOutput = this.energyOutput + ef_en_output;
			this.availableToSpend = this.availableToSpend - EFactorycost;
			this.carbonOutput = this.carbonOutput + carbon_effect_ef;
			}
		
		case 3: //invest in creating armies!
			{
			war_industry = war_industry + 1;
			this.availableToSpend = this.availableToSpend - war_industry_unit_cost;
			}
		}
	}
		//Every round our countries check cash and GDP and invest in factories to grow their economy
	
		private void Economy_check()
		{}
		
		
		//to be continued ...
	
	
	
	

		
	
}