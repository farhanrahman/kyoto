package uk.ac.ic.kyoto.simulations;

import java.util.UUID;

import org.drools.command.runtime.GetKnowledgeBaseCommand;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.exceptions.CannotJoinKyotoException;
import uk.ac.ic.kyoto.exceptions.CannotLeaveKyotoException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

public class LeavingRejoiningKyotoAgent extends AbstractCountry {

	public LeavingRejoiningKyotoAgent(UUID id, String name, String ISO) {
		super(id, name, ISO);
		// TODO Auto-generated constructor stub
	}

	public LeavingRejoiningKyotoAgent(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate,
				energyOutput, carbonOutput);
	}

	@Override
	protected void behaviour() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void yearlyFunction() {
		
		logger.info("BEFOREHAND: " + isKyotoMember());
		
//		try {
//			leaveKyoto();
//		} catch (CannotLeaveKyotoException e) {
//			e.printStackTrace();
//		}
//		try {
//			joinKyoto();
//		} catch (CannotJoinKyotoException e) {
//			e.printStackTrace();
//		}
		logger.info("MEMBERSHIP LEVEL: " + isKyotoMember()); 
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
