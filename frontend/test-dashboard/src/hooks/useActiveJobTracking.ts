import { useState, useEffect, useCallback, useRef } from "react";
import { getJobStatusSummary } from "@/api/jobTrackingApi";

// Types for better TypeScript support
interface JobStatusSummary {
  totalActiveJobs: number;
  asyncJobs: number;
  syncJobs: number;
}

interface JobStatusUpdate {
  totalActiveJobs: number;
  asyncJobs: number;
  syncJobs: number;
  timestamp: string;
}

export function useActiveJobTracking() {
  const [total, setTotal] = useState(0);
  const [asyncJobs, setAsyncJobs] = useState(0);
  const [syncJobs, setSyncJobs] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const fetchActiveJobs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getJobStatusSummary();
      const data = response.data;
      setTotal(data?.totalActiveJobs ?? 0);
      setAsyncJobs(data?.asyncJobs ?? 0);
      setSyncJobs(data?.syncJobs ?? 0);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch active jobs');
      setTotal(0);
      setAsyncJobs(0);
      setSyncJobs(0);
    } finally {
      setLoading(false);
    }
  }, []);

  const startPolling = useCallback(() => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current);
    }

    pollingIntervalRef.current = setInterval(() => {
      fetchActiveJobs();
    }, 5000);
  }, [fetchActiveJobs]);

  const stopPolling = useCallback(() => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current);
      pollingIntervalRef.current = null;
    }
  }, []);

  const connectSSE = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    stopPolling();

    try {
      const eventSource = new EventSource('/api/jobs/updates');
      eventSourceRef.current = eventSource;

      eventSource.onopen = () => {
        console.log('SSE connection established');
        setIsConnected(true);
        setError(null);

        if (reconnectTimeoutRef.current) {
          clearTimeout(reconnectTimeoutRef.current);
          reconnectTimeoutRef.current = null;
        }
      };

      eventSource.addEventListener('job-status-update', (event) => {
        try {
          const data: JobStatusUpdate = JSON.parse(event.data);
          setTotal(data.totalActiveJobs);
          setAsyncJobs(data.asyncJobs);
          setSyncJobs(data.syncJobs);
          setLoading(false);

          console.log('Job status updated:', data);
        } catch (parseError) {
          console.error('Failed to parse SSE data:', parseError);
        }
      });

      eventSource.onerror = (event) => {
        console.error('SSE connection error:', event);
        setIsConnected(false);

        // Start polling as fallback
        startPolling();

        if (!reconnectTimeoutRef.current) {
          reconnectTimeoutRef.current = setTimeout(() => {
            console.log('Attempting to reconnect SSE...');
            connectSSE();
          }, 10000);
        }
      };

    } catch (connectionError) {
      console.error('Failed to establish SSE connection:', connectionError);
      setError('Failed to establish real-time connection');
      setIsConnected(false);

      startPolling();

      if (!reconnectTimeoutRef.current) {
        reconnectTimeoutRef.current = setTimeout(() => {
          connectSSE();
        }, 10000);
      }
    }
  }, [startPolling, stopPolling]);

  const disconnectSSE = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    stopPolling();
    setIsConnected(false);
  }, [stopPolling]);

  // Initialize connection on mount
  useEffect(() => {
    // First fetch current data
    fetchActiveJobs().then(() => {
      // Then establish SSE connection
      connectSSE();
    });

    // Cleanup on unmount
    return () => {
      disconnectSSE();
    };
  }, [fetchActiveJobs, connectSSE, disconnectSSE]);

  const refreshData = useCallback(() => {
    fetchActiveJobs();
  }, [fetchActiveJobs]);

  const reconnect = useCallback(() => {
    disconnectSSE();
    setTimeout(() => {
      connectSSE();
    }, 1000);
  }, [disconnectSSE, connectSSE]);

  return {
    total,
    asyncJobs,
    syncJobs,
    loading,
    error,
    isConnected,
    fetchActiveJobs: refreshData,
    reconnect
  };
}