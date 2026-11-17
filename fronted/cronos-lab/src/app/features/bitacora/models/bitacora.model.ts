
export interface Bitacora {
  id: number;
  solicitudId: number;
  docente: string;
  materia: string;
  laboratorio: string;
  fecha: string;
  horaEntrada: string;
  horaSalida: string;
  observaciones: string | null;
  incidencias: string | null;
}

export interface BitacoraRequest {
  solicitudId: number;
  horaEntrada: string;
  horaSalida: string;
  observaciones?: string;
  incidencias?: string;
}
