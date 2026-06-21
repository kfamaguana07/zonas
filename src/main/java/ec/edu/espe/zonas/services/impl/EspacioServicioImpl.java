package ec.edu.espe.zonas.services.impl;

import ec.edu.espe.zonas.dtos.EspacioRequestDto;
import ec.edu.espe.zonas.dtos.EspacioResponseDto;
import ec.edu.espe.zonas.entidades.Espacio;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.entidades.Zona;
import ec.edu.espe.zonas.repositorios.EspacioRepositorio;
import ec.edu.espe.zonas.repositorios.ZonaRepositorio;
import ec.edu.espe.zonas.services.EspacioServicio;
import ec.edu.espe.zonas.utils.UtilsMappers;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EspacioServicioImpl implements EspacioServicio{

    private final UtilsMappers mappers;
    private final EspacioRepositorio repositorioEspacio;
    private final ZonaRepositorio zonaRepositorio;


    @Override
    public List<EspacioResponseDto> obtenerEspacios() {
        return repositorioEspacio.findAll().stream()
                .map(mappers::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EspacioResponseDto crearEspacio(EspacioRequestDto dto) {
 
        // 1. Obtener la Zona
        Zona objZona = zonaRepositorio.findById(dto.getIdZona())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Zona no encontrada con id: " + dto.getIdZona()));
 
        // 2. Contar espacios actuales de la Zona
        long espaciosActuales = repositorioEspacio.countByZona_Id(dto.getIdZona());
 
        // 3. Validar capacidad (SRP: la regla vive en el servicio, no en el controlador)
        if (espaciosActuales >= objZona.getCapacidad()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format(
                            "La zona '%s' ha alcanzado su capacidad máxima de %d espacio(s). " +
                                    "Espacios actuales: %d.",
                            objZona.getNombre(),
                            objZona.getCapacidad(),
                            espaciosActuales));
        }
 
        // 4. Construir y persistir el Espacio
        Espacio nuevoEspacio = mappers.toEntity(dto);
        if (nuevoEspacio.getCodigo() == null || nuevoEspacio.getCodigo().isBlank()) {
            String fechaStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
            nuevoEspacio.setCodigo(String.format("TICK-%s-%d-%s",
                    objZona.getCodigo(), espaciosActuales + 1, fechaStr));
        }
        nuevoEspacio.setZona(objZona);
        nuevoEspacio.setEstado(EstadoEspacio.DISPONIBLE);
        nuevoEspacio.setActivo(true);
        nuevoEspacio.setFechaCreacion(LocalDateTime.now());
 
        Espacio espacioGuardado = repositorioEspacio.save(nuevoEspacio);
        return mappers.toResponseDto(espacioGuardado);
    }


    @Override
    @Transactional
    public EspacioResponseDto actualizarEspacio(UUID idEspacio, EspacioRequestDto dto) {
        Espacio espacio = repositorioEspacio.findById(idEspacio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Espacio no encontrado con id: " + idEspacio));
 
        if (dto.getDescripcion() != null) espacio.setDescripcion(dto.getDescripcion());
        if (dto.getTipo() != null)        espacio.setTipo(dto.getTipo());
        espacio.setFechaModificacion(LocalDateTime.now());
 
        return mappers.toResponseDto(repositorioEspacio.save(espacio));
    }


    @Override
    @Transactional
    public void eliminarEspacio(UUID idEspacio) {
        Espacio espacio = repositorioEspacio.findById(idEspacio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Espacio no encontrado con id: " + idEspacio));
 
        if (!espacio.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El espacio ya se encuentra inactivo.");
        }
 
        espacio.setActivo(false);
        espacio.setEstado(EstadoEspacio.INACTIVO);
        espacio.setFechaModificacion(LocalDateTime.now());
        repositorioEspacio.save(espacio);
    }


    @Override
    @Transactional
    public EspacioResponseDto cambiarEstado(UUID idEspacio, EstadoEspacio estado) {
        Espacio espacio = repositorioEspacio.findById(idEspacio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Espacio no encontrado con id: " + idEspacio));
 
        espacio.setEstado(estado);
        espacio.setFechaModificacion(LocalDateTime.now());
        return mappers.toResponseDto(repositorioEspacio.save(espacio));
    }
    

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDto> obtenerEspaciosPorEstado(EstadoEspacio estado) {
        return repositorioEspacio.findByEstado(estado).stream()
                .map(mappers::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioResponseDto> obtenerEspaciosPorZonaEstado(UUID idZona, EstadoEspacio estado) {
        return repositorioEspacio.findByZonaAndEstado(idZona, estado).stream()
                .map(mappers::toResponseDto)
                .toList();
    }
    
}
