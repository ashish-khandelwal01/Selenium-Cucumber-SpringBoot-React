import { useState, useCallback } from 'react';
import { getLatestRuns } from '../api/dashboard';
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

  return { runs, loading, error, fetchLatestRuns };
};
