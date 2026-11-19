
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Materia, MateriaRequest } from '../models/materia.model';

@Injectable({
  providedIn: 'root'
})
export class MateriaService {

  private readonly apiUrl = `${environment.apiUrl}/materias`;

  constructor(
    private http: HttpClient
  ) {}

  // Consultar todas las materias
  listar(): Observable<Materia[]> {
    return this.http.get<Materia[]>(this.apiUrl);
  }

  // Consultar materia por ID
  buscarPorId(id: number): Observable<Materia> {
    return this.http.get<Materia>(`${this.apiUrl}/${id}`);
  }

  // Registrar materia
  crear(request: MateriaRequest): Observable<Materia> {
    return this.http.post<Materia>(this.apiUrl, request);
  }

  // Actualizar materia
  actualizar(
    id: number,
    request: MateriaRequest
  ): Observable<Materia> {

    return this.http.put<Materia>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  activar(id: number): Observable<void> {
  return this.http.patch<void>(
    `${this.apiUrl}/${id}/activar`,
    {}
  );
}

  // Desactivar materia
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}
