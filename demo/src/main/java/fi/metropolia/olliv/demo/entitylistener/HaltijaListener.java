package fi.metropolia.olliv.demo.entitylistener;

import fi.metropolia.olliv.demo.entity.Haltija;
import jakarta.persistence.PostPersist;

public class HaltijaListener {
    @PostPersist
    public void afterPersist(Haltija h) {
        System.out.println("@PostPersist: Haltija tallennettu, id=" + h.getId());
    }
}
