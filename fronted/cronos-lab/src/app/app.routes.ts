
import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

import { LoginComponent } from './features/auth/login/login';

import { DashboardComponent } from './features/dashboard/dashboard/dashboard';
import { DashboardDocente } from './features/dashboard-docente/dashboard-docente/dashboard-docente';
import { DashboardTecnico } from './features/dashboard-tecnico/dashboard-tecnico/dashboard-tecnico';
import { DashboardAdmin } from './features/dashboard-admin/dashboard-admin';
import { DashboardCoordinador } from './features/dashboard-coordinador/dashboard-coordinador/dashboard-coordinador';

import { MainLayoutComponent } from './layout/main-layout/main-layout';

import { LaboratorioListComponent } from './features/laboratorios/pages/laboratorio-list/laboratorio-list';
import { MateriasComponent } from './features/materias/pages/materias/materias';

export const routes: Routes = [

  // Login
  {
    path: '',
    component: LoginComponent
  },

  // Layout protegido
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [

      // Dashboard administrador
      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [roleGuard],
        data: {
          role: 'ADMINISTRADOR'
        }
      },

      // Dashboard docente
      {
        path: 'dashboard-docente',
        component: DashboardDocente,
        canActivate: [roleGuard],
        data: {
          role: 'DOCENTE'
        }
      },

      // Dashboard técnico
      {
        path: 'dashboard-tecnico',
        component: DashboardTecnico,
        canActivate: [roleGuard],
        data: {
          role: 'TECNICO'
        }
      },

      // Dashboard coordinador
      {
        path: 'dashboard-coordinador',
        component: DashboardCoordinador,
        canActivate: [roleGuard],
        data: {
          role: 'COORDINADOR'
        }
      },

      // Dashboard admin
      {
        path: 'dashboard-admin',
        component: DashboardAdmin,
        canActivate: [roleGuard],
        data: {
          role: 'ADMINISTRADOR'
        }
      },

      // Laboratorios
      {
        path: 'laboratorios',
        component: LaboratorioListComponent,
        canActivate: [roleGuard],
        data: {
          roles: [
            'ADMINISTRADOR',
            'COORDINADOR',
            'TECNICO',
            'DOCENTE'
          ]
        }
      },

      // Materias
      {
        path: 'materias',
        component: MateriasComponent,
        canActivate: [roleGuard],
        data: {
          roles: [
            'ADMINISTRADOR',
            'COORDINADOR'
          ]
        }
      },

      // Usuarios
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/usuarios/pages/usuario-list/usuario-list')
            .then(m => m.UsuarioListComponent)
      },

      // Solicitudes
      {
        path: 'solicitudes',
        loadComponent: () =>
          import('./features/solicitudes/pages/solicitud-list/solicitud-list')
            .then(m => m.SolicitudListComponent)
      },

      // Carreras
      {
        path: 'carreras',
        loadComponent: () =>
          import('./features/carreras/pages/carrera-list/carrera-list')
            .then(m => m.CarreraListComponent)
      },

      // Docentes
      {
        path: 'docentes',
        loadComponent: () =>
          import('./features/docentes/pages/docente-list/docente-list')
            .then(m => m.DocenteListComponent)
      },

      // Calendario
      {
        path: 'calendario',
        loadComponent: () =>
          import('./features/calendario/pages/calendario-list/calendario-list')
            .then(m => m.CalendarioListComponent)
      },

      // Bitácora
      {
        path: 'bitacora',
        loadComponent: () =>
          import('./features/bitacora/pages/bitacora-list/bitacora-list')
            .then(m => m.BitacoraListComponent)
      }

    ]
  },

  // Ruta no encontrada
  {
    path: '**',
    redirectTo: ''
  }

];
