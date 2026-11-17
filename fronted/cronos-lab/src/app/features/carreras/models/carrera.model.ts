
export interface Carrera {
  id: number;
  clave: string;
  nombre: string;
  activo: boolean;
}

export interface CarreraRequest {
  clave: string;
  nombre: string;
}
