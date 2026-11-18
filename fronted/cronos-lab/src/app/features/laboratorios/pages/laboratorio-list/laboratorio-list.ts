
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import { forkJoin, finalize, switchMap, timeout } from 'rxjs';

import { environment } from '../../../../environments/environment';

import { LaboratorioService } from '../../services/laboratorio.service';

import {
  Laboratorio,
  LaboratorioRequest
} from '../../models/laboratorio.model';

// ==========================================
// INTERFACES PARA LOS CATÁLOGOS
// ==========================================

interface UsuarioOption {
  id: number;
  nombre: string;
  correo?: string;
  rol?: string;
  activo?: boolean;
}

interface CarreraOption {
  id: number;
  nombre: string;
  clave?: string;
  activo?: boolean;
}

@Component({
  selector: 'app-laboratorio-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './laboratorio-list.html',
  styleUrl: './laboratorio-list.scss'
})
export class LaboratorioListComponent implements OnInit {

  private laboratorioService = inject(LaboratorioService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // INFORMACIÓN
  // ==========================================

  laboratorios: Laboratorio[] = [];

  usuarios: UsuarioOption[] = [];
  carreras: CarreraOption[] = [];

  // ==========================================
  // BÚSQUEDA
  // ==========================================

  busqueda = '';

  // ==========================================
  // ESTADOS
  // ==========================================

  cargando = false;
  cargandoCatalogos = false;
  guardando = false;

  mostrarFormulario = false;
  editando = false;

  laboratorioId: number | null = null;
  procesandoId: number | null = null;

  // ==========================================
  // MENSAJES
  // ==========================================

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';

  // ==========================================
  // FORMULARIO
  // ==========================================

  formulario: LaboratorioRequest = this.formularioVacio();

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

ngOnInit(): void {
  this.listarLaboratorios();

  // Los docentes solo consultan laboratorios.
  // No necesitan cargar usuarios ni carreras.
  if (this.puedeGestionarLaboratorios) {
    this.cargarCatalogos();
  }
}
  // ==========================================
  // FORMULARIO VACÍO
  // ==========================================

  formularioVacio(): LaboratorioRequest {
    return {
      nombre: '',
      encargadoId: 0,
      carreraId: 0,
      ubicacion: '',
      capacidad: 1
    };
  }

  // ==========================================
  // CARGAR USUARIOS Y CARRERAS
  // ==========================================

  cargarCatalogos(): void {

    this.cargandoCatalogos = true;

    forkJoin({
      usuarios: this.http.get<UsuarioOption[]>(
        `${environment.apiUrl}/usuarios`
      ),

      carreras: this.http.get<CarreraOption[]>(
        `${environment.apiUrl}/carreras`
      )
    })
      .pipe(
        timeout({ first: 15000 }),
        finalize(() => {
          this.cargandoCatalogos = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta) => {

          this.usuarios = respuesta.usuarios.filter(
            usuario => usuario.activo !== false
          );

          this.carreras = respuesta.carreras.filter(
            carrera => carrera.activo !== false
          );

          console.log('Usuarios cargados:', this.usuarios);
          console.log('Carreras cargadas:', this.carreras);

        },

        error: (error) => {

          console.error(
            'Error al cargar usuarios y carreras:',
            error
          );

          this.mostrarMensaje(
            'No fue posible cargar los usuarios y las carreras. Verifica los endpoints y los permisos.',
            'error'
          );

        }
      });
  }

  // ==========================================
  // FILTRO DE LABORATORIOS
  // ==========================================

  get laboratoriosFiltrados(): Laboratorio[] {

    const texto = this.busqueda.toLowerCase().trim();

    return this.laboratorios.filter(lab =>

      (lab.nombre ?? '').toLowerCase().includes(texto) ||

      (lab.carrera ?? '').toLowerCase().includes(texto) ||

      (lab.encargado ?? '').toLowerCase().includes(texto) ||

      (lab.ubicacion ?? '').toLowerCase().includes(texto)

    );
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalDisponibles(): number {

    return this.laboratorios.filter(
      laboratorio => laboratorio.estatus === 'DISPONIBLE'
    ).length;

  }

  get totalMantenimiento(): number {

    return this.laboratorios.filter(
      laboratorio => laboratorio.estatus === 'MANTENIMIENTO'
    ).length;

  }

  get totalInactivos(): number {

    return this.laboratorios.filter(
      laboratorio => laboratorio.estatus === 'INACTIVO'
    ).length;

  }

  // ==========================================
  // ACTUALIZAR ARREGLO Y VISTA
  // ==========================================
private actualizarLaboratorios(data: Laboratorio[]): void {
  if (!Array.isArray(data)) {
    console.error('La API no devolvió un arreglo válido.');
    return;
  }

  // El docente únicamente visualiza laboratorios disponibles.
  this.laboratorios = this.esDocente
    ? data.filter(lab => lab.estatus === 'DISPONIBLE')
    : [...data];

  console.log('Laboratorios visibles:', this.laboratorios);

  this.cdr.detectChanges();
}

  // ==========================================
  // LISTAR LABORATORIOS
  // ==========================================

  listarLaboratorios(): void {

    this.cargando = true;

    this.laboratorioService.listar()
      .pipe(
        timeout({ first: 10000 }),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data) => {

          console.log('Respuesta API:', data);

          if (!Array.isArray(data)) {
            this.mostrarMensaje(
              'La respuesta del servidor no es válida.',
              'error'
            );
            return;
          }

          this.actualizarLaboratorios(data);
        },

        error: (error) => {

          console.error(
            'Error al consultar laboratorios:',
            error
          );

          this.mostrarMensaje(
            error.name === 'TimeoutError'
              ? 'El servidor tardó demasiado en responder.'
              : error.error?.message ||
                'Error al consultar los laboratorios.',
            'error'
          );
        }
      });
  }

  // ==========================================
  // NUEVO LABORATORIO
  // ==========================================

  nuevoLaboratorio(): void {

if (!this.puedeGestionarLaboratorios) {
  this.mostrarMensaje(
    'No tienes permisos para registrar laboratorios.',
    'error'
  );
  return;
}

    if (this.guardando || this.procesandoId !== null) {
      return;
    }

    if (this.usuarios.length === 0 || this.carreras.length === 0) {
      this.cargarCatalogos();

      this.mostrarMensaje(
        'Espera a que se carguen los usuarios y las carreras.',
        'error'
      );

      return;
    }

    this.formulario = this.formularioVacio();

    this.laboratorioId = null;
    this.editando = false;
    this.mostrarFormulario = true;

    this.mensaje = '';
  }

  // ==========================================
  // EDITAR LABORATORIO
  // ==========================================

  editarLaboratorio(laboratorio: Laboratorio): void {


    if (!this.puedeGestionarLaboratorios) {
  return;
}
    if (this.procesandoId !== null || this.guardando) {
      return;
    }

    if (laboratorio.estatus === 'INACTIVO') {

      this.mostrarMensaje(
        'No puedes editar un laboratorio inactivo.',
        'error'
      );

      return;
    }

    this.formulario = {
      nombre: laboratorio.nombre,
      encargadoId: laboratorio.encargadoId,
      carreraId: laboratorio.carreraId,
      ubicacion: laboratorio.ubicacion,
      capacidad: laboratorio.capacidad
    };

    this.laboratorioId = laboratorio.id;

    this.editando = true;
    this.mostrarFormulario = true;

    this.mensaje = '';
  }

  // ==========================================
  // GUARDAR LABORATORIO
  // ==========================================

  guardarLaboratorio(): void {

    if (this.guardando || this.procesandoId !== null) {
      return;
    }

    if (
      !this.formulario.nombre.trim() ||
      !this.formulario.ubicacion.trim() ||
      this.formulario.encargadoId <= 0 ||
      this.formulario.carreraId <= 0 ||
      this.formulario.capacidad <= 0
    ) {

      this.mostrarMensaje(
        'Completa todos los campos con valores válidos.',
        'error'
      );

      return;
    }

    this.guardando = true;

    const esEdicion =
      this.editando && this.laboratorioId !== null;

    const operacion = esEdicion
      ? this.laboratorioService.actualizar(
          this.laboratorioId!,
          this.formulario
        )
      : this.laboratorioService.crear(this.formulario);

    operacion.pipe(

      switchMap(() => this.laboratorioService.listar()),

      timeout({ first: 10000 }),

      finalize(() => {
        this.guardando = false;
        this.cdr.detectChanges();
      })

    ).subscribe({

      next: (laboratoriosActualizados) => {

        if (!Array.isArray(laboratoriosActualizados)) {
          this.mostrarMensaje(
            'No se pudo actualizar el listado.',
            'error'
          );
          return;
        }

        this.actualizarLaboratorios(laboratoriosActualizados);

        this.mostrarFormulario = false;
        this.formulario = this.formularioVacio();

        this.laboratorioId = null;
        this.editando = false;
        this.busqueda = '';

        this.mostrarMensaje(
          esEdicion
            ? 'Laboratorio actualizado correctamente.'
            : 'Laboratorio registrado correctamente.',
          'success'
        );
      },

      error: (error) => {

        console.error(
          'Error al guardar o actualizar:',
          error
        );

        this.mostrarMensaje(
          error.error?.message ||
          'No fue posible completar la operación.',
          'error'
        );
      }
    });
  if (!this.puedeGestionarLaboratorios) {
  return;
}
  }

  

  // ==========================================
  // DESACTIVAR LABORATORIO
  // ==========================================

  eliminarLaboratorio(laboratorio: Laboratorio): void {

    if (this.procesandoId !== null || this.guardando) {
      return;
    }

    if (laboratorio.estatus === 'INACTIVO') {

      this.mostrarMensaje(
        'El laboratorio ya se encuentra inactivo.',
        'error'
      );

      return;
    }

    const confirmar = confirm(
      `¿Deseas desactivar el laboratorio "${laboratorio.nombre}"?\n\n` +
      'El registro se conservará en el sistema.'
    );

    if (!confirmar) {
      return;
    }

    this.procesandoId = laboratorio.id;

    this.laboratorioService.eliminar(laboratorio.id)
      .pipe(
        switchMap(() => this.laboratorioService.listar()),
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
              'No se pudo actualizar el listado.',
              'error'
            );
            return;
          }

          this.actualizarLaboratorios(data);

          this.mostrarMensaje(
            'Laboratorio desactivado correctamente.',
            'success'
          );

          console.log(
            'Laboratorio desactivado:',
            laboratorio.id
          );
        },

        error: (error) => {

          console.error(
            'Error al desactivar laboratorio:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No fue posible desactivar el laboratorio.',
            'error'
          );
        }
      });
      if (!this.puedeGestionarLaboratorios) {
  return;
}
  }

  // ==========================================
  // ELIMINAR DEFINITIVAMENTE
  // ==========================================

  eliminarDefinitivamente(laboratorio: Laboratorio): void {

    if (this.procesandoId !== null || this.guardando) {
      return;
    }

    if (laboratorio.estatus !== 'INACTIVO') {

      this.mostrarMensaje(
        'Solo puedes eliminar definitivamente laboratorios inactivos.',
        'error'
      );

      return;
    }

    const confirmar = confirm(
      `¿Deseas eliminar definitivamente el laboratorio "${laboratorio.nombre}"?\n\n` +
      'Esta acción no se puede deshacer.'
    );

    if (!confirmar) {
      return;
    }

    this.procesandoId = laboratorio.id;

    this.laboratorioService
      .eliminarDefinitivamente(laboratorio.id)
      .pipe(
        switchMap(() => this.laboratorioService.listar()),
        timeout({ first: 10000 }),
        finalize(() => {
          this.procesandoId = null;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta) => {

          if (!Array.isArray(respuesta)) {
            this.mostrarMensaje(
              'La respuesta del listado no es válida.',
              'error'
            );
            return;
          }

          this.actualizarLaboratorios(respuesta);

          this.mostrarMensaje(
            'Laboratorio eliminado definitivamente.',
            'success'
          );

          console.log(
            'Laboratorio eliminado:',
            laboratorio.id
          );

          console.log(
            'Total restante:',
            this.laboratorios.length
          );
        },

        error: (error) => {

          console.error(
            'Error al eliminar laboratorio:',
            error
          );

          this.mostrarMensaje(
            error.error?.message ||
            'No se pudo eliminar el laboratorio. Verifica si tiene solicitudes relacionadas.',
            'error'
          );
        }
      });
      if (!this.puedeGestionarLaboratorios) {
  return;
}
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

    this.laboratorioId = null;

    this.editando = false;
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

  // ==========================================
  // TRADUCIR ESTATUS
  // ==========================================

  nombreEstatus(estatus: string): string {

    const nombres: Record<string, string> = {
      DISPONIBLE: 'Disponible',
      MANTENIMIENTO: 'Mantenimiento',
      INACTIVO: 'Inactivo'
    };

    return nombres[estatus] ?? estatus;
  }

  get rolUsuario(): string {
  return (localStorage.getItem('rol') || '').toUpperCase();
}

get esDocente(): boolean {
  return (localStorage.getItem('rol') || '').toUpperCase() === 'DOCENTE';
}

get puedeGestionarLaboratorios(): boolean {
  const rol = (localStorage.getItem('rol') || '').toUpperCase();

  return rol === 'ADMINISTRADOR' || rol === 'COORDINADOR';
}

get esAdministrador(): boolean {
  return this.rolUsuario === 'ADMINISTRADOR';
}



}
