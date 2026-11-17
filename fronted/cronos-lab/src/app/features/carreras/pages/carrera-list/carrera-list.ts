
import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, timeout } from 'rxjs';

import { Carrera, CarreraRequest } from '../../models/carrera.model';
import { CarreraService } from '../../services/carrera.service';

@Component({
  selector: 'app-carrera-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './carrera-list.html',
  styleUrl: './carrera-list.scss'
})
export class CarreraListComponent implements OnInit {

  private carreraService = inject(CarreraService);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // DATOS
  // ==========================================

  carreras: Carrera[] = [];
  carrerasFiltradas: Carrera[] = [];

  // ==========================================
  // BÚSQUEDA
  // ==========================================

  busqueda = '';

  // ==========================================
  // FORMULARIO
  // ==========================================

  mostrarFormulario = false;
  modoEdicion = false;

  carreraId: number | null = null;

  formulario: CarreraRequest = {
    clave: '',
    nombre: ''
  };

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;
  procesando = false;

  // ==========================================
  // MENSAJES
  // ==========================================

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';

  // ==========================================
  // USUARIO Y PERMISOS
  // ==========================================

  rol = localStorage.getItem('rol') ?? '';

  get esAdministrador(): boolean {
    return this.rol === 'ADMINISTRADOR';
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalCarreras(): number {
    return this.carreras.length;
  }

  get carrerasActivas(): number {
    return this.carreras.filter(c => c.activo === true).length;
  }

  get carrerasInactivas(): number {
    return this.carreras.filter(c => c.activo === false).length;
  }

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.cargarCarreras();
  }

  // ==========================================
  // CONSULTAR CARRERAS
  // ==========================================

  cargarCarreras(): void {

    this.cargando = true;
    this.mensaje = '';

    this.cdr.detectChanges();

    console.log('SIGELAB - CONSULTA DE CARRERAS');

    this.carreraService.listar()
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta: Carrera[]) => {

          console.log('Respuesta del servidor:', respuesta);

          if (!Array.isArray(respuesta)) {

            console.error('Formato de respuesta incorrecto');

            this.carreras = [];
            this.carrerasFiltradas = [];

            this.mostrarMensaje(
              'El servidor devolvió un formato de datos incorrecto.',
              'error'
            );

            this.cdr.detectChanges();
            return;
          }

          this.carreras = respuesta;

          this.filtrarCarreras();

          console.log('Carreras cargadas:', this.carreras.length);

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error('Error al consultar carreras:', error);

          this.carreras = [];
          this.carrerasFiltradas = [];

          if (error.name === 'TimeoutError') {

            this.mostrarMensaje(
              'El servidor tardó demasiado en responder.',
              'error'
            );

          } else if (error.status === 0) {

            this.mostrarMensaje(
              'No se pudo establecer conexión con el servidor.',
              'error'
            );

          } else if (error.status === 401) {

            this.mostrarMensaje(
              'Tu sesión ha expirado. Inicia sesión nuevamente.',
              'error'
            );

          } else if (error.status === 403) {

            this.mostrarMensaje(
              'No tienes permisos para consultar las carreras.',
              'error'
            );

          } else {

            this.mostrarMensaje(
              error.error?.message ||
              `Error al consultar carreras. Código: ${error.status}`,
              'error'
            );
          }

          this.cdr.detectChanges();
        }

      });
  }

  // ==========================================
  // FILTRAR CARRERAS
  // ==========================================

  filtrarCarreras(): void {

    const termino = this.busqueda.trim().toLowerCase();

    this.carrerasFiltradas = this.carreras.filter(carrera => {

      const clave = carrera.clave?.toLowerCase() ?? '';
      const nombre = carrera.nombre?.toLowerCase() ?? '';

      return clave.includes(termino) ||
             nombre.includes(termino);

    });
  }

  // ==========================================
  // ABRIR FORMULARIO
  // ==========================================

  abrirFormulario(): void {

    if (!this.esAdministrador) return;

    this.limpiarFormulario();

    this.modoEdicion = false;
    this.mostrarFormulario = true;
    this.mensaje = '';

  }

  // ==========================================
  // EDITAR CARRERA
  // ==========================================

  editarCarrera(carrera: Carrera): void {

    if (!this.esAdministrador || !carrera.activo) return;

    this.modoEdicion = true;
    this.carreraId = carrera.id;

    this.formulario = {
      clave: carrera.clave,
      nombre: carrera.nombre
    };

    this.mostrarFormulario = true;
    this.mensaje = '';

  }

  // ==========================================
  // GUARDAR CARRERA
  // ==========================================

  guardarCarrera(): void {

    if (!this.esAdministrador || this.procesando) return;

    const clave = this.formulario.clave.trim().toUpperCase();
    const nombre = this.formulario.nombre.trim();

    if (!clave || !nombre) {

      this.mostrarMensaje(
        'Completa todos los campos.',
        'error'
      );

      return;
    }

    const request: CarreraRequest = {
      clave,
      nombre
    };

    this.procesando = true;

    const peticion =
      this.modoEdicion && this.carreraId !== null
        ? this.carreraService.actualizar(this.carreraId, request)
        : this.carreraService.crear(request);

    peticion
      .pipe(
        timeout(10000),
        finalize(() => {
          this.procesando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: () => {

          this.cancelarFormulario();

          this.mostrarMensaje(
            this.modoEdicion
              ? 'Carrera actualizada correctamente.'
              : 'Carrera registrada correctamente.',
            'success'
          );

          this.cargarCarreras();

        },

        error: (error) => {

          console.error('Error al guardar carrera:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible guardar la carrera. Verifica los datos.',
            'error'
          );

          this.cdr.detectChanges();
        }

      });
  }

  // ==========================================
  // DESACTIVAR CARRERA
  // ==========================================

  desactivarCarrera(carrera: Carrera): void {

    if (!this.esAdministrador || this.procesando) return;

    if (!carrera.activo) return;

    const confirmar = window.confirm(
      `¿Deseas desactivar la carrera "${carrera.nombre}"?`
    );

    if (!confirmar) return;

    this.procesando = true;

    this.carreraService.desactivar(carrera.id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.procesando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: () => {

          this.mostrarMensaje(
            'Carrera desactivada correctamente.',
            'success'
          );

          this.cargarCarreras();

        },

        error: (error) => {

          console.error('Error al desactivar carrera:', error);

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible desactivar la carrera.',
            'error'
          );

          this.cdr.detectChanges();
        }

      });
  }

  // ==========================================
  // CANCELAR FORMULARIO
  // ==========================================

  cancelarFormulario(): void {

    this.mostrarFormulario = false;
    this.limpiarFormulario();

  }

  // ==========================================
  // LIMPIAR FORMULARIO
  // ==========================================

  limpiarFormulario(): void {

    this.carreraId = null;

    this.formulario = {
      clave: '',
      nombre: ''
    };

  }

  // ==========================================
  // MOSTRAR MENSAJES
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
