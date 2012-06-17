package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.IsolatedAbstractCountry;
import uk.ac.ic.kyoto.countries.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.countries.NotEnoughCashException;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

/**
 * Extends AbstractCountry, provides a skeleton for all EU member countries
 * @author Nik
 *
 */
public class AnnexOneReduce extends IsolatedAbstractCountry {

	private EU eu;
	final private CountrySimulator simulator;
	public AnnexOneReduce(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP, double GDPRate, double energyOutput, 
			double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		simulator = new CountrySimulator(this);
	}

	@Override
	public void initialiseCountry(){
		/* Add the country to the EU service */
		try {
			this.eu = this.getEnvironmentService(EU.class);
			this.eu.addMemberState(this);
		} catch (UnavailableServiceException e) {
			System.out.println("Unable to reach EU service.");
			e.printStackTrace();
		}
	}

	/**
	 * Take an input and process the data.
	 */
	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub	
	}

	@Override
	protected void behaviour() {
		
		double reduction;
		double currentMoney = getAvailableToSpend();
		try {
			reduction = carbonReductionHandler.getCarbonOutputChange(currentMoney);
		} catch (Exception e) {
			e.printStackTrace();
			reduction = 0;
		}
		
		double cost;
		try {
			cost = carbonReductionHandler.getInvestmentRequired(reduction);
		} catch (Exception e1) {
			e1.printStackTrace();
			cost = 0;
		}
		
		try {
			carbonReductionHandler.investInCarbonReduction(reduction);
		} catch (NotEnoughCarbonOutputException e) {
			e.printStackTrace();
		} catch (NotEnoughCashException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double reduction2;
		double currentMoney2 = getAvailableToSpend();
		try {
			reduction2 = carbonAbsorptionHandler.getCarbonAbsorptionChange(currentMoney2);
		} catch (Exception e) {
			e.printStackTrace();
			reduction2 = 0;
		}
		
		double cost2;
		try {
			cost2 = carbonAbsorptionHandler.getInvestmentRequired(reduction2);
		} catch (Exception e1) {
			e1.printStackTrace();
			cost2 = 0;
		}
		
		try {
			carbonAbsorptionHandler.investInCarbonAbsorption(reduction2);
		} catch (NotEnoughCarbonOutputException e) {
			e.printStackTrace();
		} catch (NotEnoughCashException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		CountrySimulator.CountryState optimalState = 
				simulator.simulate(getCarbonOutput(),getEnergyOutput(),
				getCarbonOffset(),getCarbonAbsorption(), getEmissionsTarget(), getAvailableToSpend(),
				getGDP(),getGDPRate(),getArableLandArea());

	}

	/**
	 * For a given amount of carbon to reduce, return the amount of money we should invest in Absorption and Reduction.
	 * For safety's sake, will tend to overestimate a bit.
	 * @param carbonReduction Amount to reduce carbon by
	 * @param state A given country state
	 * @param investments Pass in a double[2], returned [0] = money to invest in Absorption, [1] = money to invest in Reduction
	 * @return Total Cost
	 */
	public double getAbsorbReduceInvestment(double carbonReduction,CountrySimulator.CountryState state, double[] investments) {

		if (carbonReduction <= 0) {
			investments[0] = 0;
			investments[1] = 0;
			return 0;
		}
		
		//Overestimate a bit
		carbonReduction*=1.01;
		
		double prevCost;
		try {
			prevCost = this.carbonAbsorptionHandler.getInvestmentRequired(carbonReduction,state.arableLandArea);
		} catch (Exception e) {
			e.printStackTrace();
			investments[0] = 0;
			investments[1] = 0;
			return 0;
		}

		double absorbFrac = 0.5;
		double reduceFrac = 0.5;
		
		double fracDiff = 0.25;

		//Attempt to minimise cost for a given amount of carbon
		for (int i = 0; i< 10; i++) {

			double absorbCost;
			double reduceCost;

			try {
				absorbCost = this.carbonAbsorptionHandler.getInvestmentRequired(absorbFrac * carbonReduction,state.arableLandArea);
				reduceCost = this.carbonReductionHandler.getInvestmentRequired(reduceFrac * carbonReduction,state.carbonOutput,state.energyOutput);
			} catch (Exception e) {
				e.printStackTrace();
				investments[0] = 0;
				investments[1] = 0;
				return 0;
			}
			
			double totalCost = absorbCost + reduceCost;
			
			if (totalCost < prevCost) {
				reduceFrac += fracDiff;
				absorbFrac -= fracDiff;
			}
			else {
				reduceFrac -= fracDiff;
				absorbFrac += fracDiff;
			}
			prevCost = totalCost;
			fracDiff/=2;
		}

		absorbFrac = ((double) Math.round(1000 * absorbFrac))/1000;
		reduceFrac = ((double) Math.round(1000 * reduceFrac))/1000;

		try {
			if (absorbFrac == 0) {
				investments[0] = 0;
			}
			else {
				investments[0] = this.carbonAbsorptionHandler.getInvestmentRequired(absorbFrac * carbonReduction,state.arableLandArea);
			}

			if (reduceFrac == 0) {
				investments[1] = 0;
			}
			else {
				investments[1] = this.carbonReductionHandler.getInvestmentRequired(reduceFrac * carbonReduction,state.carbonOutput,state.energyOutput);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return (investments[0] + investments[1]);
	}
	
	public double getArableLandCost(double absorbInvestment, double arableLandArea){
		
		if (absorbInvestment <= 0) {
			return 0;
		}
		
		double carbonAbsorptionChange;
		double cost;
		
		try {
			carbonAbsorptionChange = carbonAbsorptionHandler.getCarbonAbsorptionChange(absorbInvestment,arableLandArea);
			
			cost = carbonAbsorptionHandler.getForestAreaRequired(carbonAbsorptionChange);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return cost;
	}
	
	public double getCarbonAbsorptionChange(double investmentAmount, double arableLandArea){
		
		if (investmentAmount <= 0) {
			return 0;
		}
		
		double change;
		
		try {
			change = carbonAbsorptionHandler.getCarbonAbsorptionChange(investmentAmount, arableLandArea);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		return change;
	}
	
	public double getCarbonReduction(double reductionCost,CountrySimulator.CountryState state) {
		
		if (reductionCost <= 0) {
			return 0;
		}
		double reduction;
		try {
			reduction = carbonReductionHandler.getCarbonOutputChange(reductionCost, state.carbonOutput, state.energyOutput);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return reduction;
	}

	/**
	 * 
	 * @param carbonOffset The amount of carbon we want to offset by buying credits
	 * @param year The year we want to get the estimated price for. Year = 1 is current year, Year = 2 is next year etc.
	 * @return Cost of purchasing credits. Returns a very, very large number if not enough credits available.
	 */
	public double getMarketBuyPrice(double carbonOffset, int year) {
		
		if (carbonOffset==0) {
			return 0;
		}
		//TODO return something other than a very big number
		return (Double.MAX_VALUE / 1000000);
	}
	
	//TODO
	public double getMarketSellPrice(double carbonOffset, int year) {
		return 0;
	}
	
	public double getCarbonEnergyIncrease(double industryInvestment){
	
		if (industryInvestment <= 0) {
			return 0;
		}
		
		double increase;
		
		try {
			increase = energyUsageHandler.calculateCarbonIndustryGrowth(industryInvestment);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
			
		return increase;
	}
	
	public double getNextEmissionTarget(double emissionsTarget) {
		//TODO currently returns old emissions target times the 10th root of 0.95
		return emissionsTarget * 0.99488;
	}

	@Override
	protected void yearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sessionFunction() {
		// TODO Auto-generated method stub
		
	}
}
