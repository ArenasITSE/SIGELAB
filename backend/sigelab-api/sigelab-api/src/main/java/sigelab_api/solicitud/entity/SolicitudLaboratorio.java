package sigelab_api.solicitud.entity;

import jakarta.persistence.*;

import lombok.*;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.materia.entity.Materia;
import sigelab_api.laboratorio.entity.Laboratorio;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "solicitudes_laboratorio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "docente_id")
    private Usuario docente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "laboratorio_id")
    private Laboratorio laboratorio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(length = 500)
    private String observaciones;
}