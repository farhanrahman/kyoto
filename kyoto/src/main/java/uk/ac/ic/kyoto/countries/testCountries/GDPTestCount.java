package uk.ac.ic.kyoto.countries.testCountries;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.GameConst;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
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
	int i =0 ;
	@Override
	protected void behaviour() {
//		double GDPRate = getGDPRate();
//		double GDP = getGDP();
//		double prevGDP = GDP;
//		double cash = 0;
//		double prevGDPRate = GDPRate;
		/*
		 * Looking to find the difference between if there had been investment and if there hadn't
		 */
		if (i == 0){
			try {
				System.out.println("Reducing energy output by 10%");
				energyUsageHandler.reduceEnergyOutput(getEnergyOutput()*0.1);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
//		
//		double sum = (((getEnergyOutput()-getPrevEnergyOut())/getPrevEnergyOut())*GameConst.getEnergyGrowthScaler() +getGDPRate()*100)/2;
//		GDPRate = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-sum*GameConst.getGrowthScaler());
//		GDPRate /= 100;
//		
//		double prevSum = (((getPrevEnergyOut()-getPrevEnergyOut())/getPrevEnergyOut())*GameConst.getEnergyGrowthScaler() +getGDPRate()*100)/2;
//		double prevGDPRate = GameConst.getMaxGDPGrowth()-GameConst.getMaxGDPGrowth()*Math.exp(-prevSum*GameConst.getGrowthScaler());;
//		prevGDPRate /=100;
//		
//		GDP += GDP*GDPRate;
//		prevGDP +=prevGDP*prevGDPRate;
//		cash += GDP*GameConst.getPercentageOfGdp()-prevGDP*GameConst.getPercentageOfGdp();
//		
		
//		try {
//			energyUsageHandler.reduceEnergyOutput(1000000);
//		} catch (NotEnoughCarbonOutputException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		logger.debug("Current GDP: " + getGDP());
//		logger.debug("Current Return Cash: " + cash);
//		logger.debug("Previous GDP Rate: " +prevGDPRate*100);
		logger.debug("Current GDP Rate: " + getGDPRate()*100);
		logger.debug("Availiable to Spend: " + getAvailableToSpend());
		logger.debug("Current Energy Output: " + this.getEnergyOutput());
		logger.debug("Current Previous Energy Output: " + this.getPrevEnergyOutput());
		logger.debug("Energy Ratio: "+ (getEnergyOutput()-getPrevEnergyOutput())/getPrevEnergyOutput());
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
