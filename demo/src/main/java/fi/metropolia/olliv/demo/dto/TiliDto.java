package fi.metropolia.olliv.demo.dto;

public class TiliDto {
    private int id;
    private int saldo;
    private HaltijaDto haltija;

    // get & set
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getSaldo() {
        return saldo;
    }
    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }
    public HaltijaDto getHaltija() {
        return haltija;
    }
    public void setHaltija(HaltijaDto haltija) {
        this.haltija = haltija;
    }
}
