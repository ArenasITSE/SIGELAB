package sigelab_api.bitacora.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sigelab_api.bitacora.dto.BitacoraRequest;
import sigelab_api.bitacora.dto.BitacoraResponse;
import sigelab_api.bitacora.entity.BitacoraLaboratorio;
import sigelab_api.bitacora.repository.BitacoraLaboratorioRepository;

import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BitacoraServiceImpl implements BitacoraService {

    private final BitacoraLaboratorioRepository bitacoraRepository;
    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public BitacoraResponse crear(
            BitacoraRequest request
    ) {

        SolicitudLaboratorio solicitud =
                solicitudRepository.findById(
                        request.solicitudId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Solicitud no encontrada"
                        ));

        BitacoraLaboratorio bitacora =
                BitacoraLaboratorio.builder()
                        .solicitud(solicitud)
                        .fecha(solicitud.getFecha())
                        .horaEntrada(request.horaEntrada())
                        .horaSalida(request.horaSalida())
                        .observaciones(request.observaciones())
                        .incidencias(request.incidencias())
                        .build();

        bitacora = bitacoraRepository.save(bitacora);

        return mapToResponse(bitacora);
    }

    @Override
    public List<BitacoraResponse> listar() {

        return bitacoraRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BitacoraResponse buscarPorId(
            Long id
    ) {

        BitacoraLaboratorio bitacora =
                bitacoraRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bitácora no encontrada"
                                ));

        return mapToResponse(bitacora);
    }

    @Override
    public void eliminar(
            Long id
    ) {

        BitacoraLaboratorio bitacora =
                bitacoraRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bitácora no encontrada"
                                ));

        bitacoraRepository.delete(bitacora);
    }

    private BitacoraResponse mapToResponse(
            BitacoraLaboratorio bitacora
    ) {

        return new BitacoraResponse(
                bitacora.getId(),

                bitacora.getSolicitud().getId(),

                bitacora.getSolicitud()
                        .getDocente()
                        .getNombre(),

                bitacora.getSolicitud()
                        .getMateria()
                        .getNombre(),

                bitacora.getSolicitud()
                        .getLaboratorio()
                        .getNombre(),

                bitacora.getFecha(),

                bitacora.getHoraEntrada(),

                bitacora.getHoraSalida(),

                bitacora.getObservaciones(),

                bitacora.getIncidencias()
        );
    }
}