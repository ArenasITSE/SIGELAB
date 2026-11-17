
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  Laboratorio,
  LaboratorioRequest
} from '../models/laboratorio.model';

@Injectable({
  providedIn: 'root'
})
export class LaboratorioService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/laboratorios`;

  listar(): Observable<Laboratorio[]> {
    return this.http.get<Laboratorio[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Laboratorio> {
    return this.http.get<Laboratorio>(`${this.apiUrl}/${id}`);
  }

  crear(request: LaboratorioRequest): Observable<Laboratorio> {
    return this.http.post<Laboratorio>(this.apiUrl, request);
  }

  actualizar(
    id: number,
    request: LaboratorioRequest
  ): Observable<Laboratorio> {
    return this.http.put<Laboratorio>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  // Desactivar (eliminación lógica)
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Eliminar definitivamente
  eliminarDefinitivamente(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}/permanente`
    );
  }
}
