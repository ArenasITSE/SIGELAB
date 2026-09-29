import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

import { AuthRequest } from '../../models/auth-request';
import { AuthResponse } from '../../models/auth-response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/auth`;

  login(
    request: AuthRequest
  ): Observable<AuthResponse> {

    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      request
    );
  }

  guardarSesion(
    response: AuthResponse
  ): void {

    localStorage.setItem(
      'token',
      response.token
    );

    localStorage.setItem(
      'rol',
      response.rol
    );

    localStorage.setItem(
      'nombre',
      response.nombre
    );
  }

  guardarToken(
    token: string
  ): void {

    localStorage.setItem(
      'token',
      token
    );
  }

  obtenerToken(): string | null {

    return localStorage.getItem(
      'token'
    );
  }

  obtenerRol(): string | null {

    return localStorage.getItem(
      'rol'
    );
  }

  obtenerNombre(): string | null {

    return localStorage.getItem(
      'nombre'
    );
  }

  logout(): void {

    localStorage.removeItem(
      'token'
    );

    localStorage.removeItem(
      'rol'
    );

    localStorage.removeItem(
      'nombre'
    );
  }

  estaAutenticado(): boolean {

    return !!this.obtenerToken();
  }

  esAdministrador(): boolean {

    return this.obtenerRol() === 'ADMINISTRADOR';
  }

  esDocente(): boolean {

    return this.obtenerRol() === 'DOCENTE';
  }

  esTecnico(): boolean {

    return this.obtenerRol() === 'TECNICO';
  }

  esCoordinador(): boolean {

    return this.obtenerRol() === 'COORDINADOR';
  }

}