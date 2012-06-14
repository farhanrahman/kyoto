package uk.ac.ic.kyoto.annex1reduce;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.IsolatedAbstractCountry;
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
	
	public AnnexOneReduce(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double energyOutput, double carbonOutput) {

		
		super(id, name, ISO, landArea, arableLandArea, GDP,
					GDPRate, energyOutput, carbonOutput);
		
		simulator = new CountrySimulator(this);

	}

	@Override
	public void initialiseCountry(){
		
		// Add the country to the EU service
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
		
		simulator.simulate(this.carbonOutput,this.energyOutput,this.carbonOffset,
				this.emissionsTarget,this.availableToSpend,this.GDP,this.GDPRate,
				this.arableLandArea);
		
	}
	
	final private static int NUM_ITERATIONS = 3;
	
	/**
	 * For a given amount of carbon to reduce, return the amount of money we should invest in Absorption and Reduction.
	 * For safety's sake, will tend to overestimate a bit.
	 * @param carbonReduction Amount to reduce carbon by
	 * @param state A given country state
	 * @param investments Pass in a double[2], returned [0] = money to invest in Absorption, [1] = money to invest in Reduction
	 * @return Total Cost
	 */
	public double getAbsorbReduceInvestment(double carbonReduction,CountrySimulator.CountryState state, double[] investments) {
		
		if (carbonReduction <= 0) return 0;
		
		//Overestimate a bit
		carbonReduction*=1.02;
		
		double absorbFrac = 0.5;
		double reduceFrac = 0.5;
		
		//Attempt to minimise cost for a given amount of carbon
		for (int i = 0; i< NUM_ITERATIONS; i++) {
			
			double absorbCost;
			double reduceCost;
			
			try {
				absorbCost = this.carbonAbsorptionHandler.getInvestmentRequired(absorbFrac * carbonReduction,state.arableLandArea);
				reduceCost = this.carbonReductionHandler.getInvestmentRequired(reduceFrac * carbonReduction,state.carbonOutput,state.energyOutput);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
			
			double frac = absorbCost/(absorbCost + reduceCost);
			absorbFrac*=(1-frac);
			reduceFrac*=frac;
			
			double totalFrac = (absorbFrac + reduceFrac);
			
			absorbFrac/=totalFrac;
			reduceFrac/=totalFrac;
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

//		try {
//			System.out.println("absorb,reduce carbon amounts");
//			System.out.println(this.carbonAbsorptionHandler.getCarbonAbsorptionChange(investments[0]));
//			System.out.println(this.carbonReductionHandler.getCarbonOutputChange(investments[1]));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		return (investments[0] + investments[1]);
	}
	
	/**
	 * TODO
	 * @return
	 */
	public double getMarketPrice() {
		return 0;
	}
	

	@Override
	public void YearlyFunction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SessionFunction() {
		// TODO Auto-generated method stub
		
	}

}
