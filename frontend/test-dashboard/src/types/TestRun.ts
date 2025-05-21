export type BackendStatus =
  | "Execution Successful"
  | "Execution Completed with Failures"
  | "Rerun Successful"
  | "Rerun Completed with Failures"
  | "RUNNING"
  | "CANCELLED";

export type TestRun = {
  runId: string;
  status: BackendStatus;
  triggeredBy: string;
  durationSeconds: number;
  startTime: string;
  passed: number;
  failed: number;
};

export type PieChartData = {
  name: string;
  value: number;
};

export type WeeklySummary = {
  dailySummaries: DailySummary[];
  totalPassed: number;
  totalFailed: number;
}

export type DailySummary = {
  date: string;
  passed: number;
  failed: number;
}

export type ListReports = {
  runId: string;
  status: BackendStatus;
  durationSeconds: number;
  startTime: string;
  endTime: string;
  tags: string[];
  total: number;
  passed: number;
  failed: number;
}

