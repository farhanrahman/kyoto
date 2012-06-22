package uk.ac.ic.kyoto.exceptions;

/**
 * 
 * @author Adam, Sam, Stuart, Chris
 */
public class NotEnoughLandException extends Exception {
	private static final long serialVersionUID = -6705918025569748287L;
	
	public NotEnoughLandException() {
		super("Not enough arable land for the investment");
	}
}
