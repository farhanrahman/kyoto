package uk.ac.ic.kyoto.exceptions;

public class CannotJoinKyotoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7825515238566537129L;

	public CannotJoinKyotoException() {
	}

	public CannotJoinKyotoException(String message) {
		super(message);
	}

	public CannotJoinKyotoException(Throwable cause) {
		super(cause);
	}

	public CannotJoinKyotoException(String message, Throwable cause) {
		super(message, cause);
	}

}
