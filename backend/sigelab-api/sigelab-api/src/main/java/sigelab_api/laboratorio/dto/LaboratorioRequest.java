package sigelab_api.laboratorio.dto;

import jakarta.validation.constraints.*;

public record LaboratorioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotNull(message = "El encargado es obligatorio")
    Long encargadoId,

    @NotNull(message = "La carrera es obligatoria")
    Long carreraId,

    @NotBlank(message = "La ubicación es obligatoria")
    String ubicacion,

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que cero")
    Integer capacidad,

    String estatus

) {}