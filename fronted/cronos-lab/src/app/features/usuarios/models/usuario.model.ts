
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

  carreraId: number | null;
  carrera: string | null;
}

export interface UsuarioRequest {
  nombre: string;
  correo: string;
  password?: string;
  rol: RolUsuario;
  carreraId: number | null;
}

export interface CarreraOption {
  id: number;
  clave: string;
  nombre: string;
  activo: boolean;
}
