
import {
  Component,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, timeout } from 'rxjs';

import { MateriaService } from '../../services/materia.service';
import { Materia, MateriaRequest } from '../../models/materia.model';

import { CarreraService } from '../../../carreras/services/carrera.service';

interface CarreraOption {
  id: number;
  nombre: string;
}

@Component({
  selector: 'app-materias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './materias.html',
  styleUrl: './materias.scss'
})
export class MateriasComponent implements OnInit {

  materias: Materia[] = [];
  carreras: CarreraOption[] = [];

  materiaSeleccionada: Materia | null = null;

  formulario: MateriaRequest = this.formularioInicial();

  mostrarFormulario = false;
  cargando = false;
  guardando = false;
  cargandoCarreras = false;
  editando = false;

  mensaje = '';
  error = '';

  rol = localStorage.getItem('rol') ?? '';

  constructor(
    private materiaService: MateriaService,
    private carreraService: CarreraService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarMaterias();

    if (this.rol === 'ADMINISTRADOR') {
      this.cargarCarreras();
    }
  }

  // =====================================
  // FORMULARIO INICIAL
  // =====================================

  formularioInicial(): MateriaRequest {
    return {
      clave: '',
      nombre: '',
      semestre: 1,
      horasSemana: 4,
      carreraId: undefined
    };
  }

  // =====================================
  // CARGAR MATERIAS
  // =====================================

  cargarMaterias(): void {

    this.cargando = true;
    this.error = '';

    this.materiaService.listar()
      .pipe(
        timeout(15000),
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta) => {

          console.log('Materias recibidas:', respuesta);

          this.materias = Array.isArray(respuesta)
            ? respuesta
            : [];

          this.cdr.detectChanges();

        },

        error: (err) => {

          console.error('Error al consultar materias:', err);

          this.materias = [];

          if (err.name === 'TimeoutError') {
            this.error = 'El servidor tardó demasiado en responder.';
          } else if (err.status === 0) {
            this.error = 'No se pudo conectar con el servidor.';
          } else if (err.status === 401) {
            this.error = 'Tu sesión ha expirado. Inicia sesión nuevamente.';
          } else if (err.status === 403) {
            this.error = 'No tienes permisos para consultar las materias.';
          } else {
            this.error = err.error?.message ||
              'No fue posible cargar las materias.';
          }

          this.cdr.detectChanges();

        }

      });
  }

  // =====================================
  // CARGAR CARRERAS
  // =====================================

  cargarCarreras(): void {

    this.cargandoCarreras = true;

    this.carreraService.listar()
      .pipe(
        finalize(() => {
          this.cargandoCarreras = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (respuesta) => {

          console.log('Carreras recibidas:', respuesta);

          this.carreras = Array.isArray(respuesta)
            ? respuesta
            : [];

          if (this.carreras.length === 0) {
            this.error = 'No hay carreras registradas para seleccionar.';
          }

          this.cdr.detectChanges();

        },

        error: (err) => {

          console.error('Error al cargar carreras:', err);

          this.error = err.error?.message ||
            'No fue posible cargar las carreras.';

          this.cdr.detectChanges();

        }

      });
  }

  // =====================================
  // NUEVA MATERIA
  // =====================================

  nuevaMateria(): void {

    this.formulario = this.formularioInicial();
    this.materiaSeleccionada = null;

    this.editando = false;
    this.mostrarFormulario = true;

    this.mensaje = '';
    this.error = '';
  }

  // =====================================
  // EDITAR MATERIA
  // =====================================

  editarMateria(materia: Materia): void {

    this.materiaSeleccionada = materia;

    this.formulario = {
      clave: materia.clave,
      nombre: materia.nombre,
      semestre: materia.semestre,
      horasSemana: materia.horasSemana,
      carreraId: materia.carreraId
    };

    this.editando = true;
    this.mostrarFormulario = true;

    this.mensaje = '';
    this.error = '';
  }

  // =====================================
  // GUARDAR MATERIA
  // =====================================

  guardarMateria(): void {

    this.error = '';
    this.mensaje = '';

    if (
      !this.formulario.clave.trim() ||
      !this.formulario.nombre.trim() ||
      !this.formulario.semestre ||
      !this.formulario.horasSemana
    ) {
      this.error = 'Completa todos los campos obligatorios.';
      return;
    }

    if (
      this.rol === 'ADMINISTRADOR' &&
      !this.formulario.carreraId
    ) {
      this.error = 'Debes seleccionar una carrera.';
      return;
    }

    this.guardando = true;

    const operacion =
      this.editando && this.materiaSeleccionada
        ? this.materiaService.actualizar(
            this.materiaSeleccionada.id,
            this.formulario
          )
        : this.materiaService.crear(this.formulario);

    operacion
      .pipe(
        finalize(() => {
          this.guardando = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: () => {

          this.mensaje = this.editando
            ? 'Materia actualizada correctamente.'
            : 'Materia registrada correctamente.';

          this.cerrarFormulario();
          this.cargarMaterias();

        },

        error: (err) => {

          console.error('Error al guardar materia:', err);

          this.error = err.error?.message ||
            'Ocurrió un error al guardar la materia.';

        }

      });
  }

  // =====================================
  // DESACTIVAR MATERIA
  // =====================================

  eliminarMateria(materia: Materia): void {

    const confirmar = confirm(
      `¿Deseas desactivar la materia ${materia.nombre}?`
    );

    if (!confirmar) return;

    this.materiaService.eliminar(materia.id)
      .subscribe({

        next: () => {

          this.mensaje = 'Materia desactivada correctamente.';
          this.error = '';

          this.cargarMaterias();

        },

        error: (err) => {

          console.error('Error al desactivar materia:', err);

          this.error = err.error?.message ||
            'No fue posible desactivar la materia.';

        }

      });
  }

  // =====================================
  // REACTIVAR MATERIA
  // =====================================

  activarMateria(materia: Materia): void {

    const confirmar = confirm(
      `¿Deseas volver a activar la materia ${materia.nombre}?`
    );

    if (!confirmar) return;

    this.materiaService.activar(materia.id)
      .subscribe({

        next: () => {

          this.mensaje = 'Materia activada correctamente.';
          this.error = '';

          this.cargarMaterias();

        },

        error: (err) => {

          console.error('Error al activar materia:', err);

          this.error = err.error?.message ||
            'No fue posible activar la materia.';

        }

      });
  }

  // =====================================
  // CERRAR FORMULARIO
  // =====================================

  cerrarFormulario(): void {

    this.mostrarFormulario = false;
    this.editando = false;

    this.materiaSeleccionada = null;
    this.formulario = this.formularioInicial();

  }

}
