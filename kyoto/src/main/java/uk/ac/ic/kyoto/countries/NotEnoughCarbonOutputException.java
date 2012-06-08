package uk.ac.ic.kyoto.countries;
/**
 * 
 * @author Piotr
 */
public class NotEnoughCarbonOutputException extends RuntimeException{
	private static final long serialVersionUID = -2139598187603648341L;

	public NotEnoughCarbonOutputException() {
		super("Reduction exceeds current carbon output");
	}
}
