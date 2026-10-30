import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { AnalysisDetail, DecompiledFunction, FunctionSummary } from '../models';

@Component({
  selector: 'app-analysis-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './analysis-detail.component.html',
  styleUrl: './analysis-detail.component.css',
})
export class AnalysisDetailComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  detail: AnalysisDetail | null = null;
  selected: DecompiledFunction | null = null;
  selectedAddress: string | null = null;
  loadingCode = false;
  error: string | null = null;
  id = '';

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id') ?? '';
    this.api.detail(this.id).subscribe({
      next: (detail) => {
        this.detail = detail;
        if (detail.functions.length > 0) {
          this.select(detail.functions[0]);
        }
      },
      error: () => {
        this.error = 'Analysis not found.';
      },
    });
  }

  select(fn: FunctionSummary): void {
    this.selectedAddress = fn.address;
    this.loadingCode = true;
    this.api.decompiled(this.id, fn.address).subscribe({
      next: (code) => {
        this.selected = code;
        this.loadingCode = false;
      },
      error: () => {
        this.loadingCode = false;
      },
    });
  }
}
