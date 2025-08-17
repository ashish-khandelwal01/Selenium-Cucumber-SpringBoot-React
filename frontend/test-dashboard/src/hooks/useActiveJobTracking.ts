import { useState, useEffect, useCallback, useRef } from "react";
import { getJobStatusSummary } from "@/api/jobTrackingApi";

// Types for better TypeScript support
interface JobStatusSummary {
  totalActiveJobs: number;
  asyncJobs: number;
  syncJobs: number;
}

export function useActiveJobTracking() {
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const fetchActiveJobs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getJobStatusSummary();
      setTotal(response.data?.totalActiveJobs ?? 0);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch active jobs');
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshData = useCallback(() => {
    fetchActiveJobs();
  }, [fetchActiveJobs]);

  return {
    total,
    loading,
    error,
    fetchActiveJobs: refreshData
  };
}