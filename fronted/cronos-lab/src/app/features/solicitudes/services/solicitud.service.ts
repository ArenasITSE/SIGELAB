
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  SolicitudLaboratorio,
  SolicitudRequest,
  RechazoSolicitudRequest
} from '../models/solicitud.model';

@Injectable({
  providedIn: 'root'
})
export class SolicitudService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/solicitudes`;

  listar(): Observable<SolicitudLaboratorio[]> {
    return this.http.get<SolicitudLaboratorio[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<SolicitudLaboratorio> {
    return this.http.get<SolicitudLaboratorio>(
      `${this.apiUrl}/${id}`
    );
  }

  crear(request: SolicitudRequest): Observable<SolicitudLaboratorio> {
    return this.http.post<SolicitudLaboratorio>(
      this.apiUrl,
      request
    );
  }

  autorizar(id: number): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/${id}/autorizar`,
      {}
    );
  }

  rechazar(
    id: number,
    request: RechazoSolicitudRequest
  ): Observable<void> {
    return this.http.patch<void>(
      `${this.apiUrl}/${id}/rechazar`,
      request
    );
  }
}
