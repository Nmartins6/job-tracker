import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'workspace',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'workspace',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dashboard-page.component').then(
        (m) => m.DashboardPageComponent,
      ),
  },
  {
    path: 'applications/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/application-detail-page.component').then(
        (m) => m.ApplicationDetailPageComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'workspace',
  },
];
