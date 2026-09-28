package sigelab_api.bitacora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sigelab_api.bitacora.entity.BitacoraLaboratorio;

import java.util.List;

public interface BitacoraLaboratorioRepository
        extends JpaRepository<BitacoraLaboratorio, Long> {

    List<BitacoraLaboratorio> findBySolicitudDocenteId(
            Long docenteId
    );

}