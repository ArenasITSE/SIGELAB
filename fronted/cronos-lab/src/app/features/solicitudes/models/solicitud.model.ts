
export type EstatusSolicitud =
  | 'PENDIENTE'
  | 'AUTORIZADA'
  | 'RECHAZADA';

export interface SolicitudLaboratorio {
  id: number;

  laboratorioId: number;
  laboratorio: string;

  solicitanteId: number;
  solicitante: string;

  carreraId: number;
  carrera: string;

  fecha: string;
  horaInicio: string;
  horaFin: string;

  numeroPersonas: number;
  motivo: string;

  estatus: EstatusSolicitud;

  observaciones?: string | null;

  fechaSolicitud?: string;
}

export interface SolicitudRequest {
  laboratorioId: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  numeroPersonas: number;
  motivo: string;
}

export interface RechazoSolicitudRequest {
  observaciones: string;
}

export interface LaboratorioOption {
  id: number;
  nombre: string;
  capacidad: number;
  estatus: string;
  carreraId: number;
  carrera: string;
}
