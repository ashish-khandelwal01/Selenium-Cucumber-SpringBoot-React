import { useState, useEffect, useRef, useCallback } from 'react';
import { rerunTests } from '../api/testRerunApi';
import { rerunAsync } from '../api/asyncTestApi';

export interface UseTestRunManagerProps {
  onMessage: (message: { type: 'success' | 'error'; text: string }) => void;
  onAsyncJobCreated: (jobId: string, runId: string) => void;
  onRefetchNeeded: () => void;
}

export interface UseTestRunManagerReturn {
  // Auto refresh state
  autoRefresh: boolean;
  setAutoRefresh: (value: boolean) => void;
  
  // Rerun modal state
  selectedRunForRerun: any | null;
  setSelectedRunForRerun: (run: any | null) => void;
  isAsync: boolean;
  setIsAsync: (value: boolean) => void;
  isLoading: boolean;
  
  // Failed scenarios modal state
  selectedRunForFailures: any | null;
  setSelectedRunForFailures: (run: any | null) => void;
  
  // Message state
  message: { type: 'success' | 'error'; text: string } | null;
  handleExternalMessage: (message: { type: 'success' | 'error'; text: string }) => void;
  
  // Actions
  handleRerun: () => Promise<void>;
  canRerun: (run: any) => boolean;
}

export function useTestRunManager({
  onMessage,
  onAsyncJobCreated,
  onRefetchNeeded,
}: UseTestRunManagerProps): UseTestRunManagerReturn {
  // Auto refresh state
  const [autoRefresh, setAutoRefresh] = useState(false);
  const refreshIntervalRef = useRef<NodeJS.Timeout | null>(null);

  // Rerun modal state
  const [selectedRunForRerun, setSelectedRunForRerun] = useState<any | null>(null);
  const [isAsync, setIsAsync] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Failed scenarios modal state
  const [selectedRunForFailures, setSelectedRunForFailures] = useState<any | null>(null);

  // Message state
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Auto-hide message after 4 seconds
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  // Handle external messages (like from async job manager)
  const handleExternalMessage = useCallback((msg: { type: 'success' | 'error'; text: string }) => {
    setMessage(msg);
    onMessage(msg);
  }, [onMessage]);

  // Auto Refresh
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

  const handleRerun = useCallback(async () => {
    if (!selectedRunForRerun) return;
    setIsLoading(true);
    
    try {
      if (isAsync) {
        const response = await rerunAsync(selectedRunForRerun.runId);
        const jobId = response.data.jobId || response.data;

        // Notify parent about new async job
        onAsyncJobCreated(jobId, selectedRunForRerun.runId);

        const successMessage = {
          type: 'success' as const,
          text: `✅ Async rerun for run ${selectedRunForRerun.runId} triggered.`,
        };
        setMessage(successMessage);
        onMessage(successMessage);
      } else {
        await rerunTests(selectedRunForRerun.runId);
        const successMessage = {
          type: 'success' as const,
          text: `✅ Sync rerun for run ${selectedRunForRerun.runId} triggered.`,
        };
        setMessage(successMessage);
        onMessage(successMessage);
        onRefetchNeeded();
      }
    } catch (error) {
      const errorMessage = { 
        type: 'error' as const, 
        text: '❌ Failed to trigger rerun.' 
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
    return !run.tags?.includes('Rerun');
  }, []);

  return {
    // Auto refresh state
    autoRefresh,
    setAutoRefresh,
    
    // Rerun modal state
    selectedRunForRerun,
    setSelectedRunForRerun,
    isAsync,
    setIsAsync,
    isLoading,
    
    // Failed scenarios modal state
    selectedRunForFailures,
    setSelectedRunForFailures,
    
    // Message state
    message,
    handleExternalMessage,
    
    // Actions
    handleRerun,
    canRerun,
  };
}