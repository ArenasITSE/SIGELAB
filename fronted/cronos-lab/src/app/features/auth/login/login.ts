import { Component, inject } from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Router } from '@angular/router';

import Swal from 'sweetalert2';

import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {

  private fb = inject(FormBuilder);

  private authService =
    inject(AuthService);

  private router =
    inject(Router);

  loading = false;

  form = this.fb.group({

    correo: [
      '',
      [
        Validators.required,
        Validators.email
      ]
    ],

    password: [
      '',
      Validators.required
    ]

  });

  iniciarSesion(): void {

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      Swal.fire({
        icon: 'warning',
        title: 'Formulario incompleto',
        text: 'Ingresa tu correo y contraseña'
      });

      return;
    }

    this.loading = true;

    console.log('REQUEST', this.form.getRawValue());

    this.authService.login(
      this.form.getRawValue() as any
    )
    .subscribe({

     next: (response) => {

  console.log('LOGIN OK');
  console.log(response);

  this.authService.guardarSesion(
    response
  );

  this.loading = false;

  Swal.fire({
    icon: 'success',
    title: 'Bienvenido',
    text: `Hola ${response.nombre}`,
    timer: 1200,
    showConfirmButton: false
  }).then(() => {

    switch (response.rol) {

      case 'ADMINISTRADOR':

        this.router.navigate([
          '/dashboard'
        ]);

        break;

      case 'DOCENTE':

        this.router.navigate([
          '/dashboard-docente'
        ]);

        break;

      case 'TECNICO':

        this.router.navigate([
          '/dashboard-tecnico'
        ]);

        break;

      case 'COORDINADOR':

        this.router.navigate([
          '/dashboard-coordinador'
        ]);

        break;

      default:

        this.router.navigate([
          '/'
        ]);

    }

  });

},

      error: (error) => {

        this.loading = false;

        console.error('LOGIN ERROR');
        console.error(error);

        Swal.fire({
          icon: 'error',
          title: 'Credenciales incorrectas',
          text: 'Verifica tu correo y contraseña'
        });

      }

    });

  }

}