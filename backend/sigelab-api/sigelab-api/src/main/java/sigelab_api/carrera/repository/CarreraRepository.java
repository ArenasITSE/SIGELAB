package sigelab_api.carrera.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import sigelab_api.carrera.entity.Carrera;

import java.util.Optional;

public interface CarreraRepository
        extends JpaRepository<Carrera, Long> {

    Optional<Carrera> findByClave(String clave);

    boolean existsByClave(String clave);

    boolean existsByNombre(String nombre);
}