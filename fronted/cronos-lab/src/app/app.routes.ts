import { Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login/login';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard';

import { MainLayoutComponent } from './layout/main-layout/main-layout';

export const routes: Routes = [

  {
    path: '',
    component: LoginComponent
  },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent
      }
    ]
  },

  {
    path: '**',
    redirectTo: ''
  }

];