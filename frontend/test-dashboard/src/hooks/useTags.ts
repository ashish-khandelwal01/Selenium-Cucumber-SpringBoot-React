import { useCallback, useEffect, useState } from 'react';
import { getTags as fetchTagsApi } from '../api/tagApi.js';

export const useTags = () => {
  const [tags, setTags] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTags = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchTagsApi();
      setTags(res.data);
    } catch (err) {
      setError('Failed to load tags.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  // Automatically fetch tags on mount
  useEffect(() => {
    fetchTags();
  }, [fetchTags]);

  return { tags, loading, error, refreshTags: fetchTags };
};
