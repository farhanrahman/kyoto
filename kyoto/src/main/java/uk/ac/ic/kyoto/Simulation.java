package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.roguestates.CanadaAgent;
import uk.ac.ic.kyoto.roguestates.FakeCanadaAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class Simulation extends InjectedSimulation {
	
	/*@Parameter(name="annexOneReduceCount")
	public int annexOneReduceCount = 1;
	@Parameter(name="annexOneSustainCount")
	public int annexOneSustainCount = 1;
	@Parameter(name="annexTwoCount")
	public int annexTwoCount = 1;
	@Parameter(name="nonParticipantCount")
	public int nonParticipantCount = 1;*/
	
	public Simulation(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		/*modules.add(new AbstractEnvironmentModule()
			//.addParticipantEnvironmentService(FooService.class)
			//.addParticipantGlobalEnvironmentService(FooService.class)
			.addGlobalEnvironmentService(CarbonReportingService.class));
			//.addActionHandler(FooHandler.class)*/
		
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class));		
	
		//*modules.add(new RuleModule());
			//.addClasspathDrlFile("foo.drl")*/
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {
		// TODO Auto-generated method stub
		AbstractParticipant p = new CanadaAgent(Random.randomUUID(), "Canada", "CND", 10000, 5000, 1000000, 2, 500000, 1000, -200, 10);
		s.addParticipant(p);
		
		AbstractParticipant q = new CanadaAgent(Random.randomUUID(), "Canada", "CND", 10000, 5000, 1000000, 2, 500000, 1000, 200, 10);
		s.addParticipant(q);

	}

}
