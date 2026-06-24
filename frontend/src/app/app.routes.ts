import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AccountPage } from './pages/account/account';
import { AdminApprovalsPage } from './pages/admin-approvals/admin-approvals';
import { AdminDashboardPage } from './pages/admin-dashboard/admin-dashboard';
import { AdminPropertiesPage } from './pages/admin-properties/admin-properties';
import { AdminUsersPage } from './pages/admin-users/admin-users';
import { HomePage } from './pages/home/home';
import { PropertiesPage } from './pages/properties/properties';
import { LandlordApplicationsPage } from './pages/landlord-applications/landlord-applications';
import { LandlordDashboardPage } from './pages/landlord-dashboard/landlord-dashboard';
import { LandlordPropertiesPage } from './pages/landlord-properties/landlord-properties';
import { LoginPage } from './pages/login/login';
import { PropertyDetailsPage } from './pages/property-details/property-details';
import { PropertyFormPage } from './pages/property-form/property-form';
import { RegisterPage } from './pages/register/register';
import { TenantApplicationsPage } from './pages/tenant-applications/tenant-applications';
import { TenantDashboardPage } from './pages/tenant-dashboard/tenant-dashboard';
import { TenantFavoritesPage } from './pages/tenant-favorites/tenant-favorites';

export const routes: Routes = [
  { path: '', component: HomePage },
  { path: 'properties', component: PropertiesPage },
  { path: 'properties/:id', component: PropertyDetailsPage },
  { path: 'login', component: LoginPage },
  { path: 'register', component: RegisterPage },
  { path: 'account', component: AccountPage, canActivate: [authGuard] },
  { path: 'tenant', component: TenantDashboardPage, canActivate: [authGuard, roleGuard(['TENANT'])] },
  { path: 'tenant/applications', component: TenantApplicationsPage, canActivate: [authGuard, roleGuard(['TENANT'])] },
  { path: 'tenant/interested', component: TenantFavoritesPage, canActivate: [authGuard, roleGuard(['TENANT'])] },
  { path: 'tenant/favorites', redirectTo: 'tenant/interested' },
  { path: 'landlord', component: LandlordDashboardPage, canActivate: [authGuard, roleGuard(['LANDLORD'])] },
  { path: 'landlord/properties', component: LandlordPropertiesPage, canActivate: [authGuard, roleGuard(['LANDLORD'])] },
  { path: 'landlord/properties/new', component: PropertyFormPage, canActivate: [authGuard, roleGuard(['LANDLORD'])] },
  { path: 'landlord/properties/:id/edit', component: PropertyFormPage, canActivate: [authGuard, roleGuard(['LANDLORD'])] },
  { path: 'landlord/applications', component: LandlordApplicationsPage, canActivate: [authGuard, roleGuard(['LANDLORD'])] },
  { path: 'admin', component: AdminDashboardPage, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin/approvals', component: AdminApprovalsPage, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin/properties', component: AdminPropertiesPage, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin/users', component: AdminUsersPage, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: '**', redirectTo: '' }
];
