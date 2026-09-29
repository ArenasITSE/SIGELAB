import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';

import { AuthService } from '../../features/auth/auth.service';

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

  nombre =
    localStorage.getItem('nombre') ??
    'Usuario';

  rol =
    localStorage.getItem('rol') ??
    '';

  logout(): void {

    this.authService.logout();

    this.router.navigate(['/']);
  }

}