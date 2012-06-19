package uk.ac.ic.kyoto.annex1sustain;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.countries.AbstractCountry.KyotoMember;
import uk.ac.ic.kyoto.exceptions.NotEnoughCarbonOutputException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;

/**
 * @author Piotr
 */
public class AnnexOneSustain extends AbstractCountry {
	
	
	//================================================================================
    // Protected Fields
    //================================================================================
	
	protected double	surplusCarbonTarget;		// Number of credits we can and want to sell without carbon absorption/reduction
	protected double	surplusCarbonSold;			// Number of credits that we sold this year
	protected double	surplusCarbonPrice;			// Price of surplus carbon while within target
	protected double	carbonToReduce;				// Carbon reduction that we need to perform before end of year to match target
	protected double	carbonToAbsorb;				// Carbon absorption that we need to perform before end of year to match target
	protected double	decisionTreshold;			// Specifies where our acceptable price will lie between two extreme profitability points
	
	
	//================================================================================
    // Constructors
    //================================================================================
	
	public AnnexOneSustain(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			long energyOutput, long carbonOutput)
	{
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		this.surplusCarbonTarget = 0;
		this.surplusCarbonSold = 0;
		this.surplusCarbonPrice = 1;
		this.carbonToReduce = 0;
		this.carbonToAbsorb = 0;
		this.decisionTreshold = 0.5;
	}
	
	
	//================================================================================
    // Input function
    //================================================================================
	
	/**
	 * Function processing input
	 */
	@Override
	protected void processInput(Input input) {
		// TODO process input (if any)
	}
	
	/**
	 * Initialisation function
	 */
	@Override
	protected void initialiseCountry() {
		setKyotoMemberLevel(KyotoMember.ANNEXONE);
	}
	
	/**
	 * Behaviour function
	 */
	@Override
	protected void behaviour() {
		// TODO behaviour
	}
	
	@Override
	protected boolean acceptTrade(NetworkAddress address, Offer offer) {
		// TODO accepting trade
		return false;
	}
	
	//================================================================================
    // Periodic functions
    //================================================================================
	
	/**
	 * Function called at the end of each tick.
	 * - Updates the internal variables.
	 * - Decides on and initiates investments
	 * 
	 * @param e
	 * The event that is called every simulation tick
	 */
	@EventListener
	public void TickFunction(EndOfTimeCycle e) {
		//updateUncommittedTransactions();
		//updateCommittedTransactions();
		//updateInternalPrice();
		//logger.info("Internal Data of Post-Communist Country " + this.getName() + " was updated");
		//makeInvestments();
	}
	
	/**
	 * Function called at the beginning of each year.
	 */
	@Override
	public void yearlyFunction() {
		investInIndustry();
		scaleDecisionTreshold();
		updateSurplusCarbonTarget();
		resetSurplusCarbonSold();
	}
	
	/**
	 * Function called at the end of each session.
	 */
	@Override
	public void sessionFunction() {
		// TODO implement
	}
	
	
	protected void investInIndustry() {
		// Find a point at which investing in industry and scaling back carbon with reduction/absorption
		//  costs availableToSpend() * constant.
		// Use binary search, do the cheaper option, or reduction if not enough land.
	}
	
	protected void scaleDecisionTreshold() {
		double percentageSold;
		
		try {
			percentageSold = surplusCarbonSold / surplusCarbonTarget;
			
			if (percentageSold > decisionTreshold) {
				decisionTreshold /= Constants.DECISION_TRESHOLD_SCALER;
				logger.info("Sold more than expected, decreasing perceived profitability treshold");
			}
			else {
				decisionTreshold *= Constants.DECISION_TRESHOLD_SCALER;
				logger.info("Sold less than expected, increasing perceived profitability treshold");
			}
		}
		catch (ArithmeticException e) {
			logger.info("There was no surplus carbon to sell, nothing to do");
		}
		catch (Exception e) {
			logger.warn("Problem with scaling decision treshold: " + e.getMessage());
		}
	}
	
	protected void updateSurplusCarbonTarget() {
		surplusCarbonTarget = this.getEmissionsTarget() - (this.getCarbonOutput() - this.getCarbonAbsorption()) + this.getCarbonOffset();
	}
	
	protected void resetSurplusCarbonSold() {
		surplusCarbonSold = 0;
	}
	
	protected void finalInvestments() {
		try {
			carbonReductionHandler.investInCarbonReduction(carbonToReduce);
			carbonAbsorptionHandler.investInCarbonAbsorption(carbonToAbsorb);
		}
		catch (Exception e) {
			logger.warn("Problem with end of year investments: " + e.getMessage());
		}
	}
	
}