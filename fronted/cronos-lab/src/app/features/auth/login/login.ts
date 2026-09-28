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

        this.authService.guardarToken(
          response.token
        );

        localStorage.setItem(
          'token',
          response.token
        );

        this.loading = false;

        Swal.fire({
          icon: 'success',
          title: 'Bienvenido',
          text: 'Acceso correcto',
          timer: 1200,
          showConfirmButton: false
        }).then(() => {

          this.router.navigate([
            '/dashboard'
          ]);

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