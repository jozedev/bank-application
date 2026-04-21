import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'clientes', loadComponent: () => import('./clientes/clientes').then(m => m.Clientes) },
  { path: 'cuentas', loadComponent: () => import('./cuentas/cuentas').then(m => m.Cuentas) },
  { path: 'movimientos', loadComponent: () => import('./movimientos/movimientos').then(m => m.Movimientos) },
  { path: 'reportes', loadComponent: () => import('./reportes/reportes').then(m => m.Reportes) },
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
];
