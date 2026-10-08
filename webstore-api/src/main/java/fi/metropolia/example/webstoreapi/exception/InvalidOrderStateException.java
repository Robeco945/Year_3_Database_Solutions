package fi.metropolia.example.webstoreapi.exception;

/**
 * 409 — an order lifecycle rule was violated (e.g. editing a non-NEW order,
 * an illegal status transition, deleting a non-cancelled order).
 */
public class InvalidOrderStateException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public InvalidOrderStateException(String message) {
		super(message);
	}

}
