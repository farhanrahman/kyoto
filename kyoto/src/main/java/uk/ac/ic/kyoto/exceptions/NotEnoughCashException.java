package uk.ac.ic.kyoto.exceptions;
/**
 * 
 * @author Adam
 */
public class NotEnoughCashException extends Exception{
	private static final long serialVersionUID = 8538036917630953939L;

	public NotEnoughCashException() {
		super("Investment is greater than available cash to spend");
	}
}
