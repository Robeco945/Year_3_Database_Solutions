package fi.metropolia.olliv.demo.service;

import fi.metropolia.olliv.demo.dto.HaltijaDto;
import fi.metropolia.olliv.demo.entity.Tili;
import fi.metropolia.olliv.demo.entity.Haltija;
import fi.metropolia.olliv.demo.dto.TiliDto;
import fi.metropolia.olliv.demo.repository.TiliRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TiliService {
    private final TiliRepository tiliRepository;

    public TiliService(TiliRepository tiliRepository) {
        this.tiliRepository = tiliRepository;
    }

    // Yksi tilin ja haltijan DTO:n haku
    public TiliDto haeTiliDto(int tiliId) {
        Tili tili = tiliRepository.findById(tiliId)
                .orElse(null);
        if (tili == null) {
            return null; // tai heitä poikkeus jos haluat not found -logiikan
        }
        Haltija haltija = tili.getHaltija(); // EAGER, ei lazy-load ongelmaa!
        HaltijaDto haltijaDto = null;
        if (haltija != null) {
            haltijaDto = new HaltijaDto();
            haltijaDto.setId(haltija.getId());
            haltijaDto.setEtunimi(haltija.getEtunimi());
            haltijaDto.setSukunimi(haltija.getSukunimi());
        }

        TiliDto tiliDto = new TiliDto();
        tiliDto.setId(tili.getId());
        tiliDto.setSaldo(tili.getSaldo());
        tiliDto.setHaltija(haltijaDto);

        if (tili.isLukittu()) {
            System.out.println("Huomio: Tili " + tili.getId() + " on lukittu!");
        }

        return tiliDto;
    }

    @Transactional
    public int maksaKorkoKaikille(double prosentti) {
        double kerroin = 1.0 + (prosentti / 100.0);
        return tiliRepository.kasvataKaikkienSaldoja(kerroin);
    }

    public List<Tili> haeTilitVahintaan(BigDecimal minimisumma) {
        return tiliRepository.findBySaldoIsAtLeast(minimisumma);
    }

    public List<TiliDto> haeTilitDtoVahintaan(BigDecimal minimi) {
        List<Tili> tilit = tiliRepository.findBySaldoIsAtLeast(minimi);
        List<TiliDto> tulos = new ArrayList<>();

        for (Tili tili : tilit) {
            tulos.add(muutaDto(tili));
        }

        return tulos;
    }

    private TiliDto muutaDto(Tili tili) {
        TiliDto dto = new TiliDto();
        dto.setId(tili.getId());
        dto.setSaldo(tili.getSaldo());
        // haltija saa jäädä nulliksi
        return dto;
    }


}