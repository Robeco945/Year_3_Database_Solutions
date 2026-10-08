package fi.metropolia.example.webstoreapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Digital product: a {@link Product} subclass (no physical shipping; delivered
 * via download link). Holds subclass-specific attributes in its own joined
 * table ({@code digitalproducts}).
 */
@Entity
@Table(name = "digitalproducts")
@PrimaryKeyJoinColumn(name = "product_id", referencedColumnName = "id")
@DiscriminatorValue("DIG")
public class DigitalProduct extends Product {

	@Column(name = "download_url", length = 255)
	private String downloadUrl;

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

}
