/**
 * 
 */
package uk.ac.ic.kyoto.countries;

/**
 * @author farhanrahman
 *
 */
public class AlreadyInitialisedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5412492657669709083L;

	/**
	 * 
	 */
	public AlreadyInitialisedException() {
		super("AbstractParticipant already initialised");
	}
}
