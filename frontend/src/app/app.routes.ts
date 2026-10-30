import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./analysis-list/analysis-list.component').then((m) => m.AnalysisListComponent),
  },
  {
    path: 'analyses/:id',
    loadComponent: () =>
      import('./analysis-detail/analysis-detail.component').then((m) => m.AnalysisDetailComponent),
  },
  { path: '**', redirectTo: '' },
];
