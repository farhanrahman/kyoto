package uk.ac.ic.kyoto.simulations;

import java.util.HashSet;
import java.util.Set;
import uk.ac.ic.kyoto.countries.testCountries.DoNothing;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import com.google.inject.AbstractModule;

/**
 * 
 * Creates 2 agents who do no reporting and just exist independent of each other.
 * 
 * @author cmd08
 *
 */

public class NoKyoto extends InjectedSimulation {

	@Override
	protected Set<AbstractModule> getModules() {
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
/* No carbon targets in this sim, no Kyoto agreement at all! */
//			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
//			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
//			.addActionHandler(AddToCarbonTargetHandler.class)
//			.addActionHandler(QueryEmissionsTargetHandler.class)
//			.addActionHandler(AddRemoveFromMonitorHandler.class)
//			.addActionHandler(ApplyMonitorTaxHandler.class)
//			.addGlobalEnvironmentService(CarbonReportingService.class)
//			.addGlobalEnvironmentService(Monitor.class)
//			.addGlobalEnvironmentService(CarbonTarget.class)
			);
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	public NoKyoto(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected void addToScenario(Scenario s) {
//		DoNothing(id, name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput)
		AbstractParticipant p1 = new DoNothing(Random.randomUUID(), "Stuart", "LOL", 2000000, 1500000, 99999999999.00, 0.03, 70000, 50000);
		AbstractParticipant p2 = new DoNothing(Random.randomUUID(), "Lolocaust", "LOL2", 500000, 200000, 100000, 0.07, 10000, 7000);

		s.addParticipant(p1);
		s.addParticipant(p2);
	}
}
