import { useState, useEffect, useRef, useCallback } from 'react';
import { getAsyncJobStatus, cancelAsyncJob } from '../api/asyncTestApi';

export interface AsyncJob {
  jobId: string;
  status: string;
  runId: string;
}

export interface UseAsyncJobManagerReturn {
  asyncJobs: Map<string, AsyncJob>;
  addAsyncJob: (jobId: string, runId: string, initialStatus?: string) => void;
  removeAsyncJob: (jobId: string) => void;
  cancelAsyncJob: (jobId: string) => Promise<void>;
  getAsyncJobForRun: (runId: string) => (AsyncJob & { jobId: string }) | null;
  hasActiveJobs: boolean;
}

export function useAsyncJobManager(
  onJobComplete: () => void,
  onMessage: (message: { type: 'success' | 'error'; text: string }) => void
): UseAsyncJobManagerReturn {
  const [asyncJobs, setAsyncJobs] = useState<Map<string, AsyncJob>>(new Map());
  const pollIntervalRef = useRef<NodeJS.Timeout | null>(null);

  // Poll async job statuses for all active jobs every 3 seconds
  useEffect(() => {
    // Clear any existing interval first
    if (pollIntervalRef.current) {
      clearInterval(pollIntervalRef.current);
      pollIntervalRef.current = null;
    }

    // Don't start polling if there are no jobs
    if (asyncJobs.size === 0) {
      return;
    }

    const pollStatuses = async () => {
      const jobsArray = Array.from(asyncJobs.entries());
      
      if (jobsArray.length === 0) {
        return;
      }

      const updatedJobs = new Map(asyncJobs);
      const completedJobs: string[] = [];
      let hasChanges = false;

      // Process all jobs
      for (const [jobId, jobInfo] of jobsArray) {
        try {
          const statusResp = await getAsyncJobStatus(jobId);
          const newStatus = statusResp.data.status;

          if (
            newStatus === 'COMPLETED' ||
            newStatus === 'FAILED' ||
            newStatus === 'CANCELLED'
          ) {
            completedJobs.push(jobId);
            updatedJobs.delete(jobId);
            hasChanges = true;
            
            // Send notification immediately
            onMessage({
              type: newStatus === 'COMPLETED' ? 'success' : 'error',
              text: `Async job: ${jobId} finished with status: ${newStatus}`,
            });
          } else if (jobInfo.status !== newStatus) {
            updatedJobs.set(jobId, { ...jobInfo, status: newStatus });
            hasChanges = true;
          }
        } catch (err) {
          completedJobs.push(jobId);
          updatedJobs.delete(jobId);
          hasChanges = true;
          
          // Send error notification immediately
          onMessage({
            type: 'error',
            text: `Failed to fetch status for job: ${jobId}`,
          });
        }
      }

      // Update state if there were changes
      if (hasChanges) {
        console.log(
          `Updating jobs - completed: ${completedJobs.length}, remaining: ${updatedJobs.size}`
        );
        setAsyncJobs(updatedJobs);

        // Trigger refetch if there were completed jobs
        if (completedJobs.length > 0) {
          onJobComplete();
        }
      }
    };

    // Start polling immediately, then every 3 seconds
    pollStatuses();
    pollIntervalRef.current = setInterval(pollStatuses, 3000);

    return () => {
      if (pollIntervalRef.current) {
        clearInterval(pollIntervalRef.current);
        pollIntervalRef.current = null;
      }
    };
  }, [asyncJobs, onJobComplete, onMessage]); // Include asyncJobs in dependencies

  const addAsyncJob = useCallback((jobId: string, runId: string, initialStatus = 'RUNNING') => {
    setAsyncJobs(prev => {
      const updated = new Map(prev);
      updated.set(jobId, {
        jobId,
        status: initialStatus,
        runId,
      });
      return updated;
    });
  }, []);

  const removeAsyncJob = useCallback((jobId: string) => {
    setAsyncJobs(prev => {
      const updated = new Map(prev);
      updated.delete(jobId);
      return updated;
    });
  }, []);

  const handleCancelAsyncJob = useCallback(async (jobId: string) => {
    const confirmed = confirm(`Are you sure you want to cancel job ${jobId}?`);
    if (!confirmed) return;

    try {
      await cancelAsyncJob(jobId);

      // Remove job from state
      removeAsyncJob(jobId);

      onMessage({
        type: 'success',
        text: `Async job: ${jobId} cancellation requested.`,
      });
      onJobComplete();
    } catch (err) {
      onMessage({ type: 'error', text: 'Failed to cancel async job.' });
    }
  }, [removeAsyncJob, onMessage, onJobComplete]);

  // Helper function to get async job for a specific run
  const getAsyncJobForRun = useCallback((runId: string) => {
    for (const [jobId, jobInfo] of asyncJobs) {
      if (jobInfo.runId === runId) {
        return { jobId, ...jobInfo };
      }
    }
    return null;
  }, [asyncJobs]);

  return {
    asyncJobs,
    addAsyncJob,
    removeAsyncJob,
    cancelAsyncJob: handleCancelAsyncJob,
    getAsyncJobForRun,
    hasActiveJobs: asyncJobs.size > 0,
  };
}