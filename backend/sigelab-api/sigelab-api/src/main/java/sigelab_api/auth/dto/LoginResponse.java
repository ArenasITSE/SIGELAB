package sigelab_api.auth.dto;

public record LoginResponse(

        String token,
        String rol,
        String nombre

) {}