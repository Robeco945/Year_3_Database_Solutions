package fi.metropolia.example.webstoreapi.exception;

/**
 * 404 — a requested resource does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
