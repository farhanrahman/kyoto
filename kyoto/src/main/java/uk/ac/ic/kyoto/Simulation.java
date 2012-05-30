package uk.ac.ic.kyoto;

import java.util.Set;
import com.google.inject.AbstractModule;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addToScenario(Scenario s) {
		// TODO Auto-generated method stub

	}

}
