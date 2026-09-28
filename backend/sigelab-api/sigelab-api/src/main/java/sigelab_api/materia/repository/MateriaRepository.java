package sigelab_api.materia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sigelab_api.materia.entity.Materia;

public interface MateriaRepository
        extends JpaRepository<Materia, Long> {
}