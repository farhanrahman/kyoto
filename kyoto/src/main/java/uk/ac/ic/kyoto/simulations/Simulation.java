package uk.ac.ic.kyoto.simulations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import uk.ac.ic.kyoto.CarbonData1990;
import uk.ac.ic.kyoto.actions.AddRemoveFromMonitorHandler;
import uk.ac.ic.kyoto.actions.AddToCarbonTargetHandler;
import uk.ac.ic.kyoto.actions.ApplyMonitorTaxHandler;
import uk.ac.ic.kyoto.actions.QueryEmissionsTargetHandler;
import uk.ac.ic.kyoto.actions.SubmitCarbonEmissionReportHandler;
import uk.ac.ic.kyoto.annex1sustain.AnnexOneSustain;
import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.CarbonTarget;
import uk.ac.ic.kyoto.countries.Monitor;
import uk.ac.ic.kyoto.exceptions.NoCountryDataException;
import uk.ac.ic.kyoto.nonannexone.NonAnnexOne;
import uk.ac.ic.kyoto.services.CarbonReportingService;
import uk.ac.ic.kyoto.services.Decoder;
import uk.ac.ic.kyoto.services.Economy;
import uk.ac.ic.kyoto.services.GlobalTimeService;
import uk.ac.ic.kyoto.services.ParticipantCarbonReportingService;
import uk.ac.ic.kyoto.services.ParticipantTimeService;
import uk.ac.ic.kyoto.singletonfactory.SingletonProvider;
import uk.ac.ic.kyoto.tradehistory.TradeHistoryService;
import uk.ac.ic.kyoto.util.sim.jsonobjects.DataProvider;
import uk.ac.ic.kyoto.util.sim.jsonobjects.JSONObjectContainer;
import uk.ac.ic.kyoto.util.sim.jsonobjects.simulations.CountryData;
import uk.ac.ic.kyoto.util.sim.jsonobjects.simulations.SimulationData;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;
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
			.addGlobalEnvironmentService(TradeHistoryService.class)
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
		Logger logger = Logger.getLogger(Simulation.class);
		try{
			JSONObjectContainer<SimulationData> obj = new DataProvider().getSimulationData(this.simPersist.getID());
			
			if(obj.getObject().getCountries() == null || obj.getObject().getCountries().isEmpty()){
				//TODO uncomment for final code
				throw new NoCountryDataException(); //Commented out for now.
			}
				
			if(obj.getObject().getCountries() != null && !obj.getObject().getCountries().isEmpty()){
				Map<String,CountryData> countries = obj.getObject().getCountries();
				for(String countryKey : countries.keySet()){
					logger.info(countries.get(countryKey));
					String className = countries.get(countryKey).getClassName();
					CountryData countryData = countries.get(countryKey);
					AbstractCountry abstractCountry = null;
					if(className.equals("NonAnnexOne")){
						abstractCountry = new NonAnnexOne(
										Random.randomUUID(), 
										countryData.getName(),
										countryData.getISO(), 
										Double.parseDouble(countryData.getLandArea()), 
										Double.parseDouble(countryData.getArableLandArea()), 
										Double.parseDouble(countryData.getGDP()),
										Double.parseDouble(countryData.getGDPRate()), 
										Double.parseDouble(countryData.getEnergyOutput()), 
										Double.parseDouble(countryData.getCarbonOutput()));
					} else if(className.equals("AnnexOneReduce")){
//						abstractCountry = new AnnexOneReduce(
//										Random.randomUUID(), 
//										countryData.getName(),
//										countryData.getISO(), 
//										Double.parseDouble(countryData.getLandArea()), 
//										Double.parseDouble(countryData.getArableLandArea()), 
//										Double.parseDouble(countryData.getGDP()),
//										Double.parseDouble(countryData.getGDPRate()), 
//										Double.parseDouble(countryData.getEnergyOutput()), 
//										Double.parseDouble(countryData.getCarbonOutput()));
					} else if(className.equals("CanadaAgent")){
//						abstractCountry = new CanadaAgent(
//										Random.randomUUID(), 
//										countryData.getName(),
//										countryData.getISO(), 
//										Double.parseDouble(countryData.getLandArea()), 
//										Double.parseDouble(countryData.getArableLandArea()), 
//										Double.parseDouble(countryData.getGDP()),
//										Double.parseDouble(countryData.getGDPRate()),
//										0.00,//Double.parseDouble(countryData.getEmissionsTarget()), //EmissionsTarget not specified yet
//										Double.parseDouble(countryData.getEnergyOutput()), 
//										Double.parseDouble(countryData.getCarbonOutput()));					
					} else if(className.equals("AnnexOneSustain")){
						abstractCountry = new AnnexOneSustain(
										Random.randomUUID(), 
										countryData.getName(),
										countryData.getISO(), 
										Double.parseDouble(countryData.getLandArea()), 
										Double.parseDouble(countryData.getArableLandArea()), 
										Double.parseDouble(countryData.getGDP()),
										Double.parseDouble(countryData.getGDPRate()),
										Long.parseLong(countryData.getEnergyOutput()), 
										Long.parseLong(countryData.getCarbonOutput()));		
					} else if(className.equals("USAgent")){
						
					}
					
					CarbonData1990.addCountry(countries.get(countryKey).getISO(), Double.parseDouble(countries.get(countryKey).getCarbonOutput1990()));
					
					if(abstractCountry != null){
						//TODO uncomment for final code
						Decoder.addCountry(abstractCountry.getID(), abstractCountry.getName(), abstractCountry.getISO());
						s.addParticipant(abstractCountry);
					}
				}
			}		
		
		} catch(NoCountryDataException e){
			logger.warn(e);
		}
		
		SingletonProvider.getTradeHistory().setSimID(this.simPersist.getID());
	

	}
}
