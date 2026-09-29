import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

import { LoginComponent } from './features/auth/login/login';

import { DashboardComponent } from './features/dashboard/dashboard/dashboard';

import { DashboardDocente } from './features/dashboard-docente/dashboard-docente/dashboard-docente';

import { DashboardTecnico } from './features/dashboard-tecnico/dashboard-tecnico/dashboard-tecnico';

import { DashboardCoordinador } from './features/dashboard-coordinador/dashboard-coordinador/dashboard-coordinador';

import { MainLayoutComponent } from './layout/main-layout/main-layout';

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

      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [roleGuard],
        data: {
          role: 'ADMINISTRADOR'
        }
      },

      {
        path: 'dashboard-docente',
        component: DashboardDocente,
        canActivate: [roleGuard],
        data: {
          role: 'DOCENTE'
        }
      },

      {
        path: 'dashboard-tecnico',
        component: DashboardTecnico,
        canActivate: [roleGuard],
        data: {
          role: 'TECNICO'
        }
      },

      {
        path: 'dashboard-coordinador',
        component: DashboardCoordinador,
        canActivate: [roleGuard],
        data: {
          role: 'COORDINADOR'
        }
      }

    ]
  },

  // Ruta no encontrada
  {
    path: '**',
    redirectTo: ''
  }

];