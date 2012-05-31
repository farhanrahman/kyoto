package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;
import com.google.inject.AbstractModule;

import uk.ac.ic.kyoto.KnowledgeBaseService;

import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;

public class Simulation extends InjectedSimulation {
	
	@Parameter(name="annexOneReduceCount")
	public int annexOneReduceCount = 1;
	@Parameter(name="annexOneSustainCount")
	public int annexOneSustainCount = 1;
	@Parameter(name="annexTwoCount")
	public int annexTwoCount = 1;
	@Parameter(name="nonParticipantCount")
	public int nonParticipantCount = 1;
	
	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		modules.add(new AbstractEnvironmentModule()
			//.addParticipantEnvironmentService(FooService.class)
			//.addParticipantGlobalEnvironmentService(FooService.class)
			.addParticipantGlobalEnvironmentService(KnowledgeBaseService.class));
			//.addActionHandler(FooHandler.class)
		modules.add(new RuleModule());
			//.addClasspathDrlFile("foo.drl")
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {
		// TODO Auto-generated method stub

	}

}
