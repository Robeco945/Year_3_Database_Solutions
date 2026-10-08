package fi.metropolia.olliv.demo.repository;

import fi.metropolia.olliv.demo.entity.Tili;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class TiliRepositoryCustomImpl implements TiliRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Tili> findBySaldoIsAtLeast(BigDecimal minimi) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tili> cq = cb.createQuery(Tili.class);
        Root<Tili> tili = cq.from(Tili.class);
        cq.select(tili).where(cb.greaterThanOrEqualTo(tili.<BigDecimal>get("saldo"), minimi));
        return entityManager.createQuery(cq).getResultList();
    }
}