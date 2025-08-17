import { useState , useEffect, useCallback} from "react";
import { getAllRuns } from "@/api/dashboardApi";

export function useAllTestRuns() {
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchRuns = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getAllRuns();
      setTotal((response.data || []).length);
    } catch (error) {
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRuns();
  }, [fetchRuns]);

  return { total, loading, fetchRuns };
}