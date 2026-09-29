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

  const expectedRole =
    route.data['role'];

  const currentRole =
    authService.obtenerRol();

  if (currentRole === expectedRole) {
    return true;
  }

  return router.createUrlTree([
    '/'
  ]);

};