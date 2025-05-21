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
};