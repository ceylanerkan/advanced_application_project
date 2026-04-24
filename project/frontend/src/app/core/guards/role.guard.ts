import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.currentUserValue;
  if (currentUser) {
    const expectedRoles = route.data['roles'] as Array<string>;
    if (expectedRoles && expectedRoles.includes(currentUser.role)) {
      return true;
    }
    // Redirect to default dashboard or unauthorized page
    return router.createUrlTree(['/login']);
  }

  return router.createUrlTree(['/login']);
};
