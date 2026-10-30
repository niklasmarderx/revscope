import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { AnalysisSummary } from '../models';

@Component({
  selector: 'app-analysis-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './analysis-list.component.html',
  styleUrl: './analysis-list.component.css',
})
export class AnalysisListComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  analyses: AnalysisSummary[] = [];
  loading = false;
  uploading = false;
  error: string | null = null;
  selectedFile: File | null = null;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.error = null;
    this.api.list().subscribe({
      next: (data) => {
        this.analyses = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not reach the backend at /api. Is revscope-backend running?';
        this.loading = false;
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  upload(): void {
    if (!this.selectedFile) {
      return;
    }
    this.uploading = true;
    this.error = null;
    this.api.upload(this.selectedFile).subscribe({
      next: (summary) => {
        this.uploading = false;
        this.router.navigate(['/analyses', summary.id]);
      },
      error: () => {
        this.error = 'Upload failed.';
        this.uploading = false;
      },
    });
  }
}
