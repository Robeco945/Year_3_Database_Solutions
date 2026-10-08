package fi.metropolia.olliv.demo.entity;

import fi.metropolia.olliv.demo.converter.KyllaEiBooleanConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="tili")
public class Tili {

    @Id
    @Column(name="id")
    private int id;
    @Column(name="saldo")
    private BigDecimal saldo;
    @ManyToOne
    @JoinColumn(name="haltija_id")
    private Haltija haltija;
    @Column(name = "lukittu", nullable = false, length = 1)
    @Convert(converter = KyllaEiBooleanConverter.class)
    private boolean lukittu;

    // getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {this.id = id;}
    public BigDecimal getSaldo() {
        return saldo;
    }
    public void setSaldo(BigDecimal saldo) {this.saldo = saldo;}
    public Haltija getHaltija() {
        return haltija;
    }
    public void setHaltija(Haltija haltija) {
        this.haltija = haltija;
    }
    public boolean isLukittu() {
        return lukittu;
    }
    public void setLukittu(boolean lukittu) {
        this.lukittu = lukittu;
    }
}
