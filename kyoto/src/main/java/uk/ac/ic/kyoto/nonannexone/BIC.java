package uk.ac.ic.kyoto.nonannexone;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.imperial.presage2.core.messaging.Input;

import java.util.UUID;

public class BIC extends AbstractCountry {
	
	//Variables
	
	protected char currentAvailableArea; // area available for planting trees/industry
	protected long economy_threshold; //point where availableToSpend changes behaviour (to be decided)
	protected long environment_friendly_target; //our countries are environmentally friendly and set own emision target :P
	protected long normal_factory; //factory for increasing energyOutput but with normal carbon emisions
	protected long environment_friendly_factory; //like normal_factory but with less carbon emisions, however more expensive
	protected long war_industry; //invest in war industry for extreme measures ie arableArea=0 :p
	protected double GDP_aim; //aim of GDP for a year
	
	
	//Constants
	
	public static final long  nf_en_output = 0; //effect on energy output of normal factory(to be decided)
	public static final long  ef_en_output=0; //environment friendly factory's effect(to be decided)
	public static final long  NFactorycost=0; //cost of normal factory
	public static final long EFactorycost=0; //cost of environmental factory (expensive)
	
	public BIC(UUID id, String name, String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, long availableToSpend, long emissionsTarget, long carbonOffset,
			long energyOutput, long carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, availableToSpend, emissionsTarget, carbonOffset,
				energyOutput, carbonOutput);

}
	
	//Inherited functions
	
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void execute() {
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
	
	
		
	private void buildIndustry()
	{
		if (this.carbonOutput < environment_friendly_target) 
		{
			normal_factory = normal_factory + 1;
			this.energyOutput = this.energyOutput + nf_en_output;
			this.availableToSpend = this.availableToSpend - NFactorycost;
		}
		
		//to be continued ...
	
	
	
	}

	@Override
	protected void behaviour() {
		// TODO Auto-generated method stub
		
	}	
	
}