
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Carrera, CarreraRequest } from '../models/carrera.model';

@Injectable({
  providedIn: 'root'
})
export class CarreraService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/carreras`;

  listar(): Observable<Carrera[]> {
    return this.http.get<Carrera[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Carrera> {
    return this.http.get<Carrera>(`${this.apiUrl}/${id}`);
  }

  crear(request: CarreraRequest): Observable<Carrera> {
    return this.http.post<Carrera>(this.apiUrl, request);
  }

  actualizar(
    id: number,
    request: CarreraRequest
  ): Observable<Carrera> {
    return this.http.put<Carrera>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
