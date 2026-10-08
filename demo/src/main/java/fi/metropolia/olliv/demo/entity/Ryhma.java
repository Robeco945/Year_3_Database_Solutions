package fi.metropolia.olliv.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ryhma")
public class Ryhma {
    @Id
    private int id;
    private String nimi;

    public Ryhma() {
    }

    public Ryhma(int id, String nimi) {
        this.id = id;
        this.nimi = nimi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }
}
