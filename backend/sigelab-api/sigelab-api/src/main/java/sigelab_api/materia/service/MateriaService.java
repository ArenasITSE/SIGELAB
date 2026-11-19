
package sigelab_api.materia.service;

import sigelab_api.materia.dto.MateriaRequest;
import sigelab_api.materia.dto.MateriaResponse;

import java.util.List;

public interface MateriaService {

    MateriaResponse crear(MateriaRequest request);

    List<MateriaResponse> listar();

    MateriaResponse buscarPorId(Long id);

    MateriaResponse actualizar(Long id, MateriaRequest request);

    void eliminar(Long id);

    void activar(Long id);
}
