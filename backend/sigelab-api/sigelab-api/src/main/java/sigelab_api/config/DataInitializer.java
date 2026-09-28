package sigelab_api.config;

import lombok.RequiredArgsConstructor;
import sigelab_api.usuario.entity.Rol;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!usuarioRepository.existsByCorreo("admin@sigelab.com")) {

            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .correo("admin@sigelab.com")
                    .password(passwordEncoder.encode("Admin123*"))
                    .rol(Rol.ADMINISTRADOR)
                    .activo(true)
                    .build();

            usuarioRepository.save(admin);
        }
    }
}