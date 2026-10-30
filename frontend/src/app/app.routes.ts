import { Routes } from '@angular/router';
import { AnalysisListComponent } from './analysis-list/analysis-list.component';
import { AnalysisDetailComponent } from './analysis-detail/analysis-detail.component';

export const routes: Routes = [
  { path: '', component: AnalysisListComponent },
  { path: 'analyses/:id', component: AnalysisDetailComponent },
  { path: '**', redirectTo: '' },
];
