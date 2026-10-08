package fi.metropolia.olliv.demo.dto;

import fi.metropolia.olliv.demo.repository.HaltijaRepository;

import java.util.List;

public class HaltijaDto {
    private int id;
    private String etunimi;
    private String sukunimi;

    public HaltijaDto(int id, String etunimi, String sukunimi) {
        this.id = id;
        this.etunimi = etunimi;
        this.sukunimi = sukunimi;
    }

    // default constructor
    public HaltijaDto() {
    }

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

}