import { useState, useCallback } from 'react';
import { runTests } from '../api/testExecutionApi.js';

export const useRunTestsByTags = () => {
  const [running, setRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const runTestsByTags = useCallback(async (tags: string[]) => {
    setRunning(true);
    setError(null);
    setSuccess(null);
    try {
      await runTests({ tags });
      setSuccess('Test run triggered successfully!');
    } catch (err) {
      setError('Failed to trigger test run.');
      console.error(err);
    } finally {
      setRunning(false);
    }
  }, []);

  return { running, error, success, runTestsByTags };
};
