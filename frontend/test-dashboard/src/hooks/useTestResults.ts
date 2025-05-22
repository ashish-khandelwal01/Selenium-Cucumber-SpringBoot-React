import { useState, useCallback, useEffect } from 'react';
import { getWeeklySummary } from '../api/dashboardApi';
import type { DailySummary, WeeklySummary } from '../types/TestRun';

export const useTestResults = () => {
  const [runs, setRuns] = useState<WeeklySummary | null>(null);
  const [runsDaily, setDailyRuns] = useState<DailySummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDailySummary = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getWeeklySummary();
      const raw: WeeklySummary = res.data;

      setRuns(raw);
      setDailyRuns(raw.dailySummaries);

    } catch (err) {
      setError(err + ' Failed to load test runs.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDailySummary();
  }, [fetchDailySummary]);

  return { runsDaily, loading, error, fetchDailySummary };
};
