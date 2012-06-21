package uk.ac.ic.kyoto.exceptions;
/**
 * 
 * @author Adam
 */
public class NotEnoughCashException extends Exception{
	private static final long serialVersionUID = 8538036917630953939L;
	
	private final double availableToSpend;
	private final double investmentRequired;

	public NotEnoughCashException(double availableToSpend, double investmentRequired) {
		super("Investment is greater than available cash to spend");
		this.availableToSpend = availableToSpend;
		this.investmentRequired = investmentRequired;
	}

	public double getAvailableToSpend() {
		return availableToSpend;
	}

	public double getInvestmentRequired() {
		return investmentRequired;
	}
}
