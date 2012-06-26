package uk.ac.ic.kyoto.simulations;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.ic.kyoto.annex1sustain.AnnexOneSustain;
import uk.ac.ic.kyoto.CarbonData1990;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitorHandler;
import uk.ac.ic.kyoto.actions.AddToCarbonTargetHandler;
import uk.ac.ic.kyoto.actions.ApplyMonitorTaxHandler;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.ic.kyoto.countries.testCountries.GDPTestCount;
import uk.ac.ic.kyoto.nonannexone.NonAnnexOne;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.trade.TradeProtocolTestAgent;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import com.google.inject.AbstractModule;

public class AnnexOneSustainTest extends InjectedSimulation {
	
	@Override
	protected Set<AbstractModule> getModules() {
		
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addActionHandler(AddToCarbonTargetHandler.class)
			.addActionHandler(QueryEmissionsTargetHandler.class)
			.addActionHandler(AddRemoveFromMonitorHandler.class)
			.addActionHandler(ApplyMonitorTaxHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addGlobalEnvironmentService(Monitor.class)
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
			.addGlobalEnvironmentService(CarbonTarget.class)
			);
	
		modules.add(new RuleModule());
			//.addClasspathDrlFile("foo.drl")
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	public AnnexOneSustainTest(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected void addToScenario(Scenario s) {
		
		// Germany Stats
		final double landArea = 400000;
		final double arableLandArea = 10;
		final double GDP = 2000000000000.0;
		final double energyOutput = 1000000000;
		final double carbonOutput = 1000000000;
		final double GDPRate = 0.0206;
		
		String name = "AnnexOneSustainTester";
		String ISO = "DE";
		
		String name2 = "TradeBot";
		String ISO2 = "TB";
			
		AbstractParticipant p1 = new AnnexOneSustain(Random.randomUUID(), name, ISO, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		AbstractParticipant p2 = new TradeProtocolTestAgent(Random.randomUUID(), name2, ISO2, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		
		s.addParticipant(p1);
		s.addParticipant(p2);
		
		CarbonData1990.addCountry(ISO, 1000000000);
		CarbonData1990.addCountry(ISO2, 1000000000);	
	}
}
