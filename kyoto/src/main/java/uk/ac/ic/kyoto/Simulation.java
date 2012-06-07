package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;

import com.google.inject.AbstractModule;

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
		/*modules.add(new AbstractEnvironmentModule()
			//.addParticipantEnvironmentService(FooService.class)
			//.addParticipantGlobalEnvironmentService(FooService.class)
			.addGlobalEnvironmentService(CarbonReportingService.class));
			//.addActionHandler(FooHandler.class)*/
		
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class));		
	
		modules.add(new RuleModule());
			//.addClasspathDrlFile("foo.drl")
		
		modules.add(NetworkModule.fullyConnectedNetworkModule().withNodeDiscovery());
		
		return modules;
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
		   
		   1) Load all rows from simulations->countries via regular mongo load method
		   
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
		 */
		
		
		
	}

}
