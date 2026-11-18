
package sigelab_api.solicitud.service;

import sigelab_api.solicitud.dto.SolicitudRequest;
import sigelab_api.solicitud.dto.SolicitudResponse;

import java.util.List;

public interface SolicitudService {

    SolicitudResponse crear(SolicitudRequest request);

    List<SolicitudResponse> listar();

    SolicitudResponse buscarPorId(Long id);

    SolicitudResponse aprobar(
            Long id,
            String observaciones
    );

    SolicitudResponse rechazar(
            Long id,
            String observaciones
    );

    void eliminar(Long id);

    List<SolicitudResponse> listarMisSolicitudes();
}
