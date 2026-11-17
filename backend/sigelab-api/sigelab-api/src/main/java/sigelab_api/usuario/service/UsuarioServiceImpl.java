
package sigelab_api.usuario.service;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import sigelab_api.usuario.dto.UsuarioRequest;
import sigelab_api.usuario.dto.UsuarioResponse;
import sigelab_api.usuario.dto.ResultadoImportacion;
import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.entity.Rol;
import sigelab_api.usuario.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarreraRepository carreraRepository;

    // ==========================================
    // CREAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {

        String correo = request.correo().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException(
                "Ya existe un usuario con ese correo"
            );
        }

        Carrera carrera = obtenerCarrera(request.carreraId());

        if (request.password() == null || request.password().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        Usuario usuario = Usuario.builder()
            .nombre(request.nombre().trim())
            .correo(correo)
            .password(passwordEncoder.encode(request.password()))
            .rol(request.rol())
            .activo(true)
            .carrera(carrera)
            .build();

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // LISTAR USUARIOS
@Transactional(readOnly = true)
@Override
public List<UsuarioResponse> listar() {
    return usuarioRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    // ==========================================
    // BUSCAR POR ID
    // ==========================================

    @Override
    public UsuarioResponse buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Usuario no encontrado")
            );

        return mapToResponse(usuario);
    }

    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Usuario no encontrado")
            );

        String correo = request.correo().trim().toLowerCase(Locale.ROOT);

        if (!usuario.getCorreo().equalsIgnoreCase(correo)
                && usuarioRepository.existsByCorreo(correo)) {

            throw new RuntimeException(
                "El correo ya está registrado por otro usuario"
            );
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRol(request.rol());
        usuario.setCarrera(obtenerCarrera(request.carreraId()));

        if (request.password() != null
                && !request.password().isBlank()) {

            usuario.setPassword(
                passwordEncoder.encode(request.password())
            );
        }

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // ELIMINAR USUARIO
    // ==========================================

    @Override
    public void eliminar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Usuario no encontrado")
            );

        usuarioRepository.delete(usuario);
    }

    // ==========================================
    // ACTIVAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse activar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Usuario no encontrado")
            );

        usuario.setActivo(true);

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // DESACTIVAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse desactivar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Usuario no encontrado")
            );

        usuario.setActivo(false);

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // IMPORTAR USUARIOS DESDE EXCEL
    // ==========================================

    @Override
    public ResultadoImportacion importarExcel(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException(
                "Debes seleccionar un archivo Excel"
            );
        }

        String nombreArchivo = archivo.getOriginalFilename();

        if (nombreArchivo == null ||
                !(nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                || nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".xls"))) {

            throw new RuntimeException(
                "El archivo debe tener formato XLSX o XLS"
            );
        }

        List<ResultadoImportacion.DetalleImportacion> detalles =
            new ArrayList<>();

        int exitosos = 0;
        int rechazados = 0;

        Set<String> correosArchivo = new HashSet<>();

        DataFormatter formatter = new DataFormatter(
            Locale.forLanguageTag("es-MX")
        );

        try (
            InputStream inputStream = archivo.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new RuntimeException(
                    "El archivo no contiene hojas"
                );
            }

            Sheet hoja = workbook.getSheetAt(0);

            if (hoja.getLastRowNum() < 1) {
                throw new RuntimeException(
                    "El Excel no contiene usuarios"
                );
            }

            // Cargar carreras existentes
            Map<String, Carrera> carreras =
                carreraRepository.findAll()
                    .stream()
                    .filter(c -> c.getNombre() != null)
                    .collect(Collectors.toMap(
                        c -> normalizar(c.getNombre()),
                        c -> c,
                        (a, b) -> a
                    ));

            // Recorrer filas después del encabezado
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {

                Row fila = hoja.getRow(i);

                if (fila == null || filaVacia(fila, formatter)) {
                    continue;
                }

                String nombre = leerCelda(fila, 0, formatter);
                String correo = leerCelda(fila, 1, formatter)
                    .toLowerCase(Locale.ROOT);

                String rolTexto = leerCelda(fila, 3, formatter);
                String carreraTexto = leerCelda(fila, 4, formatter);

                try {

                    // Validar nombre completo
                    if (nombre.isBlank()) {
                        throw new IllegalArgumentException(
                            "El nombre completo es obligatorio"
                        );
                    }

                    if (nombre.trim().split("\\s+").length < 2) {
                        throw new IllegalArgumentException(
                            "Debes capturar nombre y apellidos"
                        );
                    }

                    // Validar correo
                    if (!correo.matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                    )) {
                        throw new IllegalArgumentException(
                            "El correo electrónico no es válido"
                        );
                    }

                    // Evitar correos duplicados en el Excel
                    if (!correosArchivo.add(correo)) {
                        throw new IllegalArgumentException(
                            "El correo está duplicado en el Excel"
                        );
                    }

                    // Evitar correos existentes
                    if (usuarioRepository.existsByCorreo(correo)) {
                        throw new IllegalArgumentException(
                            "El correo ya está registrado en SIGELAB"
                        );
                    }

                    // Validar fecha de nacimiento
                    LocalDate fechaNacimiento =
                        interpretarFecha(fila.getCell(2), formatter);

                    if (!fechaNacimiento.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException(
                            "La fecha de nacimiento debe ser anterior a hoy"
                        );
                    }

                    // Validar rol permitido
                    Rol rol;

                    if (rolTexto.equalsIgnoreCase("Docente")) {

                        rol = Rol.DOCENTE;

                    } else if (rolTexto.equalsIgnoreCase("Coordinador")) {

                        rol = Rol.COORDINADOR;

                    } else {

                        throw new IllegalArgumentException(
                            "El rol debe ser Docente o Coordinador"
                        );
                    }

                    // Buscar carrera
                    Carrera carrera = carreras.get(
                        normalizar(carreraTexto)
                    );

                    if (carrera == null) {
                        throw new IllegalArgumentException(
                            "La carrera adscrita no existe en SIGELAB"
                        );
                    }

                    if (Boolean.FALSE.equals(carrera.getActivo())) {
                        throw new IllegalArgumentException(
                            "La carrera está inactiva"
                        );
                    }

                    // Generar contraseña temporal
                    String passwordTemporal =
                        generarPasswordTemporal(
                            nombre,
                            fechaNacimiento
                        );

                    // Crear usuario
                    Usuario usuario = Usuario.builder()
                        .nombre(nombre.trim())
                        .correo(correo)
                        .password(
                            passwordEncoder.encode(passwordTemporal)
                        )
                        .rol(rol)
                        .activo(true)
                        .carrera(carrera)
                        .build();

                    usuarioRepository.saveAndFlush(usuario);

                    exitosos++;

                    detalles.add(
                        new ResultadoImportacion.DetalleImportacion(
                            i + 1,
                            correo,
                            "EXITOSO",
                            "Usuario registrado correctamente"
                        )
                    );

                } catch (Exception e) {

                    rechazados++;

                    detalles.add(
                        new ResultadoImportacion.DetalleImportacion(
                            i + 1,
                            correo,
                            "RECHAZADO",
                            e.getMessage() != null
                                ? e.getMessage()
                                : "Error al registrar el usuario"
                        )
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                "No fue posible procesar el Excel: " + e.getMessage(),
                e
            );
        }

        return new ResultadoImportacion(
            exitosos + rechazados,
            exitosos,
            rechazados,
            detalles
        );
    }

    // ==========================================
    // GENERAR CONTRASEÑA TEMPORAL
    // ==========================================

    private String generarPasswordTemporal(
            String nombre,
            LocalDate fechaNacimiento) {

        String iniciales = Arrays.stream(
                nombre.trim().split("\\s+")
            )
            .filter(p -> !p.isBlank())
            .map(p -> p.substring(0, 1).toUpperCase(Locale.ROOT))
            .collect(Collectors.joining());

        String fecha = fechaNacimiento.format(
            DateTimeFormatter.ofPattern("ddMMyyyy")
        );

        return iniciales + fecha;
    }

    // ==========================================
    // INTERPRETAR FECHA DEL EXCEL
    // ==========================================

    private LocalDate interpretarFecha(
            Cell celda,
            DataFormatter formatter) {

        if (celda == null) {
            throw new IllegalArgumentException(
                "La fecha de nacimiento es obligatoria"
            );
        }

        if (celda.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(celda)) {

            return celda.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        }

        String valor = formatter.formatCellValue(celda).trim();

        List<DateTimeFormatter> formatos = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
        );

        for (DateTimeFormatter formato : formatos) {

            try {
                return LocalDate.parse(valor, formato);

            } catch (DateTimeParseException ignored) {
                // Intentar siguiente formato
            }
        }

        throw new IllegalArgumentException(
            "Fecha inválida. Utiliza DD/MM/AAAA"
        );
    }

    // ==========================================
    // LEER CELDA
    // ==========================================

    private String leerCelda(
            Row fila,
            int columna,
            DataFormatter formatter) {

        Cell celda = fila.getCell(
            columna,
            Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        return celda == null
            ? ""
            : formatter.formatCellValue(celda).trim();
    }

    // ==========================================
    // VALIDAR FILA VACÍA
    // ==========================================

    private boolean filaVacia(
            Row fila,
            DataFormatter formatter) {

        for (int i = 0; i < 5; i++) {

            if (!leerCelda(fila, i, formatter).isBlank()) {
                return false;
            }
        }

        return true;
    }

    // ==========================================
    // NORMALIZAR TEXTO
    // ==========================================

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ");
    }

    // ==========================================
    // OBTENER CARRERA POR ID
    // ==========================================

    private Carrera obtenerCarrera(Long carreraId) {

        if (carreraId == null) {
            return null;
        }

        return carreraRepository.findById(carreraId)
            .orElseThrow(() ->
                new RuntimeException("Carrera no encontrada")
            );
    }

    // ==========================================
    // MAPEAR RESPUESTA
    // ==========================================

    private UsuarioResponse mapToResponse(Usuario usuario) {

        Carrera carrera = usuario.getCarrera();

        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRol().name(),
            usuario.getActivo(),
            carrera != null ? carrera.getId() : null,
            carrera != null ? carrera.getNombre() : null
        );
    }
}
