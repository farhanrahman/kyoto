package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

public class GDPTestCount extends AbstractCountry {

	public GDPTestCount(UUID id, String name, String ISO, double landArea,
			double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput,
				carbonOutput);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void behaviour() {
		double GDPRate = getGDPRate();
		double GDP = getGDP();
		try {
			System.out.println("Investment of 100000: " + energyUsageHandler.calculateCarbonIndustryGrowth(100000));
			energyUsageHandler.investInCarbonIndustry(100000);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double sum = (((getEnergyOutput()-getPrevEnergyOut())/getPrevEnergyOut())*GameConst.getEnergyGrowthScaler() +getGDPRate()*100)/2;
		GDPRate = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler());
		
		GDPRate /= 100;
		double prevGDP = GDP;
		GDP += GDP*GDPRate;
		double cash = GDP*GameConst.getPercentageOfGdp()-prevGDP*GameConst.getPercentageOfGdp();
		
		logger.debug("Previous GDP: " + getGDP());
		logger.debug("Current GDP: " + GDP);
		logger.debug("Current Return Cash: " + cash);
		logger.debug("Current GDP Rate: " + GDPRate*100);
		
		
		logger.debug("Current Energy Output: " + this.getEnergyOutput());
		logger.debug("Current Previous Energy Output: " + this.getPrevEnergyOut());
		logger.debug("Current CO2 Output: " + this.getCarbonOutput());
		logger.debug("Emissions Target: " + this.getEmissionsTarget());
		

	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void yearlyFunction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sessionFunction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initialiseCountry() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean acceptTrade(NetworkAddress from, Offer trade) {
		// TODO Auto-generated method stub
		return false;
	}

}
