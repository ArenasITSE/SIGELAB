
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

import { SolicitudService } from '../../../solicitudes/services/solicitud.service';

interface DiaCalendario {
  fecha: Date;
  fechaKey: string;
  numero: number;
  perteneceMes: boolean;
  esHoy: boolean;
  seleccionado: boolean;
  solicitudes: SolicitudLaboratorio[];
}

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

  solicitudes: SolicitudLaboratorio[] = [];

  calendarDays: DiaCalendario[] = [];

  solicitudesDelDia: SolicitudLaboratorio[] = [];

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
    return this.fechaActual.toLocaleDateString('es-MX', {
      month: 'long',
      year: 'numeric'
    });
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalSolicitudes(): number {
    return this.solicitudes.length;
  }

  get totalAutorizadas(): number {
    return this.solicitudes.filter(
      s => s.estatus === 'AUTORIZADA'
    ).length;
  }

  get totalPendientes(): number {
    return this.solicitudes.filter(
      s => s.estatus === 'PENDIENTE'
    ).length;
  }

  get totalRechazadas(): number {
    return this.solicitudes.filter(
      s => s.estatus === 'RECHAZADA'
    ).length;
  }

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.cargarSolicitudes();
  }

  // ==========================================
  // CONSULTAR SOLICITUDES
  // ==========================================

  cargarSolicitudes(): void {

    this.cargando = true;
    this.mensaje = '';

    this.solicitudService.listar()
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta: SolicitudLaboratorio[]) => {

          console.log('Solicitudes recibidas:', respuesta);

          this.solicitudes = Array.isArray(respuesta)
            ? respuesta
            : [];

          this.generarCalendario();

          this.actualizarSolicitudesDelDia();

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error('Error al consultar solicitudes:', error);

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

    // Semana iniciando en lunes
    const desplazamiento = (primerDia.getDay() + 6) % 7;

    const inicio = new Date(anio, mes, 1 - desplazamiento);

    const dias: DiaCalendario[] = [];

    for (let i = 0; i < 42; i++) {

      const fecha = new Date(
        inicio.getFullYear(),
        inicio.getMonth(),
        inicio.getDate() + i
      );

      const fechaKey = this.formatearFecha(fecha);

      const solicitudes = this.solicitudes
        .filter(s => this.obtenerFechaKey(s.fecha) === fechaKey)
        .filter(s => this.cumpleFiltro(s));

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
      .filter(s => this.obtenerFechaKey(s.fecha) === fechaKey)
      .filter(s => this.cumpleFiltro(s))
      .sort((a, b) =>
        a.horaInicio.localeCompare(b.horaInicio)
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
    solicitud: SolicitudLaboratorio
  ): boolean {

    return this.filtroEstatus === 'TODOS' ||
      solicitud.estatus === this.filtroEstatus;
  }

  // ==========================================
  // FORMATO DE FECHAS
  // ==========================================

  private formatearFecha(fecha: Date): string {

    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  private obtenerFechaKey(fecha: string): string {

    if (!fecha) return '';

    // Evita desfases de zona horaria al recibir YYYY-MM-DD.
    return fecha.substring(0, 10);
  }

  // ==========================================
  // CLASES Y ESTATUS
  // ==========================================

  obtenerClaseEstatus(estatus: EstatusSolicitud): string {

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
