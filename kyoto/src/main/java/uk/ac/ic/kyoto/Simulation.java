package uk.ac.ic.kyoto;

import java.util.HashSet;
import java.util.Set;
import uk.ac.ic.kyoto.actions.AddToCarbonTargetHandler;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitorHandler;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.ic.kyoto.countries.TestAgent;
import uk.ac.ic.kyoto.market.Economy;
import uk.ac.ic.kyoto.roguestates.CanadaAgent;
import uk.ac.ic.kyoto.roguestates.TestAbsorptionHandlerAgent;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
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
	
//	@Parameter(name="GROWTH_MARKET_STATE")
//	public double GROWTH_MARKET_STATE;
//	@Parameter(name="STABLE_MARKET_STATE")
//	public double STABLE_MARKET_STATE;
//	@Parameter(name="RECESSION_MARKET_STATE")
//	public double RECESSION_MARKET_STATE;
//	@Parameter(name="GROWTH_MARKET_CHANCE")
//	public double GROWTH_MARKET_CHANCE;
//	@Parameter(name="STABLE_MARKET_CHANCE")
//	public double STABLE_MARKET_CHANCE;
//	@Parameter(name="RECESSION_MARKET_CHANCE")
//	public double RECESSION_MARKET_CHANCE;
//	@Parameter(name="MONITOR_COST_PERCENTAGE")
//	public double MONITOR_COST_PERCENTAGE;
//	@Parameter(name="SANCTION_RATE")
//	public double SANCTION_RATE;
//	@Parameter(name="MONITORING_PRICE")
//	public double MONITORING_PRICE;
//	@Parameter(name="YEARS_IN_SESSION")
//	public int YEARS_IN_SESSION;
//	@Parameter(name="TARGET_REDUCTION")
//	public double TARGET_REDUCTION;
//	@Parameter(name="MINIMUM_KYOTO_REJOIN_TIME")
//	public int MINIMUM_KYOTO_REJOIN_TIME;
//	@Parameter(name="MINIMUM_KYOTO_MEMBERSHIP_DURATION")
//	public int MINIMUM_KYOTO_MEMBERSHIP_DURATION;
	
	@Override
	protected Set<AbstractModule> getModules() {
		
//		new GameConst(GROWTH_MARKET_STATE, STABLE_MARKET_STATE, RECESSION_MARKET_STATE, 
//				GROWTH_MARKET_CHANCE, STABLE_MARKET_CHANCE, RECESSION_MARKET_CHANCE,
//				MONITOR_COST_PERCENTAGE, SANCTION_RATE, MONITORING_PRICE,
//				YEARS_IN_SESSION, TARGET_REDUCTION, MINIMUM_KYOTO_REJOIN_TIME,
//				MINIMUM_KYOTO_MEMBERSHIP_DURATION);
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(SubmitCarbonEmissionReportHandler.class)
			.addGlobalEnvironmentService(CarbonReportingService.class)
			//.addParticipantEnvironmentService(Monitor.class)
			.addParticipantEnvironmentService(ParticipantCarbonReportingService.class)
			.addGlobalEnvironmentService(GlobalTimeService.class)
			.addParticipantEnvironmentService(ParticipantTimeService.class)
			.addParticipantEnvironmentService(Economy.class)
			//.addGlobalEnvironmentService(CarbonTarget.class)
			);
	
		//modules.add(new RuleModule());
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

			carbonTarget.add1990OutputData(country.ISO, 1990Data)
			
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
		
		//String endTime = this.getParameter("finishTime");
		//AbstractParticipant p = new CanadaAgent(Random.randomUUID(),"CANADA","CAN",20000,10000,5000000,3,200000,28000,0,50000,30000);
		//AbstractParticipant p1 = new EUTest1(Random.randomUUID(), "Chris Test1", "CS1", 20000, 10000, 5000000, 3, 200000, 28000, 50000);
		//AbstractParticipant p2 = new EUTest2(Random.randomUUID(), "Chris Test2", "CS2", 20000, 10000, 5000000, 3, 200000, 28000, 50000);
		//s.addParticipant(p1);
		//s.addParticipant(p2);
//		AbstractParticipant p = new CanadaAgent(Random.randomUUID(),"CANADA","CAN",20000,10000,5000000,3,200000,28000,0,50000,30000);
		AbstractParticipant p1 = new TradeProtocolTestAgent(Random.randomUUID(), "Stuart", "CS1", 20000, 10000, 5000000, 3, 200000, 28000, 50000);
		AbstractParticipant p2 = new TradeProtocolTestAgent(Random.randomUUID(), "Farhan", "CS2", 20000, 10000, 5000000, 3, 200000, 28000, 50000);
		//AbstractParticipant p1 = new TestAgent(Random.randomUUID(), "Stuart", "LOL", 2000000, 1500000, 99999999999.00, 0.03, 70000, 50000);
		//AbstractParticipant p2 = new TestAgent(Random.randomUUID(), "Lolocaust", "LOL2", 500000, 200000, 100000, 0.07, 10000, 7000);
		s.addParticipant(p1);
		s.addParticipant(p2);
		

		//AbstractParticipant p3 = new TradeProtocolTestAgent(Random.randomUUID(), "TEST1", "TST");
		//AbstractParticipant p4 = new TradeProtocolTestAgent(Random.randomUUID(), "TEST2", "TST");
		//SimulationAgent a = new SimulationAgent(Random.randomUUID(), "SimAgent", Integer.parseInt(endTime));
		//AbstractParticipant overalTester = new TestAbsorptionHandlerAgent(Random.randomUUID(),"ABSORPTION","ABS",20000, 10000 ,5000000,3,28000,50000,30000);
		//AbstractParticipant landTester = new TestAbsorptionHandlerAgent(Random.randomUUID(),"LAND","LAN",20000, 0 ,5000000,3,28000,50000,30000);
		//s.addParticipant(p3);
		//s.addParticipant(p4);
		//s.addParticipant(a);
		//s.addParticipant(overalTester);
		//s.addParticipant(landTester);		
		
		//AbstractParticipant POOR_ABS = new TestAbsorptionHandlerAgent(Random.randomUUID(),"POOR_ABS","PA", 1000000, 500000, 5000000, 3, 50000, 30000);
		  // Should run out of money in a few ticks with current constants
		//AbstractParticipant SMALL_ABS = new TestAbsorptionHandlerAgent(Random.randomUUID(),"SMALL_ABS","SA", 1000000, 100, 5000000, 3, 50000, 30000);
		  // Should run out of land in a few ticks with current constants
		//AbstractParticipant POOR_RED = new TestReductionHandlerAgent(Random.randomUUID(),"POOR_RED","PR", 1000000, 500000, 5000000, 3, 50000, 30000);
		 // Should run out of money in a few ticks with current constants
		//AbstractParticipant CLEAN_RED = new TestReductionHandlerAgent(Random.randomUUID(),"CLEAN_RED","CR", 1000000, 500000, 5000000, 3, 50000, 300);
		 // Should run out of carbon output in a few ticks with current constants
		
		//AbstractParticipant REDVALUETEST = new TestReductionHandlerAgent(Random.randomUUID(),"REDVALUETEST","RT", 1000000, 500000, 5000000, 3, 1000, 700);
		//AbstractParticipant ABSVALUETEST = new TestAbsorptionHandlerAgent(Random.randomUUID(),"ABSVALUETEST","AT", 10000, 1000, 5000000, 3, 1000, 700);
		
		//s.addParticipant(ABSVALUETEST);
	}

}
