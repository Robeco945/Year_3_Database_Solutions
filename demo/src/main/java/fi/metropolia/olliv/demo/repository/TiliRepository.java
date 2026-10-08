package fi.metropolia.olliv.demo.repository;

import fi.metropolia.olliv.demo.entity.Tili;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TiliRepository extends JpaRepository<Tili, Integer>, TiliRepositoryCustom {

    @Modifying
    @Query("UPDATE Tili t SET t.saldo = t.saldo * :kerroin")
    int kasvataKaikkienSaldoja(@Param("kerroin") double kerroin);

}

