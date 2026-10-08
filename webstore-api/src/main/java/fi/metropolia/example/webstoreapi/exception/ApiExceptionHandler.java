package fi.metropolia.example.webstoreapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps service exceptions to RFC 7807 ProblemDetail responses:
 * 404 not found, 409 conflicts (state, stock, optimistic lock, FK), 400 bad request.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Insufficient stock", ex.getMessage());
		problem.setProperty("productId", ex.getProductId());
		problem.setProperty("available", ex.getAvailable());
		problem.setProperty("requested", ex.getRequested());
		return problem;
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflict(ConflictException ex) {
		return problem(HttpStatus.CONFLICT, "Conflicting request", ex.getMessage());
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ProblemDetail handleOptimisticLocking(OptimisticLockingFailureException ex) {
		return problem(HttpStatus.CONFLICT, "Concurrent modification",
				"The resource was modified concurrently; reload it and retry");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "Database constraint violation",
				"The operation violates a database constraint (e.g. a referenced row)");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}

}
