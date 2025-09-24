package ujsu.exceptions;

public class UserSignedUpException extends RuntimeException {
	public UserSignedUpException(String message) {
		super(message);
	}
}