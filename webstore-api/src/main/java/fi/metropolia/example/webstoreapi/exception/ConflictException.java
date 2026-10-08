package fi.metropolia.example.webstoreapi.exception;

/**
 * 409 — the request conflicts with the current state or with database
 * constraints (e.g. deleting a referenced row, duplicate resource).
 */
public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}

}
