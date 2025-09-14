import { useState, useCallback,useEffect } from 'react';
import { listReports } from '../api/reportApi.js';
import type { ListReports } from '../types/TestRun';

export const useListReports = () => {
  const [aveExecutionTime, setAveExecutionTime] = useState<ListReports[]>([]);
  const [failedToday, setFailedToday] = useState<ListReports[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRunList = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await listReports();
      setAveExecutionTime(res.data.averageExecutionTime);
      setFailedToday(res.data.failedToday);
    } catch (err) {
      setError(err+'Failed to load test runs.');
    } finally {
      setLoading(false);
    }
  }, []);
  
  useEffect(() => {
    fetchRunList();
  }, [fetchRunList]);


  return { aveExecutionTime, failedToday, loading, error, fetchRunList };
};
