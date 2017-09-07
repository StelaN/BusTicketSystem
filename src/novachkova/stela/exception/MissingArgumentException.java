package novachkova.stela.exception;

public class MissingArgumentException extends Exception {

	private static final long serialVersionUID = 8086647658085792300L;
	
	public MissingArgumentException(String argument, String command) {
		super(String.format("Missing argument %s after command %s", argument, command));
	}

}
