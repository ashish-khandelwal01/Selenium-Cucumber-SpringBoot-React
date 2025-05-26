import { useEffect, useState, useCallback } from "react";
import { getAllRunsByPages } from "@/api/dashboardApi";

export function useTestRunHistory(page: number, size: number) {
  const [runs, setRuns] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchRuns = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getAllRunsByPages({ page, size });
      setRuns(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      setRuns([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    fetchRuns();
  }, [fetchRuns]); 

  return { runs, totalPages, loading, refetch: fetchRuns };
}
