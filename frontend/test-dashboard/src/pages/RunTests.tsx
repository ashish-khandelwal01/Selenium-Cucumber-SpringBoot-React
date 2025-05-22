import React, { useState } from 'react';
import { Card, CardContent } from '../components/ui/card';
import { useTags } from '../hooks/useTags';
import { runTests } from '../api/testExecutionApi'; // Add this API call

const RunTestsPage = () => {
  const { tags, loading, error } = useTags();
  const [selectedTag, setSelectedTag] = useState('');
  const [running, setRunning] = useState(false);
  const [message, setMessage] = useState('');

  const runTestsExecution = async () => {
    if (!selectedTag) {
      setMessage('Please select a tag.');
      return;
    }
    setMessage('');
    setRunning(true);
    try {
      await runTests(selectedTag);
      setMessage('Test run triggered successfully!');
    } catch (err) {
      setMessage('Failed to trigger test run.');
      console.error(err);
    } finally {
      setRunning(false);
    }
  };

  return (
    <main className="p-6">
      <Card>
        <CardContent>
          <h2 className="text-xl font-semibold mb-4">Run Tests by Tag</h2>

          {loading && <p>Loading tags...</p>}
          {error && <p className="text-red-500">{error}</p>}

          {!loading && !error && (
            <>
              <select
                value={selectedTag}
                onChange={(e) => setSelectedTag(e.target.value)}
                className="w-full p-2 mb-4 border rounded"
              >
                <option value="">Select a tag</option>
                {tags.map((tag, idx) => (
                  <option key={idx} value={tag.name}>
                    {tag.name}
                  </option>
                ))}
              </select>

              {message && <p className="mb-4 text-center text-sm text-blue-600">{message}</p>}

              <button
                onClick={runTestsExecution}
                disabled={running}
                className="w-full px-6 py-2 rounded bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:bg-gray-400"
              >
                {running ? 'Running...' : 'Run Tests'}
              </button>
            </>
          )}
        </CardContent>
      </Card>
    </main>
  );
};

export default RunTestsPage;
