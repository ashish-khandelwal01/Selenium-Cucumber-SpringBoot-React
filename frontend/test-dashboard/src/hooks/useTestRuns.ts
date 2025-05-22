import { useState, useCallback,useEffect } from 'react';
import { getLatestRuns } from '../api/dashboardApi';
import type { TestRun } from '../types/TestRun';

export const useTestRuns = () => {
  const [runs, setRuns] = useState<TestRun[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchLatestRuns = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getLatestRuns();
      setRuns(res.data);
    } catch (err) {
      setError(err+'Failed to load test runs.');
    } finally {
      setLoading(false);
    }
  }, []);
  
  useEffect(() => {
    fetchLatestRuns();
  }, [fetchLatestRuns]);


  return { runs, loading, error, fetchLatestRuns };
};
