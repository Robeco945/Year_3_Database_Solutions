package fi.metropolia.olliv.demo.entity;

import fi.metropolia.olliv.demo.entitylistener.HaltijaListener;
import jakarta.persistence.*;

@Entity
@EntityListeners(HaltijaListener.class)
public class Haltija {

    @Id
    private int id;
    private String etunimi;
    private String sukunimi;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name = "haltija_ryhma",
            joinColumns = @JoinColumn(name = "haltija_id"),
            inverseJoinColumns = @JoinColumn(name = "ryhma_id")
    )
    private java.util.List <Ryhma> ryhmat;

    // getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getEtunimi() {
        return etunimi;
    }
    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }
    public String getSukunimi() {
        return sukunimi;
    }
    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public java.util.List<Ryhma> getRyhmat() {
        return ryhmat;
    }
    public void setRyhmat(java.util.List<Ryhma> ryhmat) {
        this.ryhmat = ryhmat;
    }
}
