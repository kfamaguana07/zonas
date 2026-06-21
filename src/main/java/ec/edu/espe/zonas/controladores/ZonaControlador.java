package ec.edu.espe.zonas.controladores;

import ec.edu.espe.zonas.dtos.ZonaRequestDto;
import ec.edu.espe.zonas.dtos.ZonaResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.zonas.services.ZonaServicio;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor

public class ZonaControlador {

    private final ZonaServicio zonaServicio;

    @GetMapping
    public ResponseEntity<List<ZonaResponseDto>> listarZonas(){
        return ResponseEntity.ok(zonaServicio.listarZonas());
    }


    @PostMapping                   // ← faltaba en el original
    public ResponseEntity<ZonaResponseDto> crearZona(
            @Valid @RequestBody ZonaRequestDto request) {
        return new ResponseEntity<>(zonaServicio.crearZona(request), HttpStatus.CREATED);
    }


    @PutMapping("/{idZona}")
    public ResponseEntity<ZonaResponseDto> actualizarZona(
            @PathVariable UUID idZona,
            @Valid @RequestBody ZonaRequestDto request) {   // ← faltaba @RequestBody
        return ResponseEntity.ok(zonaServicio.actualizarZona(idZona, request));
    }
    

    // Activa o desactiva la zona según su estado actual.
    @PatchMapping("/{idZona}/estado")
    public ResponseEntity<Void> activarDesactivar(@PathVariable UUID idZona) {
        zonaServicio.activarDesactivar(idZona);
        return ResponseEntity.noContent().build();
    }
    

}
