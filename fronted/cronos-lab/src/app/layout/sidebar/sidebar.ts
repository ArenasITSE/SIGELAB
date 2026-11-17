
import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';

interface MenuItem {
  label: string;
  route: string;
  icon: string;
}

interface MenuSection {
  title: string;
  items: MenuItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class SidebarComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  nombre = localStorage.getItem('nombre') ?? 'Usuario';
  rol = localStorage.getItem('rol') ?? '';

  get dashboardRoute(): string {
    const rutas: Record<string, string> = {
      ADMINISTRADOR: '/dashboard',
      DOCENTE: '/dashboard-docente',
      TECNICO: '/dashboard-tecnico',
      COORDINADOR: '/dashboard-coordinador'
    };

    return rutas[this.rol] ?? '/';
  }

  get menuSections(): MenuSection[] {

    const menus: Record<string, MenuSection[]> = {

      ADMINISTRADOR: [
        {
          title: 'GESTIÓN',
          items: [
            { label: 'Laboratorios', route: '/laboratorios', icon: 'fas fa-flask' },
            { label: 'Solicitudes', route: '/solicitudes', icon: 'fas fa-file-signature' },
            { label: 'Calendario', route: '/calendario', icon: 'fas fa-calendar' },
            { label: 'Usuarios', route: '/usuarios', icon: 'fas fa-users' },
            { label: 'Carreras', route: '/carreras', icon: 'fas fa-graduation-cap' },
            { label: 'Docentes', route: '/docentes', icon: 'fas fa-chalkboard-teacher' },
          ]
        },
        {
          title: 'REPORTES',
          items: [
            { label: 'Bitácora', route: '/bitacora', icon: 'fas fa-clipboard-list' }
          ]
        }
      ],

      COORDINADOR: [
        {
          title: 'GESTIÓN ACADÉMICA',
          items: [
            { label: 'Laboratorios', route: '/laboratorios', icon: 'fas fa-flask' },
            { label: 'Solicitudes', route: '/solicitudes', icon: 'fas fa-file-signature' },
            { label: 'Calendario', route: '/calendario', icon: 'fas fa-calendar' },
            { label: 'Docentes', route: '/docentes', icon: 'fas fa-chalkboard-teacher' },
            { label: 'Estudiantes', route: '/estudiantes', icon: 'fas fa-user-graduate' }
          ]
        }
      ],

      DOCENTE: [
        {
          title: 'SERVICIOS',
          items: [
            { label: 'Laboratorios', route: '/laboratorios', icon: 'fas fa-flask' },
            { label: 'Mis solicitudes', route: '/solicitudes', icon: 'fas fa-file-signature' },
            { label: 'Calendario', route: '/calendario', icon: 'fas fa-calendar' }
          ]
        }
      ],

      TECNICO: [
        {
          title: 'GESTIÓN TÉCNICA',
          items: [
            { label: 'Laboratorios', route: '/laboratorios', icon: 'fas fa-flask' },
            { label: 'Solicitudes', route: '/solicitudes', icon: 'fas fa-file-signature' },
            { label: 'Calendario', route: '/calendario', icon: 'fas fa-calendar' }
          ]
        }
      ]
    };

    return menus[this.rol] ?? [];
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
