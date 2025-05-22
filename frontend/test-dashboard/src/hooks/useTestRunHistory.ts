import { useEffect, useState } from "react";
import { getAllRunsByPages } from "@/api/dashboardApi";

export function useTestRunHistory(page: number, size: number) {
  const [runs, setRuns] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRuns = async () => {
      setLoading(true);
      try {
        const response = await getAllRunsByPages({ page, size });
        setRuns(response.data.content || []);
        setTotalPages(response.data.totalPages || 0);
      } catch (error) {
        console.error("Error fetching test runs", error);
        setRuns([]);
        setTotalPages(0);
      } finally {
        setLoading(false);
      }
    };

    fetchRuns();
  }, [page, size]);

  return { runs, totalPages, loading };
}
