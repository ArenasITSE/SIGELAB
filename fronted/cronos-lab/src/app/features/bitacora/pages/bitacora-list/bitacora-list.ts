
import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, timeout } from 'rxjs';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

import { BitacoraService } from '../../services/bitacora.service';
import { Bitacora } from '../../models/bitacora.model';

@Component({
  selector: 'app-bitacora-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bitacora-list.html',
  styleUrl: './bitacora-list.scss'
})
export class BitacoraListComponent implements OnInit {

  private bitacoraService = inject(BitacoraService);
  private cdr = inject(ChangeDetectorRef);

  // ==========================================
  // DATOS
  // ==========================================

  registros: Bitacora[] = [];
  registrosFiltrados: Bitacora[] = [];

  // ==========================================
  // FILTROS
  // ==========================================

  busqueda = '';
  filtroFecha = '';

  // ==========================================
  // ESTADO
  // ==========================================

  cargando = false;
  mensaje = '';
  tipoMensaje: 'success' | 'error' | 'info' = 'info';

  // ==========================================
  // PAGINACIÓN
  // ==========================================

  paginaActual = 1;
  registrosPorPagina = 10;

  // ==========================================
  // MODAL
  // ==========================================

  registroSeleccionado: Bitacora | null = null;

  // ==========================================
  // INICIALIZACIÓN
  // ==========================================

  ngOnInit(): void {
    this.cargarRegistros();
  }

  // ==========================================
  // CARGAR REGISTROS
  // ==========================================

  cargarRegistros(): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;
    this.mensaje = '';

    this.cdr.detectChanges();

    console.log('Consultando bitácora...');

    this.bitacoraService.listar()
      .pipe(
        timeout(15000),
        finalize(() => {
          this.cargando = false;

          console.log('Finalizó la consulta de bitácora');

          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (data: Bitacora[]) => {

          console.log(
            'BITÁCORA COMPLETA:',
            JSON.stringify(data, null, 2)
          );

          if (!Array.isArray(data)) {

            this.registros = [];
            this.registrosFiltrados = [];

            this.mostrarMensaje(
              'La respuesta del servidor tiene un formato incorrecto.',
              'error'
            );

            return;
          }

          // Guardar y ordenar registros
          this.registros = [...data].sort((a, b) => {

            const fechaA = new Date(
              `${a.fecha}T${a.horaEntrada}`
            ).getTime();

            const fechaB = new Date(
              `${b.fecha}T${b.horaEntrada}`
            ).getTime();

            return fechaB - fechaA;
          });

          // Aplicar filtros
          this.aplicarFiltros();

          console.log('Total recibido:', this.registros.length);
          console.log(
            'Total filtrado:',
            this.registrosFiltrados.length
          );

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error('Error consultando bitácora:', error);

          this.registros = [];
          this.registrosFiltrados = [];

          if (error.status === 401) {

            this.mostrarMensaje(
              'Tu sesión ha expirado. Inicia sesión nuevamente.',
              'error'
            );

          } else if (error.status === 403) {

            this.mostrarMensaje(
              'No tienes permisos para consultar la bitácora.',
              'error'
            );

          } else if (error.status === 404) {

            this.mostrarMensaje(
              'No se encontró el endpoint /api/bitacoras.',
              'error'
            );

          } else if (error.name === 'TimeoutError') {

            this.mostrarMensaje(
              'El servidor no respondió en 15 segundos.',
              'error'
            );

          } else {

            this.mostrarMensaje(
              `Error al consultar bitácora: ${error.message || 'Error desconocido'}`,
              'error'
            );
          }

          this.cdr.detectChanges();
        }
      });
  }

  // ==========================================
  // FILTROS
  // ==========================================

  aplicarFiltros(): void {

    const termino = this.busqueda.toLowerCase().trim();

    this.registrosFiltrados = this.registros.filter(registro => {

      const docente = (registro.docente || '').toLowerCase();
      const materia = (registro.materia || '').toLowerCase();
      const laboratorio = (registro.laboratorio || '').toLowerCase();
      const observaciones = (registro.observaciones || '').toLowerCase();
      const incidencias = (registro.incidencias || '').toLowerCase();

      const coincideBusqueda =
        !termino ||
        docente.includes(termino) ||
        materia.includes(termino) ||
        laboratorio.includes(termino) ||
        observaciones.includes(termino) ||
        incidencias.includes(termino) ||
        String(registro.solicitudId).includes(termino) ||
        String(registro.id).includes(termino);

      const coincideFecha =
        !this.filtroFecha ||
        registro.fecha === this.filtroFecha;

      return coincideBusqueda && coincideFecha;
    });

    this.paginaActual = 1;

    this.cdr.markForCheck();
  }

  limpiarFiltros(): void {

    this.busqueda = '';
    this.filtroFecha = '';

    this.aplicarFiltros();

    this.cdr.detectChanges();
  }

  // ==========================================
  // ESTADÍSTICAS
  // ==========================================

  get totalRegistros(): number {
    return this.registrosFiltrados.length;
  }

  get totalLaboratorios(): number {
    return new Set(
      this.registrosFiltrados.map(r => r.laboratorio)
    ).size;
  }

  get totalDocentes(): number {
    return new Set(
      this.registrosFiltrados.map(r => r.docente)
    ).size;
  }

  get totalMaterias(): number {
    return new Set(
      this.registrosFiltrados.map(r => r.materia)
    ).size;
  }

  // ==========================================
  // PAGINACIÓN
  // ==========================================

  get totalPaginas(): number {
    return Math.max(
      1,
      Math.ceil(
        this.registrosFiltrados.length / this.registrosPorPagina
      )
    );
  }

  get registrosPaginados(): Bitacora[] {

    const inicio =
      (this.paginaActual - 1) * this.registrosPorPagina;

    return this.registrosFiltrados.slice(
      inicio,
      inicio + this.registrosPorPagina
    );
  }

  get primerRegistro(): number {

    return this.registrosFiltrados.length === 0
      ? 0
      : (this.paginaActual - 1) * this.registrosPorPagina + 1;
  }

  get ultimoRegistro(): number {

    return Math.min(
      this.paginaActual * this.registrosPorPagina,
      this.registrosFiltrados.length
    );
  }

  paginaAnterior(): void {

    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.cdr.detectChanges();
    }
  }

  paginaSiguiente(): void {

    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.cdr.detectChanges();
    }
  }

  // ==========================================
  // DETALLE DEL REGISTRO
  // ==========================================

  verDetalle(registro: Bitacora): void {

    this.registroSeleccionado = registro;

    this.cdr.detectChanges();
  }

  cerrarDetalle(): void {

    this.registroSeleccionado = null;

    this.cdr.detectChanges();
  }

  // ==========================================
  // FORMATO DE FECHAS Y HORAS
  // ==========================================

  formatearFecha(fecha: string): string {

    if (!fecha) return '-';

    const partes = fecha.split('-');

    if (partes.length !== 3) return fecha;

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }

  formatearFechaLarga(fecha: string): string {

    if (!fecha) return '-';

    const date = new Date(`${fecha}T12:00:00`);

    return date.toLocaleDateString('es-MX', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatearHora(hora: string): string {
    return hora ? hora.substring(0, 5) : '--:--';
  }

  obtenerDuracion(entrada: string, salida: string): string {

    if (!entrada || !salida) return '-';

    const [horaEntrada, minutoEntrada] =
      entrada.split(':').map(Number);

    const [horaSalida, minutoSalida] =
      salida.split(':').map(Number);

    const minutosEntrada =
      horaEntrada * 60 + minutoEntrada;

    const minutosSalida =
      horaSalida * 60 + minutoSalida;

    let diferencia = minutosSalida - minutosEntrada;

    if (diferencia < 0) {
      diferencia += 24 * 60;
    }

    const horas = Math.floor(diferencia / 60);
    const minutos = diferencia % 60;

    return `${horas} h ${minutos} min`;
  }

// ==========================================
// CARGAR LOGOTIPOS PARA EL PDF
// ==========================================

private cargarImagenPDF(ruta: string): Promise<string | null> {

  return new Promise((resolve) => {

    const imagen = new Image();

    imagen.crossOrigin = 'anonymous';

    imagen.onload = () => {

      const canvas = document.createElement('canvas');

      canvas.width = imagen.naturalWidth;
      canvas.height = imagen.naturalHeight;

      const contexto = canvas.getContext('2d');

      if (!contexto) {
        console.error('No se pudo crear el contexto del canvas.');
        resolve(null);
        return;
      }

      contexto.clearRect(
        0,
        0,
        canvas.width,
        canvas.height
      );

      contexto.drawImage(
        imagen,
        0,
        0,
        canvas.width,
        canvas.height
      );

      try {
        const imagenBase64 = canvas.toDataURL('image/png');
        resolve(imagenBase64);

      } catch (error) {
        console.error('Error al convertir el logotipo:', error);
        resolve(null);
      }
    };

    imagen.onerror = () => {
      console.error('No se pudo cargar el logotipo:', ruta);
      resolve(null);
    };

    // Ruta desde public/images/
    imagen.src = ruta;
  });
}
  // ==========================================
  // GENERAR REPORTE PDF INSTITUCIONAL
  // ==========================================

  async descargarPDF(): Promise<void> {

    try {

      const registros = this.registrosFiltrados;

      if (!registros.length) {

        this.mostrarMensaje(
          'No hay registros para generar el PDF.',
          'error'
        );

        return;
      }

      const doc = new jsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4'
      });

      const azul: [number, number, number] = [0, 63, 115];
      const gris: [number, number, number] = [245, 245, 245];

      const ahora = new Date();

      // Usuario que genera el documento
      const generadoPor =
        localStorage.getItem('nombre') || 'Usuario del sistema';

      // Fecha y hora de generación
      const fechaGeneracion = ahora.toLocaleString('es-MX', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      });

      // Cargar logotipos institucionales
 const [logoITSE, logoSIGELAB] = await Promise.all([
  this.cargarImagenPDF('/images/logo-itse.png'),
  this.cargarImagenPDF('/images/logo-sigelab.png')
]);

      // ======================================
      // PERIODO DEL REPORTE
      // ======================================

      const fechas = registros
        .map(r => r.fecha)
        .filter(Boolean)
        .sort();

      const fechaInicio = fechas[0] || '-';
      const fechaFin = fechas[fechas.length - 1] || '-';

      // ======================================
      // INDICADORES
      // ======================================

      const total = registros.length;

      const laboratorios = new Set(
        registros.map(r => r.laboratorio)
      ).size;

      const docentes = new Set(
        registros.map(r => r.docente)
      ).size;

      const materias = new Set(
        registros.map(r => r.materia)
      ).size;

      // ======================================
      // ENCABEZADO INSTITUCIONAL
      // ======================================

// ======================================
// ENCABEZADO INSTITUCIONAL - SIGELAB
// ======================================

const azulInstitucional: [number, number, number] = [35, 20, 105];
const azulSecundario: [number, number, number] = [65, 45, 145];
const grisTexto: [number, number, number] = [75, 75, 90];

// Fondo blanco
doc.setFillColor(255, 255, 255);
doc.rect(0, 0, 297, 210, 'F');

// ======================================
// LOGOTIPO ITSE
// ======================================

if (logoITSE) {
  const propiedades = doc.getImageProperties(logoITSE);

  const anchoMaximo = 37;
  const altoMaximo = 30;

  const escala = Math.min(
    anchoMaximo / propiedades.width,
    altoMaximo / propiedades.height
  );

  const ancho = propiedades.width * escala;
  const alto = propiedades.height * escala;

  doc.addImage(
    logoITSE,
    'PNG',
    15 + (anchoMaximo - ancho) / 2,
    8 + (altoMaximo - alto) / 2,
    ancho,
    alto
  );
}

// ======================================
// LOGOTIPO SIGELAB
// ======================================

if (logoSIGELAB) {
  const propiedades = doc.getImageProperties(logoSIGELAB);

  const anchoMaximo = 42;
  const altoMaximo = 30;

  const escala = Math.min(
    anchoMaximo / propiedades.width,
    altoMaximo / propiedades.height
  );

  const ancho = propiedades.width * escala;
  const alto = propiedades.height * escala;

  doc.addImage(
    logoSIGELAB,
    'PNG',
    240 + (anchoMaximo - ancho) / 2,
    8 + (altoMaximo - alto) / 2,
    ancho,
    alto
  );
}

// ======================================
// ENCABEZADO CENTRAL
// ======================================

doc.setTextColor(...azulInstitucional);
doc.setFont('helvetica', 'bold');
doc.setFontSize(16);

doc.text(
  'INSTITUTO TECNOLÓGICO SUPERIOR DE',
  148.5,
  17,
  { align: 'center' }
);

doc.setFontSize(19);

doc.text(
  'ESCÁRCEGA',
  148.5,
  26,
  { align: 'center' }
);

// Nombre del sistema
doc.setFont('helvetica', 'normal');
doc.setFontSize(9);
doc.setTextColor(...grisTexto);

doc.text(
  'SIGELAB | Sistema de Gestión de Laboratorios',
  148.5,
  34,
  { align: 'center' }
);

// ======================================
// LÍNEA INSTITUCIONAL
// ======================================

doc.setDrawColor(...azulInstitucional);
doc.setLineWidth(0.8);

doc.line(14, 43, 283, 43);

// Línea secundaria decorativa
doc.setDrawColor(190, 180, 225);
doc.setLineWidth(0.25);

doc.line(14, 44.5, 283, 44.5);

// ======================================
// TÍTULO DEL REPORTE
// ======================================

doc.setFont('helvetica', 'bold');
doc.setFontSize(16);
doc.setTextColor(...azulInstitucional);

doc.text(
  'REPORTE GENERAL DE BITÁCORA',
  148.5,
  58,
  { align: 'center' }
);

// Subtítulo
doc.setFont('helvetica', 'normal');
doc.setFontSize(9);
doc.setTextColor(...grisTexto);

doc.text(
  'CONTROL Y SEGUIMIENTO DEL USO DE LABORATORIOS',
  148.5,
  65,
  { align: 'center' }
);

   
    

      // ======================================
      // INFORMACIÓN DEL REPORTE
      // ======================================

      doc.setFont('helvetica', 'normal');
      doc.setFontSize(9);

      doc.text(
        `Fecha de generación: ${fechaGeneracion}`,
        14,
        61
      );

      doc.text(
        `Periodo seleccionado: ${fechaInicio} al ${fechaFin}`,
        14,
        67
      );

      doc.text(
        `Generado por: ${generadoPor}`,
        14,
        73
      );

      // ======================================
      // RESUMEN EJECUTIVO
      // ======================================

      doc.setFont('helvetica', 'bold');
      doc.setFontSize(12);

      doc.text(
        'RESUMEN EJECUTIVO',
        14,
        87
      );

      autoTable(doc, {

        startY: 92,

        head: [['Indicador', 'Cantidad']],

        body: [
          ['Total de registros', String(total)],
          ['Laboratorios utilizados', String(laboratorios)],
          ['Docentes registrados', String(docentes)],
          ['Materias registradas', String(materias)]
        ],

        theme: 'grid',

        tableWidth: 105,

        styles: {
          font: 'helvetica',
          fontSize: 9,
          cellPadding: 2,
          lineColor: [40, 40, 40],
          lineWidth: 0.2
        },

        headStyles: {
          fillColor: azul,
          textColor: [255, 255, 255],
          fontStyle: 'bold'
        },

        alternateRowStyles: {
          fillColor: gris
        }
      });

      // ======================================
      // TABLA GENERAL DE BITÁCORA
      // ======================================

      const filas = registros.map(r => [

        String(r.id),

        r.docente || '-',

        r.materia || '-',

        r.laboratorio || '-',

        this.formatearFecha(r.fecha),

        `${this.formatearHora(r.horaEntrada)} - ${this.formatearHora(r.horaSalida)}`,

        r.observaciones || '-',

        r.incidencias || '-'

      ]);

      const posicionTabla =
        (doc as any).lastAutoTable.finalY + 10;

      autoTable(doc, {

        startY: posicionTabla,

        head: [[
          'ID',
          'DOCENTE',
          'MATERIA',
          'LABORATORIO',
          'FECHA',
          'HORARIO',
          'OBSERVACIONES',
          'INCIDENCIAS'
        ]],

        body: filas,

        theme: 'grid',

        styles: {
          font: 'helvetica',
          fontSize: 7,
          cellPadding: 2,
          overflow: 'linebreak',
          valign: 'middle',
          lineColor: [40, 40, 40],
          lineWidth: 0.2
        },

        headStyles: {
          fillColor: azul,
          textColor: [255, 255, 255],
          fontStyle: 'bold',
          halign: 'center'
        },

        alternateRowStyles: {
          fillColor: gris
        },

        columnStyles: {
          0: { cellWidth: 10 },
          1: { cellWidth: 33 },
          2: { cellWidth: 30 },
          3: { cellWidth: 32 },
          4: { cellWidth: 22 },
          5: { cellWidth: 25 },
          6: { cellWidth: 38 },
          7: { cellWidth: 38 }
        },

        margin: {
          left: 14,
          right: 14,
          bottom: 15
        }
      });

      // ======================================
      // PIE DE PÁGINA
      // ======================================

      const paginasContenido = doc.getNumberOfPages();

      for (let i = 1; i <= paginasContenido; i++) {

        doc.setPage(i);

        doc.setFont('helvetica', 'normal');
        doc.setFontSize(8);
        doc.setTextColor(60, 60, 60);

        doc.text(
          `SIGELAB | Página ${i}`,
          148.5,
          200,
          { align: 'center' }
        );
      }

      // ======================================
      // PÁGINA DE CIERRE INSTITUCIONAL
      // ======================================

      doc.addPage();

      doc.setTextColor(0, 0, 0);

      // Nombre del responsable
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(13);

      doc.text(
        generadoPor,
        148.5,
        82,
        { align: 'center' }
      );

      // Línea de separación
      doc.setFontSize(12);

      doc.text(
        '________________________________________',
        148.5,
        90,
        { align: 'center' }
      );

      // Nombre del sistema
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(12);

      doc.text(
        'SIGELAB',
        148.5,
        98,
        { align: 'center' }
      );

      // Descripción del sistema
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);

      doc.text(
        'Sistema de Gestión de Laboratorios',
        148.5,
        105,
        { align: 'center' }
      );

      // Leyenda institucional
      doc.setFontSize(9);

      doc.text(
        'Documento generado automáticamente por SIGELAB - Instituto Tecnológico Superior de Escárcega',
        148.5,
        120,
        {
          align: 'center',
          maxWidth: 230
        }
      );

      // Fecha
      doc.text(
        `Fecha: ${fechaGeneracion}`,
        148.5,
        130,
        { align: 'center' }
      );

      // Pie de página
      doc.setFontSize(8);

      doc.text(
        `SIGELAB | Página ${doc.getNumberOfPages()}`,
        148.5,
        200,
        { align: 'center' }
      );

      // ======================================
      // DESCARGAR PDF
      // ======================================

      const nombreArchivo =
        `SIGELAB_Bitacora_${ahora.toISOString().slice(0, 10)}.pdf`;

      doc.save(nombreArchivo);

      this.mostrarMensaje(
        'Reporte PDF generado correctamente.',
        'success'
      );

    } catch (error) {

      console.error('Error al generar PDF:', error);

      this.mostrarMensaje(
        'No fue posible generar el reporte PDF.',
        'error'
      );
    }
  }

  // ==========================================
  // MOSTRAR MENSAJES
  // ==========================================

  mostrarMensaje(
    mensaje: string,
    tipo: 'success' | 'error' | 'info' = 'info'
  ): void {

    this.mensaje = mensaje;
    this.tipoMensaje = tipo;

    this.cdr.detectChanges();
  }

}
