package fi.metropolia.example.webstoreapi.exception;

/**
 * 409 — checkout/cart edit would drive a product's stock negative. Carries the
 * product id and the available/requested amounts into the ProblemDetail.
 */
public class InsufficientStockException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Integer productId;
	private final int available;
	private final int requested;

	public InsufficientStockException(Integer productId, int available, int requested) {
		super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available);
		this.productId = productId;
		this.available = available;
		this.requested = requested;
	}

	public Integer getProductId() {
		return productId;
	}

	public int getAvailable() {
		return available;
	}

	public int getRequested() {
		return requested;
	}

}
