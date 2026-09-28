package sigelab_api.laboratorio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sigelab_api.laboratorio.entity.Laboratorio;

public interface LaboratorioRepository
        extends JpaRepository<Laboratorio, Long> {

}