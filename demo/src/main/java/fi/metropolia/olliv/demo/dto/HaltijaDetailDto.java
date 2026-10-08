package fi.metropolia.olliv.demo.dto;

import java.util.List;

public class HaltijaDetailDto {
    private int id;
    private String etunimi;
    private String sukunimi;
    private List<RyhmaDto> ryhmat;

    // get & set
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

    public List<RyhmaDto> getRyhmat() {
        return ryhmat;
    }
    public void setRyhmat(List<RyhmaDto> ryhmät) {
        this.ryhmat = ryhmät;
    }
}