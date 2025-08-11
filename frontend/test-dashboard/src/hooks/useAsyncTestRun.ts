import { useState, useCallback } from 'react';
import { runAsync } from '../api/asyncTestApi';

export interface UseAsyncTestRunProps {
  onMessage?: (message: { type: 'success' | 'error'; text: string }) => void;
  onAsyncJobCreated?: (jobId: string, tag: string) => void;
}

export interface UseAsyncTestRunReturn {
  // Loading state
  isLoading: boolean;

  // Message state
  message: { type: 'success' | 'error'; text: string } | null;

  // Actions
  runAsyncTests: (tag: string) => Promise<void>;
  clearMessage: () => void;
}

export function useAsyncTestRun({
  onMessage,
  onAsyncJobCreated,
}: UseAsyncTestRunProps = {}): UseAsyncTestRunReturn {
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const runAsyncTests = useCallback(async (tag: string) => {
    if (!tag) {
      const errorMessage = {
        type: 'error' as const,
        text: '❌ Please select a tag.',
      };
      setMessage(errorMessage);
      onMessage?.(errorMessage);
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      const response = await runAsync(tag);
      const jobId = response.data.jobId || response.data;

      // Notify parent about new async job
      onAsyncJobCreated?.(jobId, tag);

      const successMessage = {
        type: 'success' as const,
        text: `✅ Async test run for tag "${tag}" triggered successfully.`,
      };
      setMessage(successMessage);
      onMessage?.(successMessage);
    } catch (error) {
      const errorMessage = {
        type: 'error' as const,
        text: '❌ Failed to trigger async test run.',
      };
      setMessage(errorMessage);
      onMessage?.(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, [onMessage, onAsyncJobCreated]);

  const clearMessage = useCallback(() => {
    setMessage(null);
  }, []);

  return {
    isLoading,
    message,
    runAsyncTests,
    clearMessage,
  };
}