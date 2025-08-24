import { useState, useEffect, useCallback, useRef } from "react";
import { getJobStatusSummary, createReconnectingSSE } from "@/api/jobTrackingApi";

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

  const sseConnectionRef = useRef<any>(null);
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
    // Disconnect any existing connection
    if (sseConnectionRef.current) {
      sseConnectionRef.current.disconnect();
    }

    stopPolling();

    try {
      // Create the reconnecting SSE connection
      sseConnectionRef.current = createReconnectingSSE(
        // onMessage callback
        (data: JobStatusUpdate) => {
          console.log('Job status updated via SSE:', data);
          setTotal(data.totalActiveJobs);
          setAsyncJobs(data.asyncJobs);
          setSyncJobs(data.syncJobs);
          setLoading(false);
          setError(null);
        },
        // onConnectionChange callback
        (connected: boolean) => {
          console.log('SSE connection status:', connected ? 'Connected' : 'Disconnected');
          setIsConnected(connected);

          if (connected) {
            setError(null);
            stopPolling(); // Stop polling when SSE is connected
          } else {
            // Start polling as fallback when SSE is disconnected
            console.log('Starting polling fallback...');
            startPolling();
          }
        },
        // reconnectDelay
        5000 // 5 seconds
      );

      // Start the SSE connection
      sseConnectionRef.current.connect();

    } catch (connectionError) {
      console.error('Failed to establish SSE connection:', connectionError);
      setError('Failed to establish real-time connection');
      setIsConnected(false);

      // Fallback to polling
      startPolling();
    }
  }, [startPolling, stopPolling]);

  const disconnectSSE = useCallback(() => {
    if (sseConnectionRef.current) {
      sseConnectionRef.current.disconnect();
      sseConnectionRef.current = null;
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
    console.log('Manual reconnect requested...');
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