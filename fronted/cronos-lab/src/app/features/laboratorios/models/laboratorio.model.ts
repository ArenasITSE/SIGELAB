
export interface Laboratorio {
  id: number;
  nombre: string;
  encargadoId: number;
  encargado: string;
  carreraId: number;
  carrera: string;
  ubicacion: string;
  capacidad: number;
  estatus: 'DISPONIBLE' | 'MANTENIMIENTO' | 'INACTIVO';
}

export interface LaboratorioRequest {
  nombre: string;
  encargadoId: number;
  carreraId: number;
  ubicacion: string;
  capacidad: number;
}

export type EstatusLaboratorio =
  | 'DISPONIBLE'
  | 'MANTENIMIENTO'
  | 'INACTIVO';
