package sigelab_api.laboratorio.entity;

import jakarta.persistence.*;
import lombok.*;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.usuario.entity.Usuario;

@Entity
@Table(name = "laboratorios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "encargado_id")
    private Usuario encargado;

    @ManyToOne
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatusLaboratorio estatus;
}