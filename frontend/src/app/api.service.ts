import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnalysisDetail,
  AnalysisSummary,
  DecompiledFunction,
  FunctionExplanation,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  list(): Observable<AnalysisSummary[]> {
    return this.http.get<AnalysisSummary[]>(`${this.base}/analyses`);
  }

  detail(id: string): Observable<AnalysisDetail> {
    return this.http.get<AnalysisDetail>(`${this.base}/analyses/${id}`);
  }

  upload(file: File): Observable<AnalysisSummary> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<AnalysisSummary>(`${this.base}/analyses`, form);
  }

  decompiled(id: string, address: string): Observable<DecompiledFunction> {
    return this.http.get<DecompiledFunction>(
      `${this.base}/analyses/${id}/functions/${encodeURIComponent(address)}`,
    );
  }

  explain(id: string, address: string): Observable<FunctionExplanation> {
    return this.http.post<FunctionExplanation>(
      `${this.base}/analyses/${id}/functions/${encodeURIComponent(address)}/explain`,
      {},
    );
  }
}
