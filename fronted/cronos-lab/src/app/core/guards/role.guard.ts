
import { inject } from '@angular/core';

import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router
} from '@angular/router';

import { AuthService } from '../../features/auth/auth.service';

export const roleGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot
) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  // Permite validar un rol o varios roles
  const expectedRoles: string[] =
    route.data['roles'] ??
    (route.data['role'] ? [route.data['role']] : []);

  const currentRole = authService.obtenerRol();

  // Validar acceso
  if (
    currentRole &&
    expectedRoles.length > 0 &&
    expectedRoles.includes(currentRole)
  ) {
    return true;
  }

  // Si no tiene permisos, regresar al login
  return router.createUrlTree(['/']);
};
