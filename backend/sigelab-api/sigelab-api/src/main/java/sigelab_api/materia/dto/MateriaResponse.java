package sigelab_api.materia.dto;

public record MateriaResponse(

        Long id,
        String clave,
        String nombre,
        Integer semestre,
        Integer horasSemana,
        Long carreraId,
        String carrera,
        Boolean activo

) {
}