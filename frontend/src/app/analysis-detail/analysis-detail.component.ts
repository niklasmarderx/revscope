import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import cytoscape from 'cytoscape';
import { ApiService } from '../api.service';
import {
  AnalysisDetail,
  DecompiledFunction,
  FunctionExplanation,
  FunctionSummary,
} from '../models';

type Tab = 'functions' | 'strings' | 'imports' | 'graph';

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

  @ViewChild('graphContainer') graphContainer?: ElementRef<HTMLDivElement>;

  detail: AnalysisDetail | null = null;
  selected: DecompiledFunction | null = null;
  selectedAddress: string | null = null;
  explanation: FunctionExplanation | null = null;
  loadingCode = false;
  explaining = false;
  error: string | null = null;
  tab: Tab = 'functions';
  id = '';

  private graphRendered = false;

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

  setTab(tab: Tab): void {
    this.tab = tab;
    if (tab === 'graph') {
      setTimeout(() => this.renderGraph(), 0);
    }
  }

  select(fn: FunctionSummary): void {
    this.selectedAddress = fn.address;
    this.explanation = null;
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

  explain(): void {
    if (!this.selected) {
      return;
    }
    this.explaining = true;
    this.api.explain(this.id, this.selected.address).subscribe({
      next: (explanation) => {
        this.explanation = explanation;
        this.explaining = false;
      },
      error: () => {
        this.explaining = false;
      },
    });
  }

  private renderGraph(): void {
    if (this.graphRendered || !this.detail || !this.graphContainer) {
      return;
    }
    const known = new Set(this.detail.functions.map((f) => f.address));
    const nodes = this.detail.functions.map((f) => ({
      data: { id: f.address, label: f.name },
    }));
    const edges = this.detail.callGraph
      .filter((e) => known.has(e.fromAddress) && known.has(e.toAddress))
      .map((e) => ({ data: { source: e.fromAddress, target: e.toAddress } }));

    const options: any = {
      container: this.graphContainer.nativeElement,
      elements: [...nodes, ...edges],
      style: [
        {
          selector: 'node',
          style: {
            'background-color': '#1f6feb',
            label: 'data(label)',
            color: '#c9d1d9',
            'font-size': '9px',
            'text-valign': 'top',
            'text-halign': 'center',
            width: 16,
            height: 16,
          },
        },
        {
          selector: 'edge',
          style: {
            width: 1,
            'line-color': '#30475e',
            'target-arrow-color': '#30475e',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
          },
        },
      ],
      layout: { name: 'cose', animate: false },
    };
    cytoscape(options);
    this.graphRendered = true;
  }
}
