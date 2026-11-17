
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  Usuario,
  UsuarioRequest
} from '../models/usuario.model';

// ==========================================
// RESULTADO DE IMPORTACIÓN
// ==========================================

export interface DetalleImportacion {
  fila: number;
  correo: string;
  estado: string;
  mensaje: string;
}

export interface ResultadoImportacion {
  total: number;
  exitosos: number;
  rechazados: number;
  detalles: DetalleImportacion[];
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/usuarios`;

  // ==========================================
  // LISTAR USUARIOS
  // ==========================================

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  // ==========================================
  // BUSCAR USUARIO POR ID
  // ==========================================

  buscarPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(
      `${this.apiUrl}/${id}`
    );
  }

  // ==========================================
  // CREAR USUARIO
  // ==========================================

  crear(request: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(
      this.apiUrl,
      request
    );
  }

  // ==========================================
  // ACTUALIZAR USUARIO
  // ==========================================

  actualizar(
    id: number,
    request: UsuarioRequest
  ): Observable<Usuario> {

    return this.http.put<Usuario>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  // ==========================================
  // DESACTIVAR USUARIO
  // ==========================================

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }

  // ==========================================
  // IMPORTAR USUARIOS DESDE EXCEL
  // ==========================================

  importarExcel(
    archivo: File
  ): Observable<ResultadoImportacion> {

    const formData = new FormData();

    formData.append('archivo', archivo);

    return this.http.post<ResultadoImportacion>(
      `${this.apiUrl}/importar-excel`,
      formData
    );
  }

}
