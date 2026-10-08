package fi.metropolia.olliv.demo.repository;

import fi.metropolia.olliv.demo.entity.Haltija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HaltijaRepository extends JpaRepository<Haltija, Integer> {

    @Query("SELECT h FROM Haltija h WHERE h.etunimi LIKE :alku%")
    List<Haltija> findByEtunimiAlkaa(@Param("alku") String alku);

}
