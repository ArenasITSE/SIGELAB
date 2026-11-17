
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
  switchMap,
  timeout
} from 'rxjs';

import { environment } from '../../../../environments/environment';

import { SolicitudService } from '../../services/solicitud.service';

import {
  SolicitudLaboratorio,
  SolicitudRequest,
  EstatusSolicitud,
  LaboratorioOption
} from '../../models/solicitud.model';

@Component({
  selector: 'app-solicitud-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitud-list.html',
  styleUrl: './solicitud-list.scss'
})
export class SolicitudListComponent implements OnInit {

  private solicitudService = inject(SolicitudService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // INFORMACIÓN
  // ==========================================

  solicitudes: SolicitudLaboratorio[] = [];
  laboratorios: LaboratorioOption[] = [];

  // ==========================================
  // USUARIO Y PERMISOS
  // ==========================================

  rol = localStorage.getItem('rol') ?? '';

  get puedeGestionar(): boolean {
    return [
      'ADMINISTRADOR',
      'COORDINADOR'
    ].includes(this.rol);
  }

  get puedeCrear(): boolean {
    return [
      'DOCENTE',
      'ADMINISTRADOR',
      'COORDINADOR'
    ].includes(this.rol);
  }

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;
  cargandoLaboratorios = false;
  guardando = false;
  procesandoId: number | null = null;

  mostrarFormulario = false;
  mostrarRechazo = false;

  solicitudSeleccionada: SolicitudLaboratorio | null = null;

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

  ngOnInit(): void {
    this.listarSolicitudes();
    this.cargarLaboratorios();
  }

  // ==========================================
  // FORMULARIO VACÍO
  // ==========================================

  formularioVacio(): SolicitudRequest {
    return {
      laboratorioId: 0,
      fecha: '',
      horaInicio: '',
      horaFin: '',
      numeroPersonas: 1,
      motivo: ''
    };
  }

  // ==========================================
  // FECHA LOCAL
  // ==========================================

  private obtenerFechaLocal(): string {
    const fecha = new Date();
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
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

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta de laboratorios no es válida.',
              'error'
            );
            return;
          }

          this.laboratorios = data.filter(
            laboratorio =>
              laboratorio.estatus === 'DISPONIBLE'
          );

          console.log(
            'Laboratorios disponibles:',
            this.laboratorios
          );
        },

        error: (error) => {

          console.error(
            'Error al cargar laboratorios:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible cargar los laboratorios.',
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

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta de solicitudes no es válida.',
              'error'
            );
            return;
          }

          this.solicitudes = [...data];

          console.log(
            'Solicitudes cargadas:',
            this.solicitudes
          );
        },

        error: (error) => {

          console.error(
            'Error al consultar solicitudes:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible consultar las solicitudes.',
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
        (solicitud.laboratorio ?? '').toLowerCase().includes(texto) ||
        (solicitud.solicitante ?? '').toLowerCase().includes(texto) ||
        (solicitud.carrera ?? '').toLowerCase().includes(texto) ||
        (solicitud.motivo ?? '').toLowerCase().includes(texto);

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
      s => s.estatus === 'PENDIENTE'
    ).length;
  }

  get totalAutorizadas(): number {
    return this.solicitudes.filter(
      s => s.estatus === 'AUTORIZADA'
    ).length;
  }

  get totalRechazadas(): number {
    return this.solicitudes.filter(
      s => s.estatus === 'RECHAZADA'
    ).length;
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

    if (this.cargandoLaboratorios) {
      this.mostrarMensaje(
        'Espera a que se carguen los laboratorios.',
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

    this.formulario = this.formularioVacio();
    this.mostrarFormulario = true;
    this.mensaje = '';
  }

  // ==========================================
  // LABORATORIO SELECCIONADO
  // ==========================================

  get laboratorioSeleccionado(): LaboratorioOption | undefined {

    return this.laboratorios.find(
      laboratorio =>
        laboratorio.id === Number(this.formulario.laboratorioId)
    );
  }

  // ==========================================
  // VALIDAR HORARIO
  // ==========================================

  get horarioValido(): boolean {

    if (!this.formulario.horaInicio || !this.formulario.horaFin) {
      return false;
    }

    return this.formulario.horaFin > this.formulario.horaInicio;
  }

  // ==========================================
  // GUARDAR SOLICITUD
  // ==========================================

  guardarSolicitud(): void {

    if (this.guardando) {
      return;
    }

    const laboratorio = this.laboratorioSeleccionado;

    if (
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

    if (this.formulario.fecha < this.fechaMinima) {
      this.mostrarMensaje(
        'No puedes solicitar una fecha anterior a hoy.',
        'error'
      );
      return;
    }

    if (!this.horarioValido) {
      this.mostrarMensaje(
        'La hora de finalización debe ser posterior a la hora de inicio.',
        'error'
      );
      return;
    }

    if (!laboratorio) {
      this.mostrarMensaje(
        'Selecciona un laboratorio válido.',
        'error'
      );
      return;
    }

    if (
      this.formulario.numeroPersonas < 1 ||
      this.formulario.numeroPersonas > laboratorio.capacidad
    ) {
      this.mostrarMensaje(
        `La capacidad máxima de este laboratorio es de ${laboratorio.capacidad} personas.`,
        'error'
      );
      return;
    }

    this.guardando = true;

    const request: SolicitudRequest = {
      laboratorioId: Number(this.formulario.laboratorioId),
      fecha: this.formulario.fecha,
      horaInicio: this.formulario.horaInicio,
      horaFin: this.formulario.horaFin,
      numeroPersonas: Number(this.formulario.numeroPersonas),
      motivo: this.formulario.motivo.trim()
    };

    this.solicitudService.crear(request)
      .pipe(
        switchMap(() => this.solicitudService.listar()),
        timeout({ first: 15000 }),
        finalize(() => {
          this.guardando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'No se pudo actualizar el listado.',
              'error'
            );
            return;
          }

          this.solicitudes = [...data];
          this.formulario = this.formularioVacio();
          this.mostrarFormulario = false;

          this.mostrarMensaje(
            'Solicitud registrada correctamente.',
            'success'
          );

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error al registrar solicitud:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible registrar la solicitud.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // AUTORIZAR SOLICITUD
  // ==========================================

  autorizarSolicitud(solicitud: SolicitudLaboratorio): void {

    if (!this.puedeGestionar) {
      this.mostrarMensaje(
        'No tienes permisos para autorizar solicitudes.',
        'error'
      );
      return;
    }

    if (solicitud.estatus !== 'PENDIENTE') {
      return;
    }

    if (this.procesandoId !== null || this.guardando) {
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
        switchMap(() => this.solicitudService.listar()),
        timeout({ first: 15000 }),
        finalize(() => {
          this.procesandoId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          this.solicitudes = [...data];

          this.mostrarMensaje(
            'Solicitud autorizada correctamente.',
            'success'
          );
        },

        error: (error) => {

          console.error(
            'Error al autorizar solicitud:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible autorizar la solicitud. Verifica la disponibilidad del laboratorio.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // ABRIR RECHAZO
  // ==========================================

  abrirRechazo(solicitud: SolicitudLaboratorio): void {

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
        switchMap(() => this.solicitudService.listar()),
        timeout({ first: 15000 }),
        finalize(() => {
          this.procesandoId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          this.solicitudes = [...data];

          this.mostrarRechazo = false;
          this.solicitudSeleccionada = null;
          this.observacionesRechazo = '';

          this.mostrarMensaje(
            'Solicitud rechazada correctamente.',
            'success'
          );
        },

        error: (error) => {

          console.error(
            'Error al rechazar solicitud:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible rechazar la solicitud.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // CERRAR FORMULARIOS
  // ==========================================

  cancelarFormulario(): void {

    if (this.guardando) {
      return;
    }

    this.mostrarFormulario = false;
    this.formulario = this.formularioVacio();
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

  nombreEstatus(estatus: EstatusSolicitud): string {

    const nombres: Record<EstatusSolicitud, string> = {
      PENDIENTE: 'Pendiente',
      AUTORIZADA: 'Autorizada',
      RECHAZADA: 'Rechazada'
    };

    return nombres[estatus];
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

}
