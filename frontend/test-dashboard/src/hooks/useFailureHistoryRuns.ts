import { useEffect, useState, useCallback } from "react";
import { fetchFailureHistoryByPages } from "@/api/failureHistoryApi"; // matches the call below

export function useFailureHistoryRuns(page, size) {
  const [failures, setFailures] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchFailures = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetchFailureHistoryByPages({ page, size }); // ✅ matches the import
      setFailures(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      setFailures([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  useEffect(() => {
    fetchFailures();
  }, [fetchFailures]);

  return { failures, totalPages, loading, refetch: fetchFailures };
}
