import { useState, useCallback,useEffect } from 'react';
import { listReports } from '../api/reportApi.js';
import type { ListReports } from '../types/TestRun';

export const useListReports = () => {
  const [runs, setRuns] = useState<ListReports[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRunList = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await listReports();
      setRuns(res.data);
    } catch (err) {
      setError(err+'Failed to load test runs.');
    } finally {
      setLoading(false);
    }
  }, []);
  
  useEffect(() => {
    fetchRunList();
  }, [fetchRunList]);


  return { runs, loading, error, fetchRunList };
};
