package ec.edu.espe.zonas.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ec.edu.espe.zonas.dtos.ZonaRequestDto;
import ec.edu.espe.zonas.dtos.ZonaResponseDto;
import ec.edu.espe.zonas.entidades.Zona;
import ec.edu.espe.zonas.repositorios.EspacioRepositorio;
import ec.edu.espe.zonas.repositorios.ZonaRepositorio;
import ec.edu.espe.zonas.services.ZonaServicio;

@Service
public class ZonaServicioImpl implements ZonaServicio {

    private final ZonaRepositorio repositorioZona;
    private final EspacioRepositorio repositorioEspacio;   //validar espacios activos

    
    //constructor
    public ZonaServicioImpl(ZonaRepositorio repositorioZona,
                            EspacioRepositorio repositorioEspacio) {
        this.repositorioZona     = repositorioZona;
        this.repositorioEspacio  = repositorioEspacio;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ZonaResponseDto> listarZonas() {
        return repositorioZona.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ZonaResponseDto crearZona(ZonaRequestDto request){

        if(repositorioZona.existsByNombre(request.getNombre())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"YA EXISTE EL NOMBRE" + request.getNombre());
        }
        Zona objZona = new Zona();
        objZona.setNombre(request.getNombre());
        objZona.setCodigo(generarCodigo(request));
        objZona.setDescripcion(request.getDescripcion());
        objZona.setTipo(request.getTipo());
        objZona.setCapacidad(request.getCapacidad());
        objZona.setEstado(1);
        objZona.setFechaCreacion(LocalDateTime.now());

        repositorioZona.save(objZona);

        return toResponse(objZona);
    }

    
    @Override
    @Transactional
    public ZonaResponseDto actualizarZona(UUID idZona, ZonaRequestDto request) {
        Zona zona = repositorioZona.findById(idZona)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Zona no encontrada con id: " + idZona));
 
        zona.setNombre(request.getNombre());
        zona.setDescripcion(request.getDescripcion());
        zona.setCapacidad(request.getCapacidad());
        zona.setFechaModificacion(LocalDateTime.now());
 
        return toResponse(repositorioZona.save(zona));
    }
    

    @Override
    @Transactional
    public void activarDesactivar(UUID idZona) {
 
        // 1. Buscar la zona
        Zona zona = repositorioZona.findById(idZona)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Zona no encontrada con id: " + idZona));
 
        if (zona.getEstado() == 1) {
            // 2. Zona ACTIVA → verificar espacios activos antes de desactivar
            boolean tieneEspaciosActivos = repositorioEspacio.existsEspaciosActivosPorZona(idZona);
 
            if (tieneEspaciosActivos) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede desactivar la zona '" + zona.getNombre() +
                                "' porque tiene espacios activos asociados. " +
                                "Desactive los espacios primero.");
            }
 
            zona.setEstado(0);   // Desactivar
 
        } else {
            // 3. Zona INACTIVA → activar directamente
            zona.setEstado(1);   // Activar
        }
 
        // 4. Persistir cambios
        zona.setFechaModificacion(LocalDateTime.now());
        repositorioZona.save(zona);
    }


    // Mapper interno
    private ZonaResponseDto toResponse(Zona objZona){
        return ZonaResponseDto.builder()
                .idZona(objZona.getId())
                .nombre(objZona.getNombre())
                .codigo(objZona.getCodigo())
                .descripcion(objZona.getDescripcion())
                .tipo(objZona.getTipo())
                .capacidad(objZona.getCapacidad())
                .estado(objZona.getEstado())
                .espacios(objZona.getEspacios())
                .fechaCreacion(objZona.getFechaCreacion())
                .fechaModificacion(objZona.getFechaModificacion())
                .build();
    }

    
    // Genera un codigo con formato: ZON-TIPO-NN  (ej: ZON-REG-01)
    private String generarCodigo(ZonaRequestDto request) {

        String prefijo = "ZON";
        String tipoAbrev = request.getTipo().name().substring(0, 3).toUpperCase();
        long siguiente = repositorioZona.count() + 1;
        String numero = String.format("%02d", siguiente);
        return prefijo + "-" + tipoAbrev + "-" + numero;
    }

}