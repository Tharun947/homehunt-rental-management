import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/api.models';

export const roleGuard = (roles: Role[]): CanActivateFn => {
  return () => roles.includes(inject(AuthService).role() as Role) || inject(Router).createUrlTree(['/']);
};
