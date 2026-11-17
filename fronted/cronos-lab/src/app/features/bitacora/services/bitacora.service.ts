
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  Bitacora,
  BitacoraRequest
} from '../models/bitacora.model';

@Injectable({
  providedIn: 'root'
})
export class BitacoraService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/bitacoras`;

  listar(): Observable<Bitacora[]> {
    return this.http.get<Bitacora[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Bitacora> {
    return this.http.get<Bitacora>(`${this.apiUrl}/${id}`);
  }

  crear(request: BitacoraRequest): Observable<Bitacora> {
    return this.http.post<Bitacora>(this.apiUrl, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
