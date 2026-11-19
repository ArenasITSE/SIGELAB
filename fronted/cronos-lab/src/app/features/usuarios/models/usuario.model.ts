export type RolUsuario =
  | 'ADMINISTRADOR'
  | 'COORDINADOR'
  | 'TECNICO'
  | 'DOCENTE';

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: RolUsuario;
  activo: boolean;

  // Carrera principal, para compatibilidad.
  carreraId: number | null;
  carrera: string | null;

  // Carreras adicionales del docente.
  carreraIds?: number[];
  carreras?: string[];
}

export interface UsuarioRequest {
  nombre: string;
  correo: string;
  password?: string;
  rol: RolUsuario;

  // Se conserva para los roles con una sola carrera.
  carreraId: number | null;

  // Se utiliza cuando el usuario es DOCENTE.
  carreraIds?: number[];
}

export interface CarreraOption {
  id: number;
  clave: string;
  nombre: string;
  activo: boolean;
}