package fi.metropolia.olliv.demo.service;

import fi.metropolia.olliv.demo.dto.HaltijaDetailDto;
import fi.metropolia.olliv.demo.dto.HaltijaDto;
import fi.metropolia.olliv.demo.dto.RyhmaDto;
import fi.metropolia.olliv.demo.entity.Haltija;
import fi.metropolia.olliv.demo.entity.Ryhma;
import fi.metropolia.olliv.demo.repository.HaltijaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HaltijaService {

    private final HaltijaRepository haltijaRepository;

    public HaltijaService(HaltijaRepository haltijaRepository) {
        this.haltijaRepository = haltijaRepository;
    }

    public Haltija lisaaHaltija(Haltija haltija) {
        return haltijaRepository.save(haltija);
    }

    public HaltijaDetailDto haeHaltijaDto(int id) {

        Haltija haltija = haltijaRepository.findById(id)
                .orElse(null);
        if (haltija == null) {
            return null;
        }

        // rakenna DTO
        HaltijaDetailDto haltijaDetailDto = new HaltijaDetailDto();
        haltijaDetailDto.setId(haltija.getId());
        haltijaDetailDto.setEtunimi(haltija.getEtunimi());
        haltijaDetailDto.setSukunimi(haltija.getSukunimi());
        haltijaDetailDto.setRyhmat(new ArrayList<>());

        // tee ja lisää RyhmäDTO-oliot
        for (Ryhma ryhma : haltija.getRyhmat()) {
            RyhmaDto ryhmaDto = new RyhmaDto();
            ryhmaDto.setId(ryhma.getId());
            ryhmaDto.setNimi(ryhma.getNimi());
            haltijaDetailDto.getRyhmat().add(ryhmaDto);
        }

        return haltijaDetailDto;
    }

    public List<HaltijaDto> haeHaltijatJoidenNimiAlkaa(String alku) {

        List<Haltija> haltijat = haltijaRepository.findByEtunimiAlkaa(alku);

        List<HaltijaDto> dtot = new ArrayList<>();
        for (Haltija h : haltijat) {
            HaltijaDto dto = new HaltijaDto(h.getId(), h.getEtunimi(), h.getSukunimi());
            dtot.add(dto);
        }
        return dtot;
    }

}