package uk.ac.ic.kyoto.monitor;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Time;

import com.google.inject.Singleton;

/**
 * Awaiting MongoDB access agent for implementation code. 
 * @author Tom C
 *
 */
@Singleton
public class MonitoredCountriesImp implements MonitoredCountries{
	
	public boolean IsMonitored(UUID id){
		/* 
		 * Check against MongoDB 
		 */
		return false;
	}
	
	public void addToMonitored(UUID id) {
	/*
	 * Add to MongoDB	
	 */
	}
	
	public void RemoveFromMonitored(UUID id) {
	/*
	 * Remove from MongoDB	
	 */
	}
}