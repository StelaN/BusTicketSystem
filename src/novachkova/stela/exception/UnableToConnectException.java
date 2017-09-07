package novachkova.stela.exception;

public class UnableToConnectException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7183377800102544039L;
	
	public UnableToConnectException(String message) {
		super(String.format("Unable to connect to the server. %s", message));
	}

}
