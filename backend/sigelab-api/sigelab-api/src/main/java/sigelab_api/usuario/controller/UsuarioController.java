
package sigelab_api.usuario.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.prepost.PreAuthorize;

import sigelab_api.usuario.dto.UsuarioRequest;
import sigelab_api.usuario.dto.UsuarioResponse;
import sigelab_api.usuario.dto.ResultadoImportacion;
import sigelab_api.usuario.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ==========================================
    // LISTAR USUARIOS
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    // ==========================================
    // BUSCAR POR ID
    // ==========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    // ==========================================
    // CREAR USUARIO
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public UsuarioResponse crear(
            @Valid @RequestBody UsuarioRequest request
    ) {
        return usuarioService.crear(request);
    }

    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        return usuarioService.actualizar(id, request);
    }

    // ==========================================
    // ELIMINAR USUARIO
    // ==========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    // ==========================================
    // ACTIVAR USUARIO
    // ==========================================

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public UsuarioResponse activar(@PathVariable Long id) {
        return usuarioService.activar(id);
    }

    // ==========================================
    // DESACTIVAR USUARIO
    // ==========================================

    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public UsuarioResponse desactivar(@PathVariable Long id) {
        return usuarioService.desactivar(id);
    }

    // ==========================================
    // IMPORTAR EXCEL
    // ==========================================

    @PostMapping(
            value = "/importar-excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResultadoImportacion importarExcel(
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return usuarioService.importarExcel(archivo);
    }
}
