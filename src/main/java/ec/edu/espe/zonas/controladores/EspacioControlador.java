package ec.edu.espe.zonas.controladores;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.zonas.dtos.EspacioRequestDto;
import ec.edu.espe.zonas.dtos.EspacioResponseDto;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.services.EspacioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/espacios")
@RequiredArgsConstructor

public class EspacioControlador {

    private final EspacioServicio espacioServicio; 

    @GetMapping
    public ResponseEntity<List<EspacioResponseDto>> listarEspacios() {
        return ResponseEntity.ok(espacioServicio.obtenerEspacios());
    }


    @PostMapping
    public ResponseEntity<EspacioResponseDto> crearEspacio(
            @Valid @RequestBody EspacioRequestDto request) {
        return new ResponseEntity<>(espacioServicio.crearEspacio(request), HttpStatus.CREATED);
    }


    @PutMapping("/{idEspacio}")
    public ResponseEntity<EspacioResponseDto> actualizarEspacio(
            @PathVariable UUID idEspacio,
            @Valid @RequestBody EspacioRequestDto request) {
        return ResponseEntity.ok(espacioServicio.actualizarEspacio(idEspacio, request));
    }


    // Desactivación lógica (no borrado físico).
    @DeleteMapping("/{idEspacio}")
    public ResponseEntity<Void> eliminarEspacio(@PathVariable UUID idEspacio) {
        espacioServicio.eliminarEspacio(idEspacio);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{idEspacio}/estado")
    public ResponseEntity<EspacioResponseDto> cambiarEstado(
            @PathVariable UUID idEspacio,
            @RequestParam EstadoEspacio estado) {
        return ResponseEntity.ok(espacioServicio.cambiarEstado(idEspacio, estado));
    }


    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EspacioResponseDto>> obtenerPorEstado(
            @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(espacioServicio.obtenerEspaciosPorEstado(estado));
    }


    @GetMapping("/zona/{idZona}/estado/{estado}")
    public ResponseEntity<List<EspacioResponseDto>> obtenerPorZonaYEstado(
            @PathVariable UUID idZona,
            @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(espacioServicio.obtenerEspaciosPorZonaEstado(idZona, estado));
    }


}
