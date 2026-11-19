
import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { finalize, timeout } from 'rxjs';

import {
  SolicitudLaboratorio,
  EstatusSolicitud
} from '../../../solicitudes/models/solicitud.model';

import {
  SolicitudService
} from '../../../solicitudes/services/solicitud.service';

// ==========================================
// TIPOS
// ==========================================

interface SolicitudCalendario extends SolicitudLaboratorio {
  materia?: string | null;
}

interface SolicitudCalendarioApi extends Partial<SolicitudLaboratorio> {
  materia?: string | null;
  nombreMateria?: string | null;
  docente?: string | null;
  nombreDocente?: string | null;
  estado?: string | null;
}

interface DiaCalendario {
  fecha: Date;
  fechaKey: string;
  numero: number;
  perteneceMes: boolean;
  esHoy: boolean;
  seleccionado: boolean;
  solicitudes: SolicitudCalendario[];
}

// ==========================================
// COMPONENTE
// ==========================================

@Component({
  selector: 'app-calendario-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendario-list.html',
  styleUrl: './calendario-list.scss'
})
export class CalendarioListComponent implements OnInit {

  private solicitudService = inject(SolicitudService);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // DATOS
  // ==========================================

  solicitudes: SolicitudCalendario[] = [];

  calendarDays: DiaCalendario[] = [];

  solicitudesDelDia: SolicitudCalendario[] = [];

  // ==========================================
  // FECHAS
  // ==========================================

  hoy = new Date();

  fechaActual = new Date(
    this.hoy.getFullYear(),
    this.hoy.getMonth(),
    1
  );

  fechaSeleccionada = new Date(
    this.hoy.getFullYear(),
    this.hoy.getMonth(),
    this.hoy.getDate()
  );

  // ==========================================
  // FILTROS
  // ==========================================

  filtroEstatus = 'TODOS';

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;

  mensaje = '';

  tipoMensaje: 'success' | 'error' = 'success';

  // ==========================================
  // NOMBRES DEL CALENDARIO
  // ==========================================

  diasSemana = [
    'Lun',
    'Mar',
    'Mié',
    'Jue',
    'Vie',
    'Sáb',
    'Dom'
  ];

  // ==========================================
  // ENCABEZADO
  // ==========================================

  get nombreMes(): string {
    const nombre = this.fechaActual.toLocaleDateString('es-MX', {
      month: 'long',
      year: 'numeric'
    });

    return nombre.charAt(0).toUpperCase() + nombre.slice(1);
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalSolicitudes(): number {
    return this.solicitudes.length;
  }

  get totalAutorizadas(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'AUTORIZADA'
    ).length;
  }

  get totalPendientes(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'PENDIENTE'
    ).length;
  }

  get totalRechazadas(): number {
    return this.solicitudes.filter(
      solicitud => solicitud.estatus === 'RECHAZADA'
    ).length;
  }

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.cargarSolicitudes();
  }

  // ==========================================
  // NORMALIZAR RESPUESTA
  // ==========================================

  private normalizarSolicitud(
    solicitud: SolicitudCalendarioApi
  ): SolicitudCalendario {

    const estadoRecibido = (
      solicitud.estatus ??
      solicitud.estado ??
      'PENDIENTE'
    ).toUpperCase();

    let estatus: EstatusSolicitud;

    switch (estadoRecibido) {
      case 'APROBADA':
      case 'AUTORIZADA':
        estatus = 'AUTORIZADA';
        break;

      case 'RECHAZADA':
        estatus = 'RECHAZADA';
        break;

      default:
        estatus = 'PENDIENTE';
        break;
    }

    return {
      ...solicitud,

      id: Number(solicitud.id ?? 0),

      solicitante:
        solicitud.solicitante ??
        solicitud.docente ??
        solicitud.nombreDocente ??
        'Docente no especificado',

      materia:
        solicitud.materia ??
        solicitud.nombreMateria ??
        null,

      laboratorio: solicitud.laboratorio ?? '',

      carrera: solicitud.carrera ?? '',

      fecha: solicitud.fecha ?? '',

      horaInicio: solicitud.horaInicio ?? '',

      horaFin: solicitud.horaFin ?? '',

      motivo: solicitud.motivo ?? '',

      numeroPersonas: Number(solicitud.numeroPersonas ?? 0),

      estatus,

      observaciones: solicitud.observaciones ?? null
    } as SolicitudCalendario;
  }

  // ==========================================
  // CONSULTAR SOLICITUDES
  // ==========================================

  cargarSolicitudes(): void {

    this.cargando = true;
    this.mensaje = '';

    this.solicitudService.listar()
      .pipe(
        timeout({ first: 10000 }),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: respuesta => {

          console.log('Solicitudes recibidas:', respuesta);

          const datos = Array.isArray(respuesta)
            ? respuesta as SolicitudCalendarioApi[]
            : [];

          this.solicitudes = datos.map(
            solicitud => this.normalizarSolicitud(solicitud)
          );

          this.generarCalendario();

          this.actualizarSolicitudesDelDia();

          this.cdr.detectChanges();
        },

        error: error => {

          console.error(
            'Error al consultar solicitudes:',
            error
          );

          this.solicitudes = [];
          this.calendarDays = [];
          this.solicitudesDelDia = [];

          if (error.name === 'TimeoutError') {

            this.mostrarMensaje(
              'El servidor tardó demasiado en responder.',
              'error'
            );

          } else if (error.status === 0) {

            this.mostrarMensaje(
              'No se pudo conectar con el servidor.',
              'error'
            );

          } else if (error.status === 401) {

            this.mostrarMensaje(
              'Tu sesión ha expirado. Inicia sesión nuevamente.',
              'error'
            );

          } else if (error.status === 403) {

            this.mostrarMensaje(
              'No tienes permisos para consultar las solicitudes.',
              'error'
            );

          } else {

            this.mostrarMensaje(
              error.error?.message ||
              `Error al cargar solicitudes. Código: ${error.status}`,
              'error'
            );
          }

          this.cdr.detectChanges();
        }

      });
  }

  // ==========================================
  // GENERAR CALENDARIO MENSUAL
  // ==========================================

  generarCalendario(): void {

    const anio = this.fechaActual.getFullYear();
    const mes = this.fechaActual.getMonth();

    const primerDia = new Date(anio, mes, 1);

    // Semana iniciando en lunes.
    const desplazamiento = (primerDia.getDay() + 6) % 7;

    const inicio = new Date(
      anio,
      mes,
      1 - desplazamiento
    );

    const dias: DiaCalendario[] = [];

    for (let i = 0; i < 42; i++) {

      const fecha = new Date(
        inicio.getFullYear(),
        inicio.getMonth(),
        inicio.getDate() + i
      );

      const fechaKey = this.formatearFecha(fecha);

      const solicitudes = this.solicitudes
        .filter(
          solicitud =>
            this.obtenerFechaKey(solicitud.fecha) === fechaKey
        )
        .filter(
          solicitud => this.cumpleFiltro(solicitud)
        );

      dias.push({

        fecha,

        fechaKey,

        numero: fecha.getDate(),

        perteneceMes: fecha.getMonth() === mes,

        esHoy: fechaKey === this.formatearFecha(this.hoy),

        seleccionado:
          fechaKey === this.formatearFecha(this.fechaSeleccionada),

        solicitudes

      });
    }

    this.calendarDays = dias;
  }

  // ==========================================
  // NAVEGACIÓN
  // ==========================================

  mesAnterior(): void {

    this.fechaActual = new Date(
      this.fechaActual.getFullYear(),
      this.fechaActual.getMonth() - 1,
      1
    );

    this.generarCalendario();
  }

  mesSiguiente(): void {

    this.fechaActual = new Date(
      this.fechaActual.getFullYear(),
      this.fechaActual.getMonth() + 1,
      1
    );

    this.generarCalendario();
  }

  irAHoy(): void {

    this.hoy = new Date();

    this.fechaActual = new Date(
      this.hoy.getFullYear(),
      this.hoy.getMonth(),
      1
    );

    this.fechaSeleccionada = new Date(
      this.hoy.getFullYear(),
      this.hoy.getMonth(),
      this.hoy.getDate()
    );

    this.generarCalendario();

    this.actualizarSolicitudesDelDia();
  }

  // ==========================================
  // SELECCIONAR DÍA
  // ==========================================

  seleccionarDia(dia: DiaCalendario): void {

    this.fechaSeleccionada = new Date(
      dia.fecha.getFullYear(),
      dia.fecha.getMonth(),
      dia.fecha.getDate()
    );

    if (!dia.perteneceMes) {

      this.fechaActual = new Date(
        dia.fecha.getFullYear(),
        dia.fecha.getMonth(),
        1
      );
    }

    this.generarCalendario();

    this.actualizarSolicitudesDelDia();
  }

  // ==========================================
  // SOLICITUDES DEL DÍA
  // ==========================================

  actualizarSolicitudesDelDia(): void {

    const fechaKey = this.formatearFecha(
      this.fechaSeleccionada
    );

    this.solicitudesDelDia = this.solicitudes
      .filter(
        solicitud =>
          this.obtenerFechaKey(solicitud.fecha) === fechaKey
      )
      .filter(
        solicitud => this.cumpleFiltro(solicitud)
      )
      .sort(
        (a, b) =>
          (a.horaInicio ?? '').localeCompare(b.horaInicio ?? '')
      );
  }

  get fechaSeleccionadaTexto(): string {

    return this.fechaSeleccionada.toLocaleDateString('es-MX', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  // ==========================================
  // FILTRO POR ESTATUS
  // ==========================================

  cambiarFiltro(estatus: string): void {

    this.filtroEstatus = estatus;

    this.generarCalendario();

    this.actualizarSolicitudesDelDia();
  }

  private cumpleFiltro(
    solicitud: SolicitudCalendario
  ): boolean {

    return this.filtroEstatus === 'TODOS' ||
      solicitud.estatus === this.filtroEstatus;
  }

  // ==========================================
  // FORMATO DE FECHAS
  // ==========================================

  private formatearFecha(fecha: Date): string {

    const anio = fecha.getFullYear();

    const mes = String(
      fecha.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      fecha.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  private obtenerFechaKey(fecha: string): string {

    if (!fecha) {
      return '';
    }

    // Evita desfases de zona horaria al recibir YYYY-MM-DD.
    return fecha.substring(0, 10);
  }

  // ==========================================
  // CLASES Y ESTATUS
  // ==========================================

  obtenerClaseEstatus(
    estatus: EstatusSolicitud
  ): string {

    switch (estatus) {

      case 'AUTORIZADA':
        return 'autorizada';

      case 'PENDIENTE':
        return 'pendiente';

      case 'RECHAZADA':
        return 'rechazada';

      default:
        return '';
    }
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
  }

}
