import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard-admin.html',
  styleUrl: './dashboard-admin.scss'
})
export class DashboardAdmin {

  nombre = localStorage.getItem('nombre');

  totalUsuarios = 25;
  totalLaboratorios = 8;
  totalSolicitudes = 32;
  totalMaterias = 18;

  pendientes = 10;
  aprobadas = 15;
  rechazadas = 7;

}