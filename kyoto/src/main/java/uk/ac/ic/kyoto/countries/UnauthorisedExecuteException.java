/**
 * 
 */
package uk.ac.ic.kyoto.countries;

import java.util.UUID;

/**
 * @author farhanrahman
 *
 */
public class UnauthorisedExecuteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6996816687441070764L;
	
	/**
	 * 
	 * @param simTime
	 */
	public UnauthorisedExecuteException(Integer simTime, UUID participantID, String name) {
		super("execute function called more than once in one tick. Simulation time: " 
						+ simTime
						+ ", Participant ID: "
						+ participantID
						+ ", name: "
						+ name);
	}
}
