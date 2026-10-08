package fi.metropolia.olliv.demo.controller;

import fi.metropolia.olliv.demo.dto.TiliDto;
import fi.metropolia.olliv.demo.entity.Tili;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fi.metropolia.olliv.demo.service.TiliService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/tili")
public class TiliController {

    private final TiliService tiliService;

    public TiliController(TiliService tiliService) {
        this.tiliService = tiliService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiliDto> getTili(@PathVariable int id) {
        TiliDto dto = tiliService.haeTiliDto(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    public static class KorkoDto {
        public double prosentti;
    }
    @PostMapping("/korko")
    public ResponseEntity<String> maksaKorkoKaikille(@RequestBody KorkoDto dto) {
        int paivitetyt = tiliService.maksaKorkoKaikille(dto.prosentti);
        return ResponseEntity.ok("Korko maksettu " + paivitetyt + " tilille (" + dto.prosentti + "%)");
    }

    @GetMapping("/saldoainakin/{raja}")
    public ResponseEntity<List<TiliDto>> getTilitJoillaSaldoAinakin(@PathVariable double raja) {
        List<TiliDto> tilit = tiliService.haeTilitDtoVahintaan(BigDecimal.valueOf(raja));
        return ResponseEntity.ok(tilit);
    }
}
