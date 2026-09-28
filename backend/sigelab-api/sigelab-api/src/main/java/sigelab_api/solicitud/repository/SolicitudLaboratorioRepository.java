package sigelab_api.solicitud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SolicitudLaboratorioRepository
        extends JpaRepository<SolicitudLaboratorio, Long> {

    @Query("""
        SELECT COUNT(s) > 0
        FROM SolicitudLaboratorio s
        WHERE s.laboratorio.id = :laboratorioId
        AND s.fecha = :fecha
        AND s.estado = sigelab_api.solicitud.entity.EstadoSolicitud.APROBADA
        AND s.id <> :solicitudId
        AND (
            s.horaInicio < :horaFin
            AND
            s.horaFin > :horaInicio
        )
    """)
    boolean existsConflictoHorario(
            @Param("laboratorioId") Long laboratorioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("solicitudId") Long solicitudId
    );

    @Query("""
        SELECT COUNT(s) > 0
        FROM SolicitudLaboratorio s
        WHERE s.laboratorio.id = :laboratorioId
        AND s.fecha = :fecha
        AND s.estado = sigelab_api.solicitud.entity.EstadoSolicitud.APROBADA
        AND (
            s.horaInicio < :horaFin
            AND
            s.horaFin > :horaInicio
        )
    """)
    boolean existsConflictoHorarioCrear(
            @Param("laboratorioId") Long laboratorioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    List<SolicitudLaboratorio> findByLaboratorioIdAndFechaAndEstado(
            Long laboratorioId,
            LocalDate fecha,
            EstadoSolicitud estado
    );

    @Query("""
        SELECT s
        FROM SolicitudLaboratorio s
        WHERE s.laboratorio.id = :laboratorioId
        AND s.fecha = :fecha
        AND s.estado = sigelab_api.solicitud.entity.EstadoSolicitud.APROBADA
    """)
    List<SolicitudLaboratorio> buscarAprobadas(
            @Param("laboratorioId") Long laboratorioId,
            @Param("fecha") LocalDate fecha
    );

    long countByEstado(
            EstadoSolicitud estado
    );

    List<SolicitudLaboratorio> findByDocenteId(
            Long docenteId
    );

    long countByDocenteId(Long docenteId);

long countByDocenteIdAndEstado(
        Long docenteId,
        EstadoSolicitud estado
);

List<SolicitudLaboratorio> findAll();

List<SolicitudLaboratorio> findByFechaBetween(
        LocalDate fechaInicio,
        LocalDate fechaFin
);
@Query("""
SELECT s.estado, COUNT(s)
FROM SolicitudLaboratorio s
GROUP BY s.estado
""")
List<Object[]> contarPorEstado();

@Query("""
SELECT s.laboratorio.nombre, COUNT(s)
FROM SolicitudLaboratorio s
GROUP BY s.laboratorio.nombre
ORDER BY COUNT(s) DESC
""")
List<Object[]> laboratoriosMasUsados();

@Query("""
SELECT MONTH(s.fecha), COUNT(s)
FROM SolicitudLaboratorio s
GROUP BY MONTH(s.fecha)
ORDER BY MONTH(s.fecha)
""")
List<Object[]> solicitudesPorMes();


}