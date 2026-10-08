package fi.metropolia.example.webstoreapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Contact;

/**
 * Spring Data repository for {@link Contact} (plan §2: customer contact CRUD).
 * The generated data contains duplicate emails, so lookups pick the lowest id
 * deterministically instead of failing on a non-unique result.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

	Optional<Contact> findFirstByEmailIgnoreCaseOrderByIdAsc(String email);

	Optional<Contact> findByReference(String reference);

}
