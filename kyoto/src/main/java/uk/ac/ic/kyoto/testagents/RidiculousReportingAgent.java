package uk.ac.ic.kyoto.testagents;

import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;

public class RidiculousReportingAgent extends AbstractCountry {

	public RidiculousReportingAgent(UUID id, String name, String ISO) {
		super(id, name, ISO);
		// TODO Auto-generated constructor stub
	}

	public RidiculousReportingAgent(UUID id, String name, String ISO,
			double landArea, double arableLandArea, double GDP, double GDPRate,
			double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP, GDPRate,
				energyOutput, carbonOutput);
		setKyotoMemberLevel(KyotoMember.ROGUE);
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
	
	@Override
	protected double getReportedCarbonOutput() {
		return getCarbonOutput();
	}

}
