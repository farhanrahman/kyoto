package uk.ac.ic.kyoto.exceptions;
/**
 * 
 * @author Piotr
 */
public class NotEnoughCarbonOutputException extends Exception{
	private static final long serialVersionUID = -2139598187603648341L;

	public NotEnoughCarbonOutputException() {
		super("Reduction exceeds current carbon output");
	}
}
