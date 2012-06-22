/**
 * 
 */
package uk.ac.ic.kyoto.exceptions;

/**
 * @author farhanrahman
 *
 */
public class NoCountryDataException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6315367735956532792L;

	/**
	 * 
	 */
	public NoCountryDataException() {
		super("No country data specified. Pleae load up the data from the web UI");
	}

}
