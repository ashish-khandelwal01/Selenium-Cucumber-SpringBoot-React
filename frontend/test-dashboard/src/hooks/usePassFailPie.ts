import { useState, useCallback, useEffect } from 'react';
import { getStats } from '../api/dashboardApi';
import type { PieChartData } from '../types/TestRun';

export const usePassFailPie = () => {
  const [runs, setRuns] = useState<PieChartData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [total, setTotal] = useState<number>(0);

  const fetchPassFailPie = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getStats();
      const raw = res.data; // { passed: number; failed: number }

      const total = raw.passed + raw.failed;
      console.log('ABCTotal runs:', total, 'Passed:', raw.passed, 'Failed:', raw.failed);
      setTotal(total);
      const pieData: PieChartData[] = total > 0 ? [
        { name: 'Passed', value: parseFloat(((raw.passed / total) * 100).toFixed(1)) },
        { name: 'Failed', value: parseFloat(((raw.failed / total) * 100).toFixed(1)) },
      ] : [];

      setRuns(pieData);
    } catch (err) {
      setError(err + ' Failed to load test runs.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPassFailPie();
  }, [fetchPassFailPie]);

  return { runs, loading, error, total, fetchPassFailPie };
};
