package fi.metropolia.olliv.demo.repository;

import fi.metropolia.olliv.demo.entity.Tili;

import java.math.BigDecimal;
import java.util.List;

public interface TiliRepositoryCustom {
    List<Tili> findBySaldoIsAtLeast(BigDecimal minimi);
}

