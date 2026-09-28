package sigelab_api.materia.entity;

import jakarta.persistence.*;
import lombok.*;

import sigelab_api.carrera.entity.Carrera;

@Entity
@Table(name = "materias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer semestre;

    @Column(nullable = false)
    private Integer horasSemana;

    @Column(nullable = false)
    private Boolean activo;

    @ManyToOne
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;
}