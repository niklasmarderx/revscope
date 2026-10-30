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

export interface StringRef {
  address: string;
  value: string;
}

export interface ImportRef {
  name: string;
  library: string;
}

export interface CallEdge {
  fromAddress: string;
  toAddress: string;
}

export interface AnalysisDetail {
  summary: AnalysisSummary;
  functions: FunctionSummary[];
  strings: StringRef[];
  imports: ImportRef[];
  callGraph: CallEdge[];
}

export interface DecompiledFunction {
  address: string;
  name: string;
  signature: string;
  code: string;
}

export interface FunctionExplanation {
  address: string;
  suggestedName: string;
  summary: string;
  source: string;
}
