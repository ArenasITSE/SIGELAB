
package sigelab_api.usuario.service;

import org.springframework.web.multipart.MultipartFile;

import sigelab_api.usuario.dto.UsuarioRequest;
import sigelab_api.usuario.dto.UsuarioResponse;
import sigelab_api.usuario.dto.ResultadoImportacion;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse buscarPorId(Long id);

    List<UsuarioResponse> listar();

    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    void eliminar(Long id);

    UsuarioResponse activar(Long id);

    UsuarioResponse desactivar(Long id);

    ResultadoImportacion importarExcel(MultipartFile archivo);
}
