package fi.metropolia.example.webstoreapi.converter;

import fi.metropolia.example.webstoreapi.entity.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps {@link OrderStatus} to the {@code orders.status} varchar column.
 * Fails loudly on unexpected DB values — the earlier lesson: "silent default"
 *
 * <p>Loaded statuses exist only in DB as the exact enum names ('NEW',
 * 'SHIPPED', 'DELIVERED', 'CANCELLED').
 * Mirrors the strictness of the sample repo's {@code KyllaEiBooleanConverter}.</p>
 */
@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

	@Override
	public String convertToDatabaseColumn(OrderStatus attribute) {
		return attribute == null ? null : attribute.name();
	}

	@Override
	public OrderStatus convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			return OrderStatus.valueOf(dbData);
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unknown order status in DB: '" + dbData + "'", ex);
		}
	}

}
