export interface Materia {
  id: number;
  clave: string;
  nombre: string;
  semestre: number;
  horasSemana: number;
  carreraId: number;
  carrera: string;
  activo: boolean;
}

export interface MateriaRequest {
  clave: string;
  nombre: string;
  semestre: number;
  horasSemana: number;
  carreraId?: number;
}