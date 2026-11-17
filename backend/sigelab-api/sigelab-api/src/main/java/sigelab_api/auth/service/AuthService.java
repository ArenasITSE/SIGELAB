package sigelab_api.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import sigelab_api.auth.dto.LoginRequest;
import sigelab_api.auth.dto.LoginResponse;
import sigelab_api.security.jwt.JwtService;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.correo(),
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(request.correo())
                        .orElseThrow(() ->
                                new RuntimeException("Usuario no encontrado")
                        );

                        

        return new LoginResponse(
                token,
                usuario.getRol().name(),
                usuario.getNombre()
        );
    }
}