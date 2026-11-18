
package sigelab_api.auth.controller;

import lombok.RequiredArgsConstructor;

import sigelab_api.auth.dto.LoginRequest;
import sigelab_api.auth.dto.LoginResponse;
import sigelab_api.auth.dto.UsuarioSesionResponse;
import sigelab_api.auth.service.AuthService;
import sigelab_api.usuario.service.UsuarioAutenticado;
import sigelab_api.usuario.entity.Usuario;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioAutenticado usuarioAutenticado;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    /**
     * Obtener información del usuario autenticado.
     */
    @GetMapping("/me")
    public UsuarioSesionResponse obtenerSesion() {

        Usuario usuario = usuarioAutenticado.obtenerUsuario();

        return new UsuarioSesionResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.getActivo(),
                usuario.getCarrera() != null
                        ? usuario.getCarrera().getId()
                        : null,
                usuario.getCarrera() != null
                        ? usuario.getCarrera().getNombre()
                        : null
        );
    }
}
