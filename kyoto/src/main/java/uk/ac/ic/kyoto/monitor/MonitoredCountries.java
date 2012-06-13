package uk.ac.ic.kyoto.monitor;

import java.util.UUID;

import uk.ac.imperial.presage2.core.Time;

/*
 * Interface for checking which countries should be monitored at any point in time.
 * Countries Canada and USA have the possibility of leaving/joining the protocol
 * and thus the trading and monitoring framework during execution of the simulation. 
 * @author Tom C
 *
 */

public interface MonitoredCountries {
	
	public boolean IsMonitored(UUID id);
	
	public void addToMonitored(UUID id);
	
	public void RemoveFromMonitored(UUID id);
}
