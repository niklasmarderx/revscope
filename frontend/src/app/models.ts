export interface AnalysisSummary {
  id: string;
  fileName: string;
  fileSize: number;
  arch: string;
  status: string;
  functionCount: number;
  createdAt: string;
}

export interface FunctionSummary {
  address: string;
  name: string;
  size: number;
  paramCount: number;
}

export interface AnalysisDetail {
  summary: AnalysisSummary;
  functions: FunctionSummary[];
}

export interface DecompiledFunction {
  address: string;
  name: string;
  signature: string;
  code: string;
}
