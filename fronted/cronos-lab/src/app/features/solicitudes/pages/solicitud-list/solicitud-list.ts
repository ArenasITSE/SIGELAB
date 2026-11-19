
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import {
  finalize,
  timeout
} from 'rxjs';

import { environment } from '../../../../environments/environment';

import { SolicitudService } from '../../services/solicitud.service';

import { UsuarioService } from '../../../usuarios/services/usuario.service';

import {
  SolicitudLaboratorio,
  SolicitudRequest,
  EstatusSolicitud,
  LaboratorioOption
} from '../../models/solicitud.model';

import { CarreraOption } from '../../../usuarios/models/usuario.model';

// ==========================================
// INTERFACES
// ==========================================

interface MateriaOption {
  id: number;
  nombre: string;
  carreraId?: number;
}

type SolicitudApi = Partial<SolicitudLaboratorio> & {
  estado?: string;
  estatus?: string;
  docenteId?: number;
  docente?: string;
  solicitanteId?: number;
  solicitante?: string;
  materiaId?: number;
  materia?: string;
};

// ==========================================
// COMPONENTE
// ==========================================

@Component({
  selector: 'app-solicitud-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitud-list.html',
  styleUrl: './solicitud-list.scss'
})
export class SolicitudListComponent implements OnInit {

  private solicitudService = inject(SolicitudService);
  private usuarioService = inject(UsuarioService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // INFORMACIÓN
  // ==========================================

  solicitudes: SolicitudLaboratorio[] = [];

  laboratorios: LaboratorioOption[] = [];

  materias: MateriaOption[] = [];

  carrerasDocente: CarreraOption[] = [];

  // ==========================================
  // USUARIO Y PERMISOS
  // ==========================================

  get rol(): string {
    return (localStorage.getItem('rol') ?? '').toUpperCase();
  }

  get puedeGestionar(): boolean {
    return ['ADMINISTRADOR', 'COORDINADOR'].includes(this.rol);
  }

  get puedeCrear(): boolean {
    return ['DOCENTE', 'ADMINISTRADOR', 'COORDINADOR']
      .includes(this.rol);
  }

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;

  cargandoLaboratorios = false;

  cargandoMaterias = false;

  cargandoCarreras = false;

  guardando = false;

  procesandoId: number | null = null;

  mostrarFormulario = false;

  mostrarRechazo = false;

  solicitudSeleccionada: SolicitudLaboratorio | null = null;

  // ==========================================
  // CARRERA SELECCIONADA
  // ==========================================

  carreraSeleccionadaId: number | null = null;

  // ==========================================
  // FILTROS
  // ==========================================

  busqueda = '';

  filtroEstatus = '';

  filtroFecha = '';

  // ==========================================
  // MENSAJES
  // ==========================================

  mensaje = '';

  tipoMensaje: 'success' | 'error' = 'success';

  // ==========================================
  // FORMULARIO
  // ==========================================

  formulario: SolicitudRequest = this.formularioVacio();

  observacionesRechazo = '';

  fechaMinima = this.obtenerFechaLocal();

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.listarSolicitudes();
    this.cargarLaboratorios();
    this.cargarMaterias();

    if (this.rol === 'DOCENTE') {
      this.cargarMisCarreras();
    }
  }

  // ==========================================
  // FORMULARIO VACÍO
  // ==========================================

  formularioVacio(): SolicitudRequest {
    return {
      materiaId: 0,
      laboratorioId: 0,
      fecha: '',
      horaInicio: '',
      horaFin: '',
      numeroPersonas: 1,
      motivo: ''
    };
  }

  private obtenerFechaLocal(): string {
    const fecha = new Date();

    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  // ==========================================
  // CARGAR CARRERAS DEL DOCENTE
  // ==========================================

  cargarMisCarreras(): void {

    this.cargandoCarreras = true;

    this.usuarioService.misCarreras()
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.cargandoCarreras = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: data => {

          if (!Array.isArray(data)) {
            this.carrerasDocente = [];

            this.mostrarMensaje(
              'La respuesta de carreras no es válida.',
              'error'
            );

            return;
          }

          this.carrerasDocente = data.filter(
            carrera => carrera.activo
          );
        },

        error: error => {

          console.error(
            'Error al cargar las carreras:',
            error
          );

          this.carrerasDocente = [];

          this.mostrarMensaje(
            this.obtenerMensajeError(
              error,
              'No fue posible cargar tus carreras asignadas.'
            ),
            'error'
          );
        }
      });
  }

  // ==========================================
  // CAMBIAR CARRERA
  // ==========================================

  cambiarCarrera(carreraId: number | null): void {

    this.carreraSeleccionadaId =
      carreraId !== null ? Number(carreraId) : null;

    // Reiniciar las selecciones anteriores.
    this.formulario.materiaId = 0;
    this.formulario.laboratorioId = 0;

    this.cdr.detectChanges();
  }

  // ==========================================
  // MATERIAS DISPONIBLES
  // ==========================================

  get materiasDisponibles(): MateriaOption[] {

    // Administrador y coordinador conservan el listado completo.
    if (this.rol !== 'DOCENTE') {
      return this.materias;
    }

    if (this.carreraSeleccionadaId === null) {
      return [];
    }

    return this.materias.filter(
      materia =>
        Number(materia.carreraId) === this.carreraSeleccionadaId
    );
  }

  // ==========================================
  // LABORATORIOS DISPONIBLES
  // ==========================================

  get laboratoriosDisponibles(): LaboratorioOption[] {

    // Administrador y coordinador conservan el listado completo.
    if (this.rol !== 'DOCENTE') {
      return this.laboratorios;
    }

    if (this.carreraSeleccionadaId === null) {
      return [];
    }

    return this.laboratorios.filter(
      laboratorio =>
        Number(laboratorio.carreraId) === this.carreraSeleccionadaId
    );
  }

  // ==========================================
  // NORMALIZAR ESTADOS
  // ==========================================

  private normalizarEstado(
    estado?: string | null
  ): EstatusSolicitud {

    switch ((estado ?? '').toUpperCase()) {

      case 'APROBADA':
      case 'AUTORIZADA':
        return 'AUTORIZADA';

      case 'RECHAZADA':
        return 'RECHAZADA';

      default:
        return 'PENDIENTE';
    }
  }

  // ==========================================
  // NORMALIZAR SOLICITUD
  // ==========================================

  private normalizarSolicitud(
    data: SolicitudApi
  ): SolicitudLaboratorio {

    return {
      id: Number(data.id ?? 0),

      laboratorioId: Number(data.laboratorioId ?? 0),
      laboratorio: data.laboratorio ?? '',

      solicitanteId: Number(
        data.solicitanteId ?? data.docenteId ?? 0
      ),

      solicitante:
        data.solicitante ?? data.docente ?? '',

      carreraId: Number(data.carreraId ?? 0),
      carrera: data.carrera ?? '',

      fecha: data.fecha ?? '',
      horaInicio: data.horaInicio ?? '',
      horaFin: data.horaFin ?? '',

      numeroPersonas: Number(data.numeroPersonas ?? 0),
      motivo: data.motivo ?? '',

      estatus: this.normalizarEstado(
        data.estado ?? data.estatus
      ),

      observaciones: data.observaciones ?? null,
      fechaSolicitud: data.fechaSolicitud
    };
  }

  private actualizarSolicitudes(
    data: SolicitudApi[]
  ): void {

    if (!Array.isArray(data)) {
      this.mostrarMensaje(
        'La respuesta de solicitudes no es válida.',
        'error'
      );

      return;
    }

    this.solicitudes = data.map(
      solicitud => this.normalizarSolicitud(solicitud)
    );

    this.cdr.detectChanges();
  }

  // ==========================================
  // CARGAR LABORATORIOS
  // ==========================================

  cargarLaboratorios(): void {

    this.cargandoLaboratorios = true;

    this.http.get<LaboratorioOption[]>(
      `${environment.apiUrl}/laboratorios`
    )
    .pipe(
      timeout({ first: 15000 }),
      finalize(() => {
        this.cargandoLaboratorios = false;
        this.cdr.detectChanges();
      })
    )
    .subscribe({

      next: data => {

        if (!Array.isArray(data)) {
          this.mostrarMensaje(
            'La respuesta de laboratorios no es válida.',
            'error'
          );

          return;
        }

        this.laboratorios = data.filter(
          laboratorio =>
            laboratorio.estatus?.toUpperCase() === 'DISPONIBLE'
        );
      },

      error: error => {

        console.error(
          'Error al cargar laboratorios:',
          error
        );

        this.mostrarMensaje(
          this.obtenerMensajeError(
            error,
            'No fue posible cargar los laboratorios.'
          ),
          'error'
        );
      }
    });
  }

  // ==========================================
  // CARGAR MATERIAS
  // ==========================================

  cargarMaterias(): void {

    this.cargandoMaterias = true;

    this.http.get<MateriaOption[]>(
      `${environment.apiUrl}/materias`
    )
    .pipe(
      timeout({ first: 15000 }),
      finalize(() => {
        this.cargandoMaterias = false;
        this.cdr.detectChanges();
      })
    )
    .subscribe({

      next: data => {

        if (!Array.isArray(data)) {
          this.materias = [];

          this.mostrarMensaje(
            'La respuesta de materias no es válida.',
            'error'
          );

          return;
        }

        this.materias = data;
      },

      error: error => {

        console.error(
          'Error al cargar materias:',
          error
        );

        this.materias = [];

        this.mostrarMensaje(
          this.obtenerMensajeError(
            error,
            'No fue posible cargar las materias.'
          ),
          'error'
        );
      }
    });
  }

  // ==========================================
  // LISTAR SOLICITUDES
  // ==========================================

  listarSolicitudes(): void {

    this.cargando = true;

    this.solicitudService.listar()
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: data => {
          this.actualizarSolicitudes(data);
        },

        error: error => {

          console.error(
            'Error al consultar solicitudes:',
            error
          );

          this.mostrarMensaje(
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las solicitudes.'
            ),
            'error'
          );
        }
      });
  }

  // ==========================================
  // FILTROS
  // ==========================================

  get solicitudesFiltradas(): SolicitudLaboratorio[] {

    const texto = this.busqueda.toLowerCase().trim();

    return this.solicitudes.filter(solicitud => {

      const coincideTexto =
        (solicitud.laboratorio ?? '')
          .toLowerCase().includes(texto) ||

        (solicitud.solicitante ?? '')
          .toLowerCase().includes(texto) ||

        (solicitud.carrera ?? '')
          .toLowerCase().includes(texto) ||

        (solicitud.motivo ?? '')
          .toLowerCase().includes(texto);

      const coincideEstatus =
        !this.filtroEstatus ||
        solicitud.estatus === this.filtroEstatus;

      const coincideFecha =
        !this.filtroFecha ||
        solicitud.fecha === this.filtroFecha;

      return coincideTexto && coincideEstatus && coincideFecha;
    });
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalSolicitudes(): number {
    return this.solicitudes.length;
  }

  get totalPendientes(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'PENDIENTE'
    ).length;
  }

  get totalAutorizadas(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'AUTORIZADA'
    ).length;
  }

  get totalRechazadas(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'RECHAZADA'
    ).length;
  }

  // ==========================================
  // LABORATORIO SELECCIONADO
  // ==========================================

  get laboratorioSeleccionado(): LaboratorioOption | undefined {

    return this.laboratoriosDisponibles.find(
      laboratorio =>
        laboratorio.id === Number(this.formulario.laboratorioId)
    );
  }

  // ==========================================
  // VALIDAR HORARIO
  // ==========================================

  get horarioValido(): boolean {

    if (
      !this.formulario.horaInicio ||
      !this.formulario.horaFin
    ) {
      return false;
    }

    return this.formulario.horaFin > this.formulario.horaInicio;
  }

  // ==========================================
  // NUEVA SOLICITUD
  // ==========================================

  nuevaSolicitud(): void {

    if (!this.puedeCrear) {
      this.mostrarMensaje(
        'No tienes permisos para registrar solicitudes.',
        'error'
      );

      return;
    }

    if (this.guardando || this.procesandoId !== null) {
      return;
    }

    if (
      this.cargandoLaboratorios ||
      this.cargandoMaterias ||
      (this.rol === 'DOCENTE' && this.cargandoCarreras)
    ) {
      this.mostrarMensaje(
        'Espera a que se carguen las carreras, laboratorios y materias.',
        'error'
      );

      return;
    }

    if (this.rol === 'DOCENTE' && this.carrerasDocente.length === 0) {
      this.mostrarMensaje(
        'No tienes carreras asignadas para registrar solicitudes.',
        'error'
      );

      return;
    }

    if (this.laboratorios.length === 0) {
      this.mostrarMensaje(
        'No hay laboratorios disponibles para solicitar.',
        'error'
      );

      return;
    }

    if (this.materias.length === 0) {
      this.mostrarMensaje(
        'No hay materias disponibles para seleccionar.',
        'error'
      );

      return;
    }

    this.formulario = this.formularioVacio();

    this.carreraSeleccionadaId = null;

    this.mostrarFormulario = true;

    this.mensaje = '';
  }

  // ==========================================
  // GUARDAR SOLICITUD
  // ==========================================

  guardarSolicitud(): void {

    if (this.guardando || !this.puedeCrear) {
      return;
    }

    const laboratorio = this.laboratorioSeleccionado;

    // Validar selección de carrera del docente.
    if (this.rol === 'DOCENTE') {

      if (this.carreraSeleccionadaId === null) {
        this.mostrarMensaje(
          'Selecciona una carrera.',
          'error'
        );

        return;
      }

      const carreraAsignada = this.carrerasDocente.some(
        carrera => carrera.id === this.carreraSeleccionadaId
      );

      if (!carreraAsignada) {
        this.mostrarMensaje(
          'La carrera seleccionada no está asignada a tu usuario.',
          'error'
        );

        return;
      }
    }

    // Validar campos obligatorios.
    if (
      !this.formulario.materiaId ||
      !this.formulario.laboratorioId ||
      !this.formulario.fecha ||
      !this.formulario.horaInicio ||
      !this.formulario.horaFin ||
      !this.formulario.numeroPersonas ||
      !this.formulario.motivo.trim()
    ) {

      this.mostrarMensaje(
        'Completa todos los campos obligatorios.',
        'error'
      );

      return;
    }

    // Validar fecha.
    if (this.formulario.fecha < this.fechaMinima) {

      this.mostrarMensaje(
        'No puedes solicitar una fecha anterior a hoy.',
        'error'
      );

      return;
    }

    // Validar horario.
    if (!this.horarioValido) {

      this.mostrarMensaje(
        'La hora de finalización debe ser posterior a la hora de inicio.',
        'error'
      );

      return;
    }

    // Validar laboratorio.
    if (!laboratorio) {

      this.mostrarMensaje(
        'Selecciona un laboratorio válido.',
        'error'
      );

      return;
    }

    // Validar materia.
    const materia = this.materiasDisponibles.find(
      item =>
        item.id === Number(this.formulario.materiaId)
    );

    if (!materia) {

      this.mostrarMensaje(
        'Selecciona una materia válida para la carrera elegida.',
        'error'
      );

      return;
    }

    // Validar que materia y laboratorio correspondan
    // a la carrera seleccionada por el docente.
    if (this.rol === 'DOCENTE') {

      if (
        Number(materia.carreraId) !== this.carreraSeleccionadaId
      ) {
        this.mostrarMensaje(
          'La materia no pertenece a la carrera seleccionada.',
          'error'
        );

        return;
      }

      if (
        Number(laboratorio.carreraId) !== this.carreraSeleccionadaId
      ) {
        this.mostrarMensaje(
          'El laboratorio no pertenece a la carrera seleccionada.',
          'error'
        );

        return;
      }
    }

    // Validar que materia y laboratorio pertenezcan
    // a la misma carrera para todos los roles.
    if (
      Number(materia.carreraId) !== Number(laboratorio.carreraId)
    ) {
      this.mostrarMensaje(
        'La materia y el laboratorio deben pertenecer a la misma carrera.',
        'error'
      );

      return;
    }

    // Validar capacidad.
    const personas = Number(this.formulario.numeroPersonas);

    if (
      !Number.isInteger(personas) ||
      personas < 1 ||
      personas > laboratorio.capacidad
    ) {

      this.mostrarMensaje(
        `La capacidad máxima de este laboratorio es de ${laboratorio.capacidad} personas.`,
        'error'
      );

      return;
    }

    // Construir petición.
    const request: SolicitudRequest = {

      materiaId: Number(this.formulario.materiaId),

      laboratorioId: Number(this.formulario.laboratorioId),

      fecha: this.formulario.fecha,

      horaInicio: this.formulario.horaInicio,

      horaFin: this.formulario.horaFin,

      numeroPersonas: personas,

      motivo: this.formulario.motivo.trim()
    };

    this.guardando = true;

    this.solicitudService.crear(request)
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.guardando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: () => {

          this.formulario = this.formularioVacio();

          this.carreraSeleccionadaId = null;

          this.mostrarFormulario = false;

          this.mostrarMensaje(
            'Solicitud registrada correctamente.',
            'success'
          );

          this.listarSolicitudes();
        },

        error: error => {

          console.error(
            'Error al registrar solicitud:',
            error
          );

          this.mostrarMensaje(
            this.obtenerMensajeError(
              error,
              'No fue posible registrar la solicitud.'
            ),
            'error'
          );
        }
      });
  }

  // ==========================================
  // AUTORIZAR SOLICITUD
  // ==========================================

  autorizarSolicitud(
    solicitud: SolicitudLaboratorio
  ): void {

    if (!this.puedeGestionar) {
      this.mostrarMensaje(
        'No tienes permisos para autorizar solicitudes.',
        'error'
      );

      return;
    }

    if (
      solicitud.estatus !== 'PENDIENTE' ||
      this.procesandoId !== null ||
      this.guardando
    ) {
      return;
    }

    const confirmar = confirm(
      `¿Deseas autorizar la solicitud de ${solicitud.solicitante}?\n\n` +
      `Laboratorio: ${solicitud.laboratorio}\n` +
      `Fecha: ${solicitud.fecha}\n` +
      `Horario: ${solicitud.horaInicio} - ${solicitud.horaFin}`
    );

    if (!confirmar) {
      return;
    }

    this.procesandoId = solicitud.id;

    this.solicitudService.autorizar(solicitud.id)
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.procesandoId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: () => {

          this.mostrarMensaje(
            'Solicitud autorizada correctamente.',
            'success'
          );

          this.listarSolicitudes();
        },

        error: error => {

          console.error(
            'Error al autorizar solicitud:',
            error
          );

          this.mostrarMensaje(
            this.obtenerMensajeError(
              error,
              'No fue posible autorizar la solicitud.'
            ),
            'error'
          );
        }
      });
  }

  // ==========================================
  // ABRIR RECHAZO
  // ==========================================

  abrirRechazo(
    solicitud: SolicitudLaboratorio
  ): void {

    if (!this.puedeGestionar) {
      return;
    }

    if (solicitud.estatus !== 'PENDIENTE') {
      return;
    }

    this.solicitudSeleccionada = solicitud;

    this.observacionesRechazo = '';

    this.mostrarRechazo = true;
  }

  // ==========================================
  // RECHAZAR SOLICITUD
  // ==========================================

  rechazarSolicitud(): void {

    if (
      !this.puedeGestionar ||
      !this.solicitudSeleccionada ||
      this.procesandoId !== null
    ) {
      return;
    }

    if (!this.observacionesRechazo.trim()) {

      this.mostrarMensaje(
        'Debes indicar el motivo del rechazo.',
        'error'
      );

      return;
    }

    const id = this.solicitudSeleccionada.id;

    this.procesandoId = id;

    this.solicitudService.rechazar(id, {
      observaciones: this.observacionesRechazo.trim()
    })
    .pipe(
      timeout({ first: 15000 }),
      finalize(() => {
        this.procesandoId = null;
        this.cdr.detectChanges();
      })
    )
    .subscribe({

      next: () => {

        this.mostrarRechazo = false;

        this.solicitudSeleccionada = null;

        this.observacionesRechazo = '';

        this.mostrarMensaje(
          'Solicitud rechazada correctamente.',
          'success'
        );

        this.listarSolicitudes();
      },

      error: error => {

        console.error(
          'Error al rechazar solicitud:',
          error
        );

        this.mostrarMensaje(
          this.obtenerMensajeError(
            error,
            'No fue posible rechazar la solicitud.'
          ),
          'error'
        );
      }
    });
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

    this.carreraSeleccionadaId = null;
  }

  cancelarRechazo(): void {

    if (this.procesandoId !== null) {
      return;
    }

    this.mostrarRechazo = false;

    this.solicitudSeleccionada = null;

    this.observacionesRechazo = '';
  }

  // ==========================================
  // NOMBRE DEL ESTATUS
  // ==========================================

  nombreEstatus(
    estatus: EstatusSolicitud | string | null | undefined
  ): string {

    const nombres: Record<EstatusSolicitud, string> = {

      PENDIENTE: 'Pendiente',

      AUTORIZADA: 'Autorizada',

      RECHAZADA: 'Rechazada'
    };

    return nombres[this.normalizarEstado(estatus)];
  }

  // ==========================================
  // MENSAJES DE ERROR
  // ==========================================

  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    if (
      typeof error?.error === 'string' &&
      error.error.trim()
    ) {
      return error.error;
    }

    if (error?.error?.mensaje) {
      return error.error.mensaje;
    }

    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.error?.detail) {
      return error.error.detail;
    }

    if (error?.status === 403) {
      return 'No tienes permisos para realizar esta operación.';
    }

    if (error?.status === 400) {
      return error?.error?.mensaje ??
        'La solicitud contiene datos inválidos. Revisa los campos obligatorios.';
    }

    return mensajeDefecto;
  }

  // ==========================================
  // MOSTRAR MENSAJE
  // ==========================================

  mostrarMensaje(
    mensaje: string,
    tipo: 'success' | 'error'
  ): void {

    this.mensaje = mensaje;

    this.tipoMensaje = tipo;

    this.cdr.detectChanges();
  }

}
