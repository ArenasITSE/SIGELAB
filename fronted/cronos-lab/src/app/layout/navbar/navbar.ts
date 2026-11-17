
import { Component, inject } from '@angular/core';
import { AuthService } from '../../features/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class NavbarComponent {

  private authService = inject(AuthService);

  get nombre(): string {
    return this.authService.obtenerNombre() ?? 'Usuario';
  }

  get rol(): string {
    return this.authService.obtenerRol() ?? '';
  }

  get tituloRol(): string {
    const roles: Record<string, string> = {
      ADMINISTRADOR: 'Administrador',
      COORDINADOR: 'Coordinador',
      DOCENTE: 'Docente',
      TECNICO: 'Técnico'
    };

    return roles[this.rol] ?? 'Usuario';
  }
}
