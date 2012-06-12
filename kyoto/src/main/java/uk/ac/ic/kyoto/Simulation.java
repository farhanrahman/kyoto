package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.monitor.Monitor;
import uk.ac.ic.kyoto.roguestates.CanadaAgent;
//import uk.ac.ic.kyoto.roguestates.TestAbsorptionHandlerAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.CarbonTarget;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
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
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
//			.addParticipantEnvironmentService(CarbonTarget.class)
			);
	
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
		
		
		
		
		/* pseudo code for Agent initialisation to implement  */
		/*
		 * The country data is not stored in simulations->parameters
		 * It is stored in  simulations->countries
		 * I.e 1 row per country, with "type" field defining what annex
		 * Which contains all the country agent init data
		   
		   1) Find out which simulation ID has been requested to run
		   2) Load all rows from simulations[ID]->countries via regular mongo load method
		   3) Loop each row to init correct agent :)
		 	foreach (countries as country) {
		    
			UUID pid = Random.randomUUID();
			
			switch(country.type) {
				case x:  // NonAnnex
					s.addParticipant(new 
				 						 NonAnnexOne(	pid, 
				 						 				country.,name,					// name String
				 						 				country.ISO						// ISO String
				 						 				country.landArea				//landArea, double
				 						 				country.arableLandArea			//arableLandArea double  
				 						 				country.GDP						//GDP double 
				 						 				country.GDPRate					//GDPRate double  
				 						 				country.availableToSpend		//availableToSpend long
				 						 				country.emissionsTarget			//emissionsTarget long
				 						 				country.carbonOffset,			//carbonOffset long 
				 						 				country.energyOutput, 			//energyOutput long
				 						 				country.carbonOutput			//carbonOutput long
				 						 				//Room to expand if teams have  their own country specific
				 						 				//coefficients.
				 						 				) 						 						 
				 					);
				case x:  // Annex 1 reduce (EU)
					s.addParticipant(new 
				 						 EUCountry(	pid, 
				 						 				country.,name,					// name String
				 						 				country.ISO						// ISO String
				 						 				country.landArea				//landArea, double
				 						 				country.arableLandArea			//arableLandArea double  
				 						 				country.GDP						//GDP double 
				 						 				country.GDPRate					//GDPRate double  
				 						 				country.availableToSpend		//availableToSpend long
				 						 				country.emissionsTarget			//emissionsTarget long
				 						 				country.carbonOffset,			//carbonOffset long 
				 						 				country.energyOutput, 			//energyOutput long
				 						 				country.carbonOutput			//carbonOutput long
				 						 				//Room to expand if teams have  their own country specific
				 						 				//coefficients.
				 						 				) 						 						 
				 					);
				case x:  // Annex 1 sustain
					s.addParticipant(new 
				 						 AbstractPostCommunistCountry(	pid, 
				 						 				country.,name,					// name String
				 						 				country.ISO						// ISO String
				 						 				country.landArea				//landArea, double
				 						 				country.arableLandArea			//arableLandArea double  
				 						 				country.GDP						//GDP double 
				 						 				country.GDPRate					//GDPRate double  
				 						 				country.availableToSpend		//availableToSpend long
				 						 				country.emissionsTarget			//emissionsTarget long
				 						 				country.carbonOffset,			//carbonOffset long 
				 						 				country.energyOutput, 			//energyOutput long
				 						 				country.carbonOutput			//carbonOutput long
				 						 				//Room to expand if teams have  their own country specific
				 						 				//coefficients.
				 						 				) 						 						 
				 					);
			} // End case
			
			
			//Left over stuff from studying LPG game
			//Player p = new Player(pid, Random.randomDouble(),	Random.randomDouble());
			//players.add(p);
			//session.insert(p);
			//session.insert(new JoinCluster(p, c));
		}
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		AbstractParticipant p = new CanadaAgent(Random.randomUUID(),"CANADA","CAN",20000,10000,5000000,3,28000,50000,30000);
		//AbstractParticipant overalTester = new TestAbsorptionHandlerAgent(Random.randomUUID(),"ABSORPTION","ABS",20000, 10000 ,5000000,3,28000,50000,30000);
		//AbstractParticipant landTester = new TestAbsorptionHandlerAgent(Random.randomUUID(),"LAND","LAN",20000, 0 ,5000000,3,28000,50000,30000);
		s.addParticipant(p);
		//s.addParticipant(overalTester);
		//s.addParticipant(landTester);
	}

}
