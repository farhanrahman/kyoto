package uk.ac.ic.kyoto.simulations;

import java.util.HashSet;
import java.util.Set;
import uk.ac.ic.kyoto.CarbonData1990;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitorHandler;
import uk.ac.ic.kyoto.actions.AddToCarbonTargetHandler;
import uk.ac.ic.kyoto.actions.ApplyMonitorTaxHandler;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.ic.kyoto.countries.testCountries.Reduce;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import com.google.inject.AbstractModule;

/**
 * 
 * Creates N agents who reduce their CO2 output as far as possible at any cost.
 * 
 * @author cmd08
 *
 */

public class ReduceSimulation extends InjectedSimulation {
	
	@Parameter(name="countries")
	public int countries;

	@Override
	protected Set<AbstractModule> getModules() {
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
/* No carbon targets in this sim, no Kyoto agreement at all! */
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addActionHandler(AddToCarbonTargetHandler.class)
			.addActionHandler(QueryEmissionsTargetHandler.class)
			.addActionHandler(AddRemoveFromMonitorHandler.class)
			.addActionHandler(ApplyMonitorTaxHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addGlobalEnvironmentService(Monitor.class)
			.addGlobalEnvironmentService(CarbonTarget.class)
			);
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	public ReduceSimulation(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected void addToScenario(Scenario s) {
		final double landArea = 10000;
		final double arableLandArea = 1500000;
		final double GDP = 100000;
		final double energyOutput = 100000;
		final double carbonOutput = 70000;
		final double GDPRate = 0.03;
		
		for(int i = 0; i < countries; i++){
			String name = "Country"+i;
			String ISO = "DN"+i;
			AbstractParticipant p = new Reduce(Random.randomUUID(), name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
			s.addParticipant(p);
			CarbonData1990.addCountry(ISO, energyOutput);
		}
	}
}

