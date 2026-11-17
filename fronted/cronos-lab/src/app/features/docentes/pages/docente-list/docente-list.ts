
import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, timeout } from 'rxjs';

import { Usuario } from '../../../usuarios/models/usuario.model';
import { UsuarioService } from '../../../usuarios/services/usuario.service';

@Component({
  selector: 'app-docente-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './docente-list.html',
  styleUrl: './docente-list.scss'
})
export class DocenteListComponent implements OnInit {

  private usuarioService = inject(UsuarioService);
  private cdr = inject(ChangeDetectorRef);

  docentes: Usuario[] = [];
  docentesFiltrados: Usuario[] = [];

  busqueda = '';
  filtroEstatus = 'TODOS';

  cargando = false;
  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';

  docenteSeleccionado: Usuario | null = null;
  mostrarDetalle = false;

  // Estadísticas
  get totalDocentes(): number {
    return this.docentes.length;
  }

  get docentesActivos(): number {
    return this.docentes.filter(d => d.activo).length;
  }

  get docentesInactivos(): number {
    return this.docentes.filter(d => !d.activo).length;
  }

  ngOnInit(): void {
    this.cargarDocentes();
  }

  // ==========================================
  // CONSULTAR DOCENTES
  // ==========================================

  cargarDocentes(): void {

    this.cargando = true;
    this.mensaje = '';

    this.usuarioService.listar()
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (usuarios: Usuario[]) => {

          console.log('Usuarios recibidos:', usuarios);

          this.docentes = Array.isArray(usuarios)
            ? usuarios.filter(usuario => usuario.rol === 'DOCENTE')
            : [];

          this.filtrarDocentes();

          console.log('Docentes encontrados:', this.docentes.length);

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error('Error al consultar docentes:', error);

          this.docentes = [];
          this.docentesFiltrados = [];

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
              'No tienes permisos para consultar los docentes.',
              'error'
            );
          } else {
            this.mostrarMensaje(
              error.error?.message ||
              `Error al consultar docentes. Código: ${error.status}`,
              'error'
            );
          }

          this.cdr.detectChanges();
        }

      });
  }

  // ==========================================
  // FILTRAR DOCENTES
  // ==========================================

  filtrarDocentes(): void {

    const termino = this.busqueda.trim().toLowerCase();

    this.docentesFiltrados = this.docentes.filter(docente => {

      const coincideBusqueda =
        (docente.nombre ?? '').toLowerCase().includes(termino) ||
        (docente.correo ?? '').toLowerCase().includes(termino) ||
        (docente.carrera ?? '').toLowerCase().includes(termino);

      const coincideEstatus =
        this.filtroEstatus === 'TODOS' ||
        (this.filtroEstatus === 'ACTIVO' && docente.activo) ||
        (this.filtroEstatus === 'INACTIVO' && !docente.activo);

      return coincideBusqueda && coincideEstatus;
    });
  }

  // ==========================================
  // VER DETALLE
  // ==========================================

  verDetalle(docente: Usuario): void {
    this.docenteSeleccionado = docente;
    this.mostrarDetalle = true;
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.docenteSeleccionado = null;
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
