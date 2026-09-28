import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

import Swal from 'sweetalert2';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class SidebarComponent {

  private router = inject(Router);

  cerrarSesion(): void {

    Swal.fire({
      title: 'Cerrar sesión',
      text: '¿Deseas salir del sistema?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, salir',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc3545'
    }).then((result) => {

      if (result.isConfirmed) {

        localStorage.removeItem('token');

        Swal.fire({
          icon: 'success',
          title: 'Sesión cerrada',
          timer: 1000,
          showConfirmButton: false
        }).then(() => {

          this.router.navigate(['/']);

        });

      }

    });

  }

}