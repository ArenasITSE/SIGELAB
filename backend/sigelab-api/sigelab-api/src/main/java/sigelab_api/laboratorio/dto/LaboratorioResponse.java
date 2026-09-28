package sigelab_api.laboratorio.dto;

public record LaboratorioResponse(

        Long id,

        String nombre,

        Long encargadoId,

        String encargado,

        Long carreraId,

        String carrera,

        String ubicacion,

        Integer capacidad,

        String estatus

) {
}