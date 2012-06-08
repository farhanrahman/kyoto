package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.monitor.Monitor;
import uk.ac.ic.kyoto.roguestates.CanadaAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.TimeService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import com.google.inject.AbstractModule;

public class Simulation extends InjectedSimulation {
	
//	@Parameter(name="annexOneReduceCount")
//	public int annexOneReduceCount = 1;
//	@Parameter(name="annexOneSustainCount")
//	public int annexOneSustainCount = 1;
//	@Parameter(name="annexTwoCount")
//	public int annexTwoCount = 1;
//	@Parameter(name="nonParticipantCount")
//	public int nonParticipantCount = 1;
	
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addParticipantEnvironmentService(Monitor.class)
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
			.addParticipantEnvironmentService(TimeService.class)
			.addParticipantEnvironmentService(Economy.class));		
	
		modules.add(new RuleModule());
			//.addClasspathDrlFile("foo.drl")
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	public Simulation(Set<AbstractModule> modules) {
		super(modules);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addToScenario(Scenario s) {
		// TODO Auto-generated method stub
		
		//Something new
		AbstractParticipant p = new CanadaAgent(Random.randomUUID(),"CANADA","CAN",20000,10000,5000000,3,200000,28000,0,50000,30000);
		s.addParticipant(p);
		
	}

}
