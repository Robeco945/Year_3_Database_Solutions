package fi.metropolia.olliv.demo.controller;

import fi.metropolia.olliv.demo.dto.HaltijaDetailDto;
import fi.metropolia.olliv.demo.dto.HaltijaDto;
import fi.metropolia.olliv.demo.entity.Haltija;
import fi.metropolia.olliv.demo.service.HaltijaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/haltija")
public class HaltijaController {

    private final HaltijaService haltijaService;

    public HaltijaController(final HaltijaService haltijaService) {
        this.haltijaService = haltijaService;
    }

    @PostMapping()
    public ResponseEntity<Haltija> lisaaHaltija(@RequestBody Haltija haltija) {
        Haltija savedHaltija = haltijaService.lisaaHaltija(haltija);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHaltija);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HaltijaDetailDto> getHaltija(@PathVariable int id) {
        HaltijaDetailDto haltijaDetailDto = haltijaService.haeHaltijaDto(id);
        if (haltijaDetailDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(haltijaDetailDto);
    }

    @GetMapping("/etunimi/{alku}")
    public ResponseEntity<List<HaltijaDto>> getHaltijatEtunimiAlkaa(@PathVariable String alku) {
        List<HaltijaDto> haltijat = haltijaService.haeHaltijatJoidenNimiAlkaa(alku);
        return ResponseEntity.ok(haltijat);
    }

}
