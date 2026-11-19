
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  finalize,
  switchMap,
  timeout
} from 'rxjs';

import { UsuarioService } from '../../services/usuario.service';
import {
  ResultadoImportacion
} from '../../services/usuario.service';

import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

import {
  Usuario,
  UsuarioRequest,
  RolUsuario,
  CarreraOption
} from '../../models/usuario.model';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuario-list.html',
  styleUrl: './usuario-list.scss'
})
export class UsuarioListComponent implements OnInit {

  private usuarioService = inject(UsuarioService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // INFORMACIÓN
  // ==========================================

  usuarios: Usuario[] = [];
  carreras: CarreraOption[] = [];

  roles: RolUsuario[] = [
    'ADMINISTRADOR',
    'COORDINADOR',
    'TECNICO',
    'DOCENTE'
  ];

  // ==========================================
  // BÚSQUEDA
  // ==========================================

  busqueda = '';
  filtroRol = '';

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;
  cargandoCarreras = false;
  guardando = false;

  mostrarFormulario = false;
  editando = false;
  mostrarPassword = false;

  usuarioId: number | null = null;
  procesandoId: number | null = null;

  // ==========================================
  // MENSAJES
  // ==========================================

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';

  // ==========================================
  // FORMULARIO
  // ==========================================

  formulario: UsuarioRequest = this.formularioVacio();

  // ==========================================
  // IMPORTACIÓN EXCEL
  // ==========================================

  archivoSeleccionado: File | null = null;
  importando = false;
  resultadoImportacion: ResultadoImportacion | null = null;

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.listarUsuarios();
    this.cargarCarreras();
  }

  // ==========================================
  // FORMULARIO VACÍO
  // ==========================================

 
formularioVacio(): UsuarioRequest {
  return {
    nombre: '',
    correo: '',
    password: '',
    rol: 'DOCENTE',
    carreraId: null,
    carreraIds: []
  };
}

  // ==========================================
  // CARGAR CARRERAS
  // ==========================================

  cargarCarreras(): void {

    this.cargandoCarreras = true;

    this.http.get<CarreraOption[]>(
      `${environment.apiUrl}/carreras`
    )
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.cargandoCarreras = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta de carreras no es válida.',
              'error'
            );
            return;
          }

          this.carreras = data.filter(
            carrera => carrera.activo !== false
          );
        },

        error: (error) => {

          console.error('Error al cargar carreras:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible cargar las carreras.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // FILTROS
  // ==========================================

  get usuariosFiltrados(): Usuario[] {

    const texto = this.busqueda.toLowerCase().trim();

    return this.usuarios.filter(usuario => {

      const coincideTexto =
        (usuario.nombre ?? '').toLowerCase().includes(texto) ||
        (usuario.correo ?? '').toLowerCase().includes(texto) ||
        (usuario.carrera ?? '').toLowerCase().includes(texto);

      const coincideRol =
        !this.filtroRol || usuario.rol === this.filtroRol;

      return coincideTexto && coincideRol;
    });
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalUsuarios(): number {
    return this.usuarios.length;
  }

  get totalActivos(): number {
    return this.usuarios.filter(u => u.activo).length;
  }

  get totalInactivos(): number {
    return this.usuarios.filter(u => !u.activo).length;
  }

  get totalAdministradores(): number {
    return this.usuarios.filter(
      u => u.rol === 'ADMINISTRADOR' && u.activo
    ).length;
  }

  // ==========================================
  // LISTAR USUARIOS
  // ==========================================

  listarUsuarios(): void {

    this.cargando = true;

    this.usuarioService.listar()
      .pipe(
        timeout({ first: 10000 }),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta del servidor no es válida.',
              'error'
            );
            return;
          }

          this.usuarios = [...data];
        },

        error: (error) => {

          console.error('Error al listar usuarios:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible consultar los usuarios.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // NUEVO USUARIO
  // ==========================================

  nuevoUsuario(): void {

    if (this.guardando || this.procesandoId !== null) {
      return;
    }

    if (this.cargandoCarreras || this.carreras.length === 0) {

      this.cargarCarreras();

      this.mostrarMensaje(
        'Espera a que se carguen las carreras disponibles.',
        'error'
      );

      return;
    }

    this.formulario = this.formularioVacio();
    this.usuarioId = null;
    this.editando = false;
    this.mostrarPassword = false;
    this.mostrarFormulario = true;
    this.mensaje = '';
  }

  // ==========================================
  // EDITAR USUARIO
  // ==========================================
editarUsuario(usuario: Usuario): void {

  if (this.guardando || this.procesandoId !== null) {
    return;
  }

  const carreraIds = usuario.carreraIds?.length
    ? [...usuario.carreraIds]
    : usuario.carreraId !== null
      ? [usuario.carreraId]
      : [];

  this.formulario = {
    nombre: usuario.nombre,
    correo: usuario.correo,
    password: '',
    rol: usuario.rol,
    carreraId: usuario.carreraId ?? null,
    carreraIds
  };

  this.usuarioId = usuario.id;
  this.editando = true;
  this.mostrarPassword = false;
  this.mostrarFormulario = true;
  this.mensaje = '';
}

  // ==========================================
  // GUARDAR USUARIO
  // ==========================================
guardarUsuario(): void {

  if (this.guardando || this.procesandoId !== null) {
    return;
  }

  const esDocente = this.formulario.rol === 'DOCENTE';

  const carrerasSeleccionadas = esDocente
    ? [...new Set(this.formulario.carreraIds ?? [])]
    : this.formulario.carreraId !== null
      ? [this.formulario.carreraId]
      : [];

  if (
    !this.formulario.nombre.trim() ||
    !this.formulario.correo.trim() ||
    !this.formulario.rol ||
    carrerasSeleccionadas.length === 0
  ) {
    this.mostrarMensaje(
      esDocente
        ? 'Completa los campos obligatorios y selecciona al menos una carrera para el docente.'
        : 'Completa todos los campos obligatorios, incluida la carrera.',
      'error'
    );
    return;
  }

  const correoValido =
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
      this.formulario.correo.trim()
    );

  if (!correoValido) {
    this.mostrarMensaje(
      'Ingresa un correo electrónico válido.',
      'error'
    );
    return;
  }

  if (
    !this.editando &&
    !this.formulario.password?.trim()
  ) {
    this.mostrarMensaje(
      'La contraseña es obligatoria para registrar un usuario.',
      'error'
    );
    return;
  }

  if (
    this.formulario.password?.trim() &&
    this.formulario.password.length < 8
  ) {
    this.mostrarMensaje(
      'La contraseña debe tener al menos 8 caracteres.',
      'error'
    );
    return;
  }

  this.guardando = true;

  const esEdicion =
    this.editando && this.usuarioId !== null;

  const request: UsuarioRequest = {
    nombre: this.formulario.nombre.trim(),
    correo: this.formulario.correo.trim(),
    rol: this.formulario.rol,

    // Se conserva la primera carrera como principal.
    carreraId: carrerasSeleccionadas[0],

    // Para docentes se envían todas las carreras.
    carreraIds: esDocente ? carrerasSeleccionadas : undefined
  };

  if (this.formulario.password?.trim()) {
    request.password = this.formulario.password;
  }

  const operacion = esEdicion
    ? this.usuarioService.actualizar(
        this.usuarioId!,
        request
      )
    : this.usuarioService.crear(request);

  operacion.pipe(
    switchMap(() => this.usuarioService.listar()),
    timeout({ first: 10000 }),
    finalize(() => {
      this.guardando = false;
      this.cdr.detectChanges();
    })
  ).subscribe({

    next: (data) => {

      if (!Array.isArray(data)) {
        this.mostrarMensaje(
          'No se pudo actualizar el listado.',
          'error'
        );
        return;
      }

      this.usuarios = [...data];

      this.mostrarFormulario = false;
      this.formulario = this.formularioVacio();
      this.usuarioId = null;
      this.editando = false;
      this.mostrarPassword = false;
      this.busqueda = '';
      this.filtroRol = '';

      this.mostrarMensaje(
        esEdicion
          ? 'Usuario actualizado correctamente.'
          : 'Usuario registrado correctamente.',
        'success'
      );
    },

    error: (error) => {

      console.error('Error al guardar usuario:', error);

      this.mostrarMensaje(
        error.error?.message ||
        'No fue posible guardar el usuario.',
        'error'
      );
    }
  });
}
  // ==========================================
  // DESACTIVAR USUARIO
  // ==========================================

  desactivarUsuario(usuario: Usuario): void {

    if (this.guardando || this.procesandoId !== null) {
      return;
    }

    if (!usuario.activo) {
      this.mostrarMensaje(
        'El usuario ya se encuentra inactivo.',
        'error'
      );
      return;
    }

    const confirmar = confirm(
      `¿Deseas desactivar al usuario "${usuario.nombre}"?\n\n` +
      'El usuario dejará de tener acceso al sistema.'
    );

    if (!confirmar) {
      return;
    }

    this.procesandoId = usuario.id;

    this.usuarioService.desactivar(usuario.id)
      .pipe(
        switchMap(() => this.usuarioService.listar()),
        timeout({ first: 10000 }),
        finalize(() => {
          this.procesandoId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta del listado no es válida.',
              'error'
            );
            return;
          }

          this.usuarios = [...data];

          this.mostrarMensaje(
            'Usuario desactivado correctamente.',
            'success'
          );
        },

        error: (error) => {

          console.error('Error al desactivar usuario:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible desactivar el usuario.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // SELECCIONAR ARCHIVO EXCEL
  // ==========================================

  seleccionarArchivo(event: Event): void {

    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    this.resultadoImportacion = null;

    if (!archivo) {
      this.archivoSeleccionado = null;
      return;
    }

    const extension = archivo.name
      .split('.')
      .pop()
      ?.toLowerCase();

    if (!['xlsx', 'xls'].includes(extension ?? '')) {

      this.archivoSeleccionado = null;
      input.value = '';

      this.mostrarMensaje(
        'Selecciona un archivo Excel .xlsx o .xls.',
        'error'
      );

      return;
    }

    if (archivo.size > 10 * 1024 * 1024) {

      this.archivoSeleccionado = null;
      input.value = '';

      this.mostrarMensaje(
        'El archivo no puede superar los 10 MB.',
        'error'
      );

      return;
    }

    this.archivoSeleccionado = archivo;
    this.mensaje = '';
  }

  // ==========================================
  // IMPORTAR USUARIOS
  // ==========================================

  importarUsuarios(): void {

    if (this.importando) {
      return;
    }

    if (!this.archivoSeleccionado) {
      this.mostrarMensaje(
        'Primero selecciona un archivo Excel.',
        'error'
      );
      return;
    }

    this.importando = true;
    this.resultadoImportacion = null;
    this.mensaje = '';

    this.usuarioService.importarExcel(this.archivoSeleccionado)
      .pipe(
        timeout({ first: 120000 }),
        finalize(() => {
          this.importando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (resultado) => {

          this.resultadoImportacion = resultado;
          this.archivoSeleccionado = null;

          this.listarUsuarios();

          this.mostrarMensaje(
            `Importación finalizada: ${resultado.exitosos} registrados y ${resultado.rechazados} rechazados.`,
            resultado.rechazados > 0 ? 'error' : 'success'
          );
        },

        error: (error) => {

          console.error('Error al importar Excel:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible importar el archivo Excel.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // LIMPIAR IMPORTACIÓN
  // ==========================================

  limpiarImportacion(input: HTMLInputElement): void {
    this.archivoSeleccionado = null;
    this.resultadoImportacion = null;
    input.value = '';
  }

  // ==========================================
  // CANCELAR FORMULARIO
  // ==========================================

  cancelarFormulario(): void {

    if (this.guardando) {
      return;
    }

    this.mostrarFormulario = false;
    this.formulario = this.formularioVacio();
    this.usuarioId = null;
    this.editando = false;
    this.mostrarPassword = false;
  }

  // ==========================================
  // MENSAJES
  // ==========================================

  mostrarMensaje(
    mensaje: string,
    tipo: 'success' | 'error'
  ): void {

    this.mensaje = mensaje;
    this.tipoMensaje = tipo;

    this.cdr.detectChanges();
  }

  // ==========================================
  // NOMBRE DEL ROL
  // ==========================================

  nombreRol(rol: string): string {

    const nombres: Record<string, string> = {
      ADMINISTRADOR: 'Administrador',
      COORDINADOR: 'Coordinador',
      TECNICO: 'Técnico',
      DOCENTE: 'Docente'
    };

    return nombres[rol] ?? rol;
  }

  actualizarCarrerasDocente(carreraIds: number[]): void {
  this.formulario.carreraIds = carreraIds;
  this.formulario.carreraId = carreraIds.length
    ? carreraIds[0]
    : null;
}

toggleCarrera(carreraId: number, seleccionada: boolean): void {
  const carrerasActuales = this.formulario.carreraIds ?? [];

  let nuevasCarreras: number[];

  if (seleccionada) {
    nuevasCarreras = [...carrerasActuales, carreraId];
  } else {
    nuevasCarreras = carrerasActuales.filter(
      id => id !== carreraId
    );
  }

  this.actualizarCarrerasDocente(nuevasCarreras);
}

  
  
}
