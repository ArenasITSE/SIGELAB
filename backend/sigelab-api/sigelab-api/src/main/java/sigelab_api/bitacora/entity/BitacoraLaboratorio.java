package sigelab_api.bitacora.entity;

import jakarta.persistence.*;
import lombok.*;

import sigelab_api.solicitud.entity.SolicitudLaboratorio;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bitacoras_laboratorio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "solicitud_id")
    private SolicitudLaboratorio solicitud;

    private LocalDate fecha;

    private LocalTime horaEntrada;

    private LocalTime horaSalida;

    @Column(length = 1000)
    private String observaciones;

    @Column(length = 1000)
    private String incidencias;
}