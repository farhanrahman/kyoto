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
import uk.ac.ic.kyoto.countries.testCountries.DoNothing;
import uk.ac.ic.kyoto.countries.testCountries.GDPTestCount;
import uk.ac.ic.kyoto.nonannexone.NonAnnexOne;
import uk.ac.ic.kyoto.roguestates.USAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.Economy;
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
 *Sits through multiple years and watches its cash do something
 * 
 * @author ct
 *
 */

public class RogueStateTest extends InjectedSimulation {
	
//	@Parameter(name="countries")
//	public int countries;

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

	public RogueStateTest(Set<AbstractModule> modules) {
		super(modules);
	}
	
	@Override
	protected void addToScenario(Scenario s) {
		
		// Russia Stats
		final double landArea = 16377742;
		final double arableLandArea = 1218599;
		final double GDP = 2.41602E+11;
		final double energyOutput = 1571812255.0;
		final double carbonOutput = 1511270000.0;
		final double GDPRate = -0.0481;

		//className,name,ISO,landArea,arableLandArea,GDP,GDPRate,energyOutput,carbonOutput,carbonOutput1990
		//AnnexOneSustain,Russia,RUS,16377742,1218599,2.41602E+11,-0.0481,1571812255,1511270000,2220721000	
		String name1 = "Russia";
		String ISO1 = "RUS";
		
		AbstractParticipant p1 = new NonAnnexOne(Random.randomUUID(), name1, ISO1, landArea, arableLandArea, GDP, GDPRate, energyOutput, carbonOutput);
		s.addParticipant(p1);
		CarbonData1990.addCountry(ISO1, 2220721000.0);
		
		// US Stats
		final double landArea2 = 9161966;
		final double arableLandArea2 = 1650062;
		final double GDP2 = 8.741E+12;
		final double energyOutput2 = 6304070667.0;
		final double carbonOutput2 = 5449078000.0;
		final double GDPRate2 = 0.0492;		
			
		//className,name,ISO,landArea,arableLandArea,GDP,GDPRate,energyOutput,carbonOutput,carbonOutput1990			
		//USAgent,United States,USA,9161966,1650062,8.741E+12,0.0492,6304070667,5449078000,4879376000		
		String name2 = "United States";
		String ISO2 = "USA";
		
		AbstractParticipant p2 = new USAgent(Random.randomUUID(), name2, ISO2, landArea2, arableLandArea2, GDP2, GDPRate2, energyOutput2, carbonOutput2);
		s.addParticipant(p2);
		CarbonData1990.addCountry(ISO2, 4879376000.0);
		
	}
}

