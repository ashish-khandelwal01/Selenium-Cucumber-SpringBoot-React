import { useState, useEffect, useRef, useCallback } from "react";
import { rerunFailedTests } from "../api/testRerunApi";
import { rerunFailedAsync } from "../api/asyncTestApi";

export interface UseTestRunFailedManagerProps {
  onMessage: (message: { type: "success" | "error"; text: string }) => void;
  onAsyncJobCreated: (jobId: string, runId: string) => void;
  onRefetchNeeded: () => void;
}

export interface UseTestRunFailedManagerReturn {
  autoRefresh: boolean;
  setAutoRefresh: (value: boolean) => void;

  selectedRunForRerun: any | null;
  setSelectedRunForRerun: (run: any | null) => void;
  isAsync: boolean;
  setIsAsync: (value: boolean) => void;
  isLoading: boolean;

  selectedRunForFailures: any | null;
  setSelectedRunForFailures: (run: any | null) => void;

  message: { type: "success" | "error"; text: string } | null;
  handleExternalMessage: (message: { type: "success" | "error"; text: string }) => void;

  handleRerunFailed: () => Promise<void>;
  canRerun: (run: any) => boolean;
}

export function useTestRunFailedManager({
  onMessage,
  onAsyncJobCreated,
  onRefetchNeeded,
}: UseTestRunFailedManagerProps): UseTestRunFailedManagerReturn {
  const [autoRefresh, setAutoRefresh] = useState(false);
  const refreshIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const [selectedRunForRerun, setSelectedRunForRerun] = useState<any | null>(null);
  const [isAsync, setIsAsync] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const [selectedRunForFailures, setSelectedRunForFailures] = useState<any | null>(null);

  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  const handleExternalMessage = useCallback(
    (msg: { type: "success" | "error"; text: string }) => {
      setMessage(msg);
      onMessage(msg);
    },
    [onMessage]
  );

  useEffect(() => {
    if (autoRefresh) {
      refreshIntervalRef.current = setInterval(() => {
        onRefetchNeeded();
      }, 60000);
    } else {
      if (refreshIntervalRef.current) {
        clearInterval(refreshIntervalRef.current);
        refreshIntervalRef.current = null;
      }
    }
    return () => {
      if (refreshIntervalRef.current) clearInterval(refreshIntervalRef.current);
    };
  }, [autoRefresh, onRefetchNeeded]);

  const handleRerunFailed = useCallback(async () => {
    if (!selectedRunForRerun) return;
    setIsLoading(true);

    try {
      if (isAsync) {
        const response = await rerunFailedAsync(selectedRunForRerun.runId);
        const jobId = response.data.jobId || response.data;

        onAsyncJobCreated(jobId, selectedRunForRerun.runId);

        const successMessage = {
          type: "success" as const,
          text: `✅ Async rerun of failed tests for run ${selectedRunForRerun.runId} triggered.`,
        };
        setMessage(successMessage);
        onMessage(successMessage);
      } else {
        await rerunFailedTests(selectedRunForRerun.runId);
        const successMessage = {
          type: "success" as const,
          text: `✅ Sync rerun of failed tests for run ${selectedRunForRerun.runId} triggered.`,
        };
        setMessage(successMessage);
        onMessage(successMessage);
        onRefetchNeeded();
      }
    } catch (error) {
      const errorMessage = {
        type: "error" as const,
        text: "❌ Failed to trigger rerun of failed tests.",
      };
      setMessage(errorMessage);
      onMessage(errorMessage);
    } finally {
      setIsLoading(false);
      setSelectedRunForRerun(null);
      setIsAsync(false);
    }
  }, [selectedRunForRerun, isAsync, onAsyncJobCreated, onMessage, onRefetchNeeded]);

  const canRerun = useCallback((run: any) => {
    return !run.tags?.includes("Rerun");
  }, []);

  return {
    autoRefresh,
    setAutoRefresh,

    selectedRunForRerun,
    setSelectedRunForRerun,
    isAsync,
    setIsAsync,
    isLoading,

    selectedRunForFailures,
    setSelectedRunForFailures,

    message,
    handleExternalMessage,

    handleRerunFailed,
    canRerun,
  };
}
