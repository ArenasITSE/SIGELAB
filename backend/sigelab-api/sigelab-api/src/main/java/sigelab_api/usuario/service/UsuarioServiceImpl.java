
package sigelab_api.usuario.service;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import sigelab_api.usuario.dto.UsuarioRequest;
import sigelab_api.usuario.dto.UsuarioResponse;
import sigelab_api.usuario.dto.ResultadoImportacion;

import sigelab_api.usuario.entity.Usuario;
import sigelab_api.usuario.entity.Rol;
import sigelab_api.usuario.repository.UsuarioRepository;

import sigelab_api.carrera.entity.Carrera;
import sigelab_api.carrera.repository.CarreraRepository;

import sigelab_api.usuario.service.UsuarioAutenticado;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import sigelab_api.usuario.dto.CarreraUsuarioResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarreraRepository carreraRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    // ==========================================
    // CREAR USUARIO
    // ==========================================
        @Override
public UsuarioResponse crear(UsuarioRequest request) {

    Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

    boolean esAdministrador =
            usuarioActual.getRol() == Rol.ADMINISTRADOR;

    String correo = request.correo()
            .trim()
            .toLowerCase(Locale.ROOT);

    if (usuarioRepository.existsByCorreo(correo)) {
        throw new IllegalArgumentException(
                "Ya existe un usuario con ese correo."
        );
    }

    // El coordinador únicamente puede registrar docentes.
    if (!esAdministrador && request.rol() != Rol.DOCENTE) {
        throw new AccessDeniedException(
                "El coordinador solamente puede registrar docentes."
        );
    }

    List<Carrera> carrerasAsignadas;

    if (request.rol() == Rol.DOCENTE) {

        if (esAdministrador) {
            // El administrador puede asignar varias carreras.
            carrerasAsignadas = obtenerCarrerasDocente(
                    request.carreraIds()
            );
        } else {
            // El coordinador solamente asigna su propia carrera.
            carrerasAsignadas = List.of(
                    obtenerCarreraDelCoordinador(usuarioActual)
            );
        }

    } else {

        // Para otros roles se conserva la asignación individual.
        Carrera carrera = obtenerCarrera(request.carreraId());

        carrerasAsignadas = carrera != null
                ? List.of(carrera)
                : new ArrayList<>();
    }

    if (request.password() == null
            || request.password().isBlank()) {
        throw new IllegalArgumentException(
                "La contraseña es obligatoria."
        );
    }

    Carrera carreraPrincipal = carrerasAsignadas.isEmpty()
            ? null
            : carrerasAsignadas.get(0);

    Usuario usuario = Usuario.builder()
            .nombre(request.nombre().trim())
            .correo(correo)
            .password(passwordEncoder.encode(request.password()))
            .rol(request.rol())
            .activo(true)
            .carrera(carreraPrincipal)
            .carreras(new HashSet<>(carrerasAsignadas))
            .build();

    return mapToResponse(usuarioRepository.save(usuario));
}
    // ==========================================
    // LISTAR USUARIOS
    // ==========================================

    @Override
@Transactional(readOnly = true)
public List<UsuarioResponse> listar() {

    Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

    List<Usuario> usuarios;

    if (usuarioActual.getRol() == Rol.ADMINISTRADOR) {

        usuarios = usuarioRepository.findAll();

    } else if (usuarioActual.getRol() == Rol.COORDINADOR) {

        Long carreraId = usuarioAutenticado.obtenerCarreraId();

        usuarios = usuarioRepository.findAll()
                .stream()
                .filter(usuario -> usuario.getRol() == Rol.DOCENTE)
                .filter(usuario ->
                        perteneceACarrera(usuario, carreraId)
                )
                .toList();

    } else {

        throw new AccessDeniedException(
                "No tienes permisos para consultar usuarios."
        );
    }

    return usuarios.stream()
            .map(this::mapToResponse)
            .toList();
}

    // ==========================================
    // BUSCAR USUARIO POR ID
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {

        Usuario usuario = obtenerUsuarioGestionable(id);

        return mapToResponse(usuario);
    }

    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================
@Override
public UsuarioResponse actualizar(Long id, UsuarioRequest request) {

    Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

    boolean esAdministrador =
            usuarioActual.getRol() == Rol.ADMINISTRADOR;

    Usuario usuario = obtenerUsuarioGestionable(id);

    String correo = request.correo()
            .trim()
            .toLowerCase(Locale.ROOT);

    if (!usuario.getCorreo().equalsIgnoreCase(correo)
            && usuarioRepository.existsByCorreo(correo)) {

        throw new IllegalArgumentException(
                "El correo ya está registrado por otro usuario."
        );
    }

    if (!esAdministrador && request.rol() != Rol.DOCENTE) {
        throw new AccessDeniedException(
                "El coordinador solamente puede administrar docentes."
        );
    }

    usuario.setNombre(request.nombre().trim());
    usuario.setCorreo(correo);
    usuario.setRol(request.rol());

    if (esAdministrador) {

        if (request.rol() == Rol.DOCENTE) {

            List<Carrera> carrerasAsignadas =
                    obtenerCarrerasDocente(request.carreraIds());

            usuario.setCarreras(
                    new HashSet<>(carrerasAsignadas)
            );

            usuario.setCarrera(
                    carrerasAsignadas.get(0)
            );

        } else {

            Carrera carrera = obtenerCarrera(request.carreraId());

            usuario.setCarrera(carrera);
            usuario.getCarreras().clear();

        }
    }

    // El coordinador conserva las carreras que ya tiene asignadas.

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

        if (!usuarioAutenticado.esAdministrador()) {
            throw new AccessDeniedException(
                    "Solo un administrador puede eliminar usuarios definitivamente."
            );
        }

        Usuario usuario = usuarioRepository.findWithCarreraById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado.")
                );

        usuarioRepository.delete(usuario);
    }

    // ==========================================
    // ACTIVAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse activar(Long id) {

        Usuario usuario = obtenerUsuarioGestionable(id);

        usuario.setActivo(true);

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // DESACTIVAR USUARIO
    // ==========================================

    @Override
    public UsuarioResponse desactivar(Long id) {

        Usuario usuario = obtenerUsuarioGestionable(id);

        usuario.setActivo(false);

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ==========================================
    // OBTENER USUARIO SEGÚN PERMISOS
    // ==========================================
private Usuario obtenerUsuarioGestionable(Long id) {

    Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

    if (usuarioActual.getRol() == Rol.ADMINISTRADOR) {

        return usuarioRepository.findWithCarreraById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado.")
                );
    }

    if (usuarioActual.getRol() == Rol.COORDINADOR) {

        Long carreraId = usuarioAutenticado.obtenerCarreraId();

        Usuario usuario = usuarioRepository.findWithCarreraById(id)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "El usuario no existe o no pertenece a tu carrera."
                        )
                );

        if (usuario.getRol() != Rol.DOCENTE
                || !perteneceACarrera(usuario, carreraId)) {

            throw new AccessDeniedException(
                    "El usuario no existe o no pertenece a tu carrera."
            );
        }

        return usuario;
    }

    throw new AccessDeniedException(
            "No tienes permisos para administrar usuarios."
    );
}
    // ==========================================
    // IMPORTAR USUARIOS DESDE EXCEL
    // ==========================================

    @Override
    public ResultadoImportacion importarExcel(
            MultipartFile archivo
    ) {

        Usuario usuarioActual = usuarioAutenticado.obtenerUsuario();

        boolean esAdministrador =
                usuarioActual.getRol() == Rol.ADMINISTRADOR;

        if (!esAdministrador
                && usuarioActual.getRol() != Rol.COORDINADOR) {

            throw new AccessDeniedException(
                    "No tienes permisos para importar usuarios."
            );
        }

        Carrera carreraCoordinador = null;

        if (!esAdministrador) {

            carreraCoordinador =
                    obtenerCarreraDelCoordinador(usuarioActual);
        }

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un archivo Excel."
            );
        }

        String nombreArchivo = archivo.getOriginalFilename();

        if (nombreArchivo == null
                || !(nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                || nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".xls"))) {

            throw new IllegalArgumentException(
                    "El archivo debe tener formato XLSX o XLS."
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
                throw new IllegalArgumentException(
                        "El archivo no contiene hojas."
                );
            }

            Sheet hoja = workbook.getSheetAt(0);

            if (hoja.getLastRowNum() < 1) {
                throw new IllegalArgumentException(
                        "El Excel no contiene usuarios."
                );
            }

            // Cargar carreras disponibles para administradores.
            Map<String, Carrera> carreras = new HashMap<>();

            if (esAdministrador) {

                carreras = carreraRepository.findAll()
                        .stream()
                        .filter(c -> c.getNombre() != null)
                        .collect(Collectors.toMap(
                                c -> normalizar(c.getNombre()),
                                c -> c,
                                (a, b) -> a
                        ));
            }

            // Recorrer filas después del encabezado.
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

                    // Validar nombre completo.
                    if (nombre.isBlank()) {
                        throw new IllegalArgumentException(
                                "El nombre completo es obligatorio."
                        );
                    }

                    if (nombre.trim().split("\\s+").length < 2) {
                        throw new IllegalArgumentException(
                                "Debes capturar nombre y apellidos."
                        );
                    }

                    // Validar correo.
                    if (!correo.matches(
                            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                    )) {
                        throw new IllegalArgumentException(
                                "El correo electrónico no es válido."
                        );
                    }

                    // Evitar correos duplicados en el Excel.
                    if (!correosArchivo.add(correo)) {
                        throw new IllegalArgumentException(
                                "El correo está duplicado en el Excel."
                        );
                    }

                    // Evitar correos existentes.
                    if (usuarioRepository.existsByCorreo(correo)) {
                        throw new IllegalArgumentException(
                                "El correo ya está registrado en SIGELAB."
                        );
                    }

                    // Validar fecha de nacimiento.
                    LocalDate fechaNacimiento =
                            interpretarFecha(fila.getCell(2), formatter);

                    if (!fechaNacimiento.isBefore(LocalDate.now())) {
                        throw new IllegalArgumentException(
                                "La fecha de nacimiento debe ser anterior a hoy."
                        );
                    }

                    // Validar rol permitido.
                    Rol rol;

                    if (rolTexto.equalsIgnoreCase("Docente")) {

                        rol = Rol.DOCENTE;

                    } else if (rolTexto.equalsIgnoreCase("Coordinador")) {

                        if (!esAdministrador) {
                            throw new IllegalArgumentException(
                                    "Los coordinadores no pueden importar otros coordinadores."
                            );
                        }

                        rol = Rol.COORDINADOR;

                    } else {

                        throw new IllegalArgumentException(
                                "El rol debe ser Docente o Coordinador."
                        );
                    }

                    // Determinar carrera.
                    Carrera carrera;

                    if (esAdministrador) {

                        carrera = carreras.get(
                                normalizar(carreraTexto)
                        );

                        if (carrera == null) {
                            throw new IllegalArgumentException(
                                    "La carrera adscrita no existe en SIGELAB."
                            );
                        }

                    } else {

                        // Se ignora la carrera escrita en el Excel.
                        // El coordinador solo registra docentes en su carrera.
                        carrera = carreraCoordinador;
                    }

                    if (Boolean.FALSE.equals(carrera.getActivo())) {
                        throw new IllegalArgumentException(
                                "La carrera está inactiva."
                        );
                    }

                    // Generar contraseña temporal.
                    String passwordTemporal =
                            generarPasswordTemporal(
                                    nombre,
                                    fechaNacimiento
                            );

                    // Crear usuario.
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
                                    "Usuario registrado correctamente."
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
                                            : "Error al registrar el usuario."
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
    // OBTENER CARRERA DEL COORDINADOR
    // ==========================================

    private Carrera obtenerCarreraDelCoordinador(Usuario coordinador) {

        if (coordinador.getCarrera() == null) {
            throw new AccessDeniedException(
                    "El coordinador no tiene una carrera asignada."
            );
        }

        Carrera carrera = coordinador.getCarrera();

        if (Boolean.FALSE.equals(carrera.getActivo())) {
            throw new AccessDeniedException(
                    "La carrera asignada al coordinador está inactiva."
            );
        }

        return carrera;
    }

    // ==========================================
    // OBTENER CARRERA POR ID
    // ==========================================

    private Carrera obtenerCarrera(Long carreraId) {

        if (carreraId == null) {
            return null;
        }

        Carrera carrera = carreraRepository.findById(carreraId)
                .orElseThrow(() ->
                        new RuntimeException("Carrera no encontrada.")
                );

        if (Boolean.FALSE.equals(carrera.getActivo())) {
            throw new IllegalArgumentException(
                    "No puedes asignar usuarios a una carrera inactiva."
            );
        }

        return carrera;
    }

    // ==========================================
// OBTENER VARIAS CARRERAS PARA UN DOCENTE
// ==========================================

private List<Carrera> obtenerCarrerasDocente(List<Long> carreraIds) {

    if (carreraIds == null || carreraIds.isEmpty()) {
        throw new IllegalArgumentException(
                "Debes asignar al menos una carrera al docente."
        );
    }

    Set<Long> idsUnicos = new HashSet<>(carreraIds);

    if (idsUnicos.size() != carreraIds.size()) {
        throw new IllegalArgumentException(
                "No puedes seleccionar carreras duplicadas."
        );
    }

    List<Carrera> carreras = carreraRepository.findAllById(idsUnicos);

    if (carreras.size() != idsUnicos.size()) {
        throw new IllegalArgumentException(
                "Una o más carreras seleccionadas no existen."
        );
    }

    if (carreras.stream().anyMatch(
            carrera -> !Boolean.TRUE.equals(carrera.getActivo())
    )) {
        throw new IllegalArgumentException(
                "No puedes asignar carreras inactivas."
        );
    }

    // Mantener el mismo orden en el que se recibieron los IDs.
    Map<Long, Carrera> carrerasPorId = carreras.stream()
            .collect(Collectors.toMap(
                    Carrera::getId,
                    carrera -> carrera
            ));

    return carreraIds.stream()
            .map(carrerasPorId::get)
            .toList();
}


// ==========================================
// VALIDAR SI UN DOCENTE PERTENECE A UNA CARRERA
// ==========================================

private boolean perteneceACarrera(Usuario usuario, Long carreraId) {

    if (carreraId == null) {
        return false;
    }

    if (usuario.getCarreras() != null
            && usuario.getCarreras().stream()
            .anyMatch(carrera ->
                    carrera.getId().equals(carreraId))) {
        return true;
    }

    // Compatibilidad con usuarios anteriores a la relación múltiple.
    return usuario.getCarrera() != null
            && usuario.getCarrera().getId().equals(carreraId);
}


// ==========================================
// OBTENER LAS CARRERAS DEL DOCENTE AUTENTICADO
// ==========================================

@Override
@Transactional(readOnly = true)
public List<CarreraUsuarioResponse> misCarreras() {

    Usuario usuario = usuarioAutenticado.obtenerUsuario();

    if (usuario.getRol() != Rol.DOCENTE) {
        throw new AccessDeniedException(
                "Esta operación está disponible para docentes."
        );
    }

    Set<Carrera> carreras = new HashSet<>();

    if (usuario.getCarreras() != null) {
        carreras.addAll(usuario.getCarreras());
    }

    // Compatibilidad con usuarios que todavía no tengan relación múltiple.
    if (carreras.isEmpty() && usuario.getCarrera() != null) {
        carreras.add(usuario.getCarrera());
    }

    return carreras.stream()
            .filter(carrera -> Boolean.TRUE.equals(carrera.getActivo()))
            .sorted(Comparator.comparing(Carrera::getNombre))
            .map(carrera -> new CarreraUsuarioResponse(
                    carrera.getId(),
                    carrera.getClave(),
                    carrera.getNombre(),
                    carrera.getActivo()
            ))
            .toList();
}

    // ==========================================
    // GENERAR CONTRASEÑA TEMPORAL
    // ==========================================

    private String generarPasswordTemporal(
            String nombre,
            LocalDate fechaNacimiento
    ) {

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
            DataFormatter formatter
    ) {

        if (celda == null) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
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
                // Intentar siguiente formato.
            }
        }

        throw new IllegalArgumentException(
                "Fecha inválida. Utiliza DD/MM/AAAA."
        );
    }

    // ==========================================
    // LEER CELDA
    // ==========================================

    private String leerCelda(
            Row fila,
            int columna,
            DataFormatter formatter
    ) {

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
            DataFormatter formatter
    ) {

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
    // MAPEAR RESPUESTA
    // ==========================================

   private UsuarioResponse mapToResponse(Usuario usuario) {

    Set<Carrera> carrerasAsignadas = new HashSet<>();

    if (usuario.getCarreras() != null) {
        carrerasAsignadas.addAll(usuario.getCarreras());
    }

    // Compatibilidad con usuarios que todavía no tengan carreras en la tabla puente.
    if (carrerasAsignadas.isEmpty() && usuario.getCarrera() != null) {
        carrerasAsignadas.add(usuario.getCarrera());
    }

    List<Carrera> carrerasOrdenadas = carrerasAsignadas.stream()
            .sorted(Comparator.comparing(Carrera::getNombre))
            .toList();

    Carrera carreraPrincipal = usuario.getCarrera();

    if (carreraPrincipal == null && !carrerasOrdenadas.isEmpty()) {
        carreraPrincipal = carrerasOrdenadas.get(0);
    }

    List<Long> carreraIds = carrerasOrdenadas.stream()
            .map(Carrera::getId)
            .toList();

    List<String> nombresCarreras = carrerasOrdenadas.stream()
            .map(Carrera::getNombre)
            .toList();

    return new UsuarioResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRol().name(),
            usuario.getActivo(),
            carreraPrincipal != null ? carreraPrincipal.getId() : null,
            carreraPrincipal != null ? carreraPrincipal.getNombre() : null,
            carreraIds,
            nombresCarreras
    );
}
}
