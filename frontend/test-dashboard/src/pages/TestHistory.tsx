import { useState, useEffect, useRef } from 'react';
import { useTestRunHistory } from '@/hooks/useTestRunHistory';
import { formatDuration } from '@/utils/RunCardUtil';
import { rerunTests } from '../api/testRerunApi';
import {
  rerunAsync,
  getAsyncJobStatus,
  cancelAsyncJob,
} from '../api/asyncTestApi';

export default function TestRunHistoryPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedRunForRerun, setSelectedRunForRerun] = useState<any | null>(
    null
  );
  const [selectedRunForFailures, setSelectedRunForFailures] = useState<
    any | null
  >(null);
  const [isAsync, setIsAsync] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // New state for user feedback messages
  const [asyncJobId, setAsyncJobId] = useState<string | null>(null);
  const [asyncJobStatus, setAsyncJobStatus] = useState<string | null>(null);
  const [message, setMessage] = useState<{
    type: 'success' | 'error';
    text: string;
  } | null>(null);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const refreshIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const { runs, totalPages, loading, refetch } = useTestRunHistory(page, size);

  // Auto-hide message after 4 seconds
  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  // Auto Refresh
  useEffect(() => {
    if (autoRefresh) {
      refreshIntervalRef.current = setInterval(() => {
        refetch();
      }, 60000);
    } else {
      if (refreshIntervalRef.current) {
        clearInterval(refreshIntervalRef.current);
        refreshIntervalRef.current = null;
      }
    }
    return () => {
      if (refreshIntervalRef.current) clearInterval(refreshIntervalRef.current);
    };
  }, [autoRefresh, refetch]);

  // === Poll async job status===
  useEffect(() => {
    let pollInterval: NodeJS.Timeout | null = null;
    if (asyncJobId) {
      const pollStatus = async () => {
        try {
          const statusResp = await getAsyncJobStatus(asyncJobId);
          setAsyncJobStatus(statusResp.data.status);
          if (
            statusResp.data.status === 'COMPLETED' ||
            statusResp.data.status === 'FAILED' ||
            statusResp.data.status === 'CANCELLED'
          ) {
            setAsyncJobId(null);
            setAsyncJobStatus(null);
            setMessage({
              type: 'success',
              text: `Async job ${asyncJobId} finished with status: ${statusResp.data.status}`,
            });
            refetch();
          }
        } catch (err) {
          console.error('Failed to get async job status:', err);
          setMessage({
            type: 'error',
            text: 'Failed to fetch async job status.',
          });
          setAsyncJobId(null);
          setAsyncJobStatus(null);
        }
      };

      pollStatus();
      pollInterval = setInterval(pollStatus, 3000);
    }
    return () => {
      if (pollInterval) clearInterval(pollInterval);
    };
  }, [asyncJobId, refetch]);

  const handleRerun = async () => {
    if (!selectedRunForRerun) return;
    setIsLoading(true);
    try {
      if (isAsync) {
        const response = await rerunAsync(selectedRunForRerun.runId);

        const jobId = response.data.jobId || response.data;
        setAsyncJobId(jobId);
        setAsyncJobStatus('STARTED');
        setMessage({
          type: 'success',
          text: `✅ Async rerun for run ${selectedRunForRerun.runId} triggered.`,
        });
      } else {
        await rerunTests(selectedRunForRerun.runId);
        setMessage({
          type: 'success',
          text: `✅ Sync rerun for run ${selectedRunForRerun.runId} triggered.`,
        });
        refetch();
      }
    } catch (error) {
      console.error('Rerun failed:', error);
      setMessage({ type: 'error', text: '❌ Failed to trigger rerun.' });
    } finally {
      setIsLoading(false);
      setSelectedRunForRerun(null);
      setIsAsync(false);
    }
  };

  const handleCancelAsyncJob = async () => {
    if (!asyncJobId) return;
    const confirmed = confirm(
      `Are you sure you want to cancel job ${asyncJobId}?`
    );
    if (!confirmed) return;
    try {
      console.log('Cancelling async job:', asyncJobId);
      await cancelAsyncJob(asyncJobId);
      setMessage({
        type: 'success',
        text: `Async job ${asyncJobId} cancellation requested.`,
      });
      setAsyncJobId(null);
      setAsyncJobStatus(null);
      refetch();
    } catch (err) {
      console.error('Cancel async job failed:', err);
      setMessage({ type: 'error', text: 'Failed to cancel async job.' });
    }
  };

  return (
    <div className="p-6 bg-gray-900 min-h-screen text-gray-100 relative">
      <h1 className="text-3xl font-bold mb-6">Test Run History</h1>

      {/* Feedback Message */}
      {message && (
        <div
          className={`fixed top-6 left-1/2 transform -translate-x-1/2 px-6 py-3 rounded shadow-lg font-semibold max-w-lg w-full text-center z-50 ${
            message.type === 'success'
              ? 'bg-green-700 text-green-100'
              : 'bg-red-700 text-red-100'
          }`}
          role="alert"
        >
          {message.text}
        </div>
      )}

      <div className="mb-6 flex items-center gap-4">
        <label className="inline-flex items-center cursor-pointer select-none">
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={() => setAutoRefresh(!autoRefresh)}
            disabled={!!asyncJobId}
            className="form-checkbox text-blue-500 h-5 w-5"
          />
          <span className="ml-2 text-gray-300 text-sm">
            Auto Refresh (every 60s)
          </span>
        </label>

        {asyncJobId && (
          <div className="ml-auto flex items-center gap-4 text-sm">
            <span>
              Async Job <code className="font-mono">{asyncJobId}</code> Status:{' '}
              <strong>{asyncJobStatus ?? 'Checking...'}</strong>
            </span>
            <button
              onClick={handleCancelAsyncJob}
              className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded"
            >
              Cancel Async Job
            </button>
          </div>
        )}
      </div>

      {/* Controls */}
      <div className="mb-6 flex flex-wrap gap-4">
        <div>
          <label className="block text-sm text-gray-400 mb-1">Page Size</label>
          <select
            value={size}
            onChange={(e) => {
              setSize(Number(e.target.value));
              setPage(0);
            }}
            className="bg-gray-800 border border-gray-600 text-gray-100 text-sm rounded px-2 py-2"
          >
            {[10, 20, 50, 100].map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm text-gray-400 mb-1">
            Jump to Page
          </label>
          <select
            value={page}
            onChange={(e) => setPage(Number(e.target.value))}
            className="bg-gray-800 border border-gray-600 text-gray-100 text-sm rounded px-2 py-2"
          >
            {Array.from({ length: totalPages }, (_, i) => (
              <option key={i} value={i}>
                Page {i + 1}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Table */}
      {loading ? (
        <p className="text-gray-400">Loading...</p>
      ) : (
        <div className="overflow-x-auto rounded-lg shadow border border-gray-700">
          <table className="min-w-full bg-gray-800 text-sm">
            <thead className="bg-gray-700 text-gray-300 uppercase tracking-wider text-xs">
              <tr>
                <th className="px-6 py-3 text-left border-b">Run ID</th>
                <th className="px-6 py-3 text-left border-b">Tags</th>
                <th className="px-6 py-3 text-left border-b">Start Time</th>
                <th className="px-6 py-3 text-left border-b">Duration</th>
                <th className="px-6 py-3 text-left border-b">Total</th>
                <th className="px-6 py-3 text-left border-b">Passed</th>
                <th className="px-6 py-3 text-left border-b">Failed</th>
                <th className="px-6 py-3 text-left border-b">Status</th>
                <th className="px-6 py-3 text-left border-b">
                  Failed Scenarios
                </th>
              </tr>
            </thead>
            <tbody>
              {runs.map((run) => (
                <tr
                  key={run.runId}
                  className="hover:bg-gray-700 transition cursor-pointer"
                  onClick={() => {
                    if (run.tags?.includes('Rerun')) {
                      alert('⚠️ Rerun tests cannot be rerun again.');
                      return;
                    }
                    setSelectedRunForRerun(run);
                  }}
                >
                  <td className="px-6 py-4 border-b font-mono text-yellow-300">
                    {run.runId}
                  </td>
                  <td className="px-6 py-4 border-b">
                    {run.tags?.length ? run.tags : '—'}
                  </td>
                  <td className="px-6 py-4 border-b">
                    {new Date(run.startTime).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 border-b">
                    {formatDuration(run.durationSeconds)}
                  </td>
                  <td className="px-6 py-4 border-b">{run.total}</td>
                  <td className="px-6 py-4 border-b">{run.passed}</td>
                  <td className="px-6 py-4 border-b">{run.failed}</td>
                  <td className="px-6 py-4 border-b">
                    <span
                      className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${
                        run.status === 'PASSED'
                          ? 'bg-green-700 text-green-100'
                          : run.status === 'FAILED'
                          ? 'bg-red-700 text-red-100'
                          : 'bg-gray-600 text-gray-200'
                      }`}
                    >
                      {run.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 border-b">
                    {run.failureScenarios?.length ? (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedRunForFailures(run);
                        }}
                        className="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded text-xs"
                      >
                        View ({run.failureScenarios.length})
                      </button>
                    ) : (
                      <span className="text-gray-500 text-xs">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      <div className="mt-6 flex justify-between items-center">
        <button
          onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
          disabled={page === 0}
          className="px-4 py-2 rounded bg-blue-600 text-white font-medium hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed"
        >
          Prev
        </button>
        <span className="text-sm text-gray-400">
          Page {page + 1} of {totalPages}
        </span>
        <button
          onClick={() =>
            setPage((prev) => (prev + 1 < totalPages ? prev + 1 : prev))
          }
          disabled={page + 1 >= totalPages}
          className="px-4 py-2 rounded bg-blue-600 text-white font-medium hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed"
        >
          Next
        </button>
      </div>

      {/* Rerun Modal */}
      {selectedRunForRerun && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
          <div className="bg-gray-800 text-white rounded-xl shadow-2xl w-[600px] max-w-full p-8 pt-12 relative">
            <button
              onClick={() => {
                setSelectedRunForRerun(null);
                setIsAsync(false);
              }}
              className="absolute top-4 right-4 text-gray-400 hover:text-white text-xl"
            >
              ✖
            </button>
            <h2 className="text-2xl font-semibold mb-4 text-center">
              Rerun Test With Tags
            </h2>
            <p className="mb-6 text-center text-gray-300">
              Are you sure you want to rerun test run{' '}
              <span className="text-yellow-300 font-mono">
                {selectedRunForRerun.runId}
              </span>
              ?
            </p>

            <div className="flex items-center justify-center gap-2 mb-6">
              <input
                id="async-checkbox"
                type="checkbox"
                checked={isAsync}
                onChange={() => setIsAsync(!isAsync)}
                className="form-checkbox text-blue-500 h-4 w-4"
                disabled={isLoading}
              />
              <label htmlFor="async-checkbox" className="text-sm text-gray-300">
                Run asynchronously?
              </label>
            </div>

            <div className="flex justify-center gap-4 relative">
              <button
                onClick={handleRerun}
                disabled={isLoading}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded disabled:opacity-50"
              >
                {isLoading ? 'Rerunning...' : 'Rerun'}
              </button>
              <button
                onClick={() => {
                  setSelectedRunForRerun(null);
                  setIsAsync(false);
                }}
                disabled={isLoading}
                className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded"
              >
                Cancel
              </button>

              {/* Loading spinner overlay */}
              {isLoading && (
                <div className="absolute inset-0 bg-black bg-opacity-40 flex items-center justify-center rounded-xl">
                  <svg
                    className="animate-spin h-8 w-8 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v8H4z"
                    ></path>
                  </svg>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Failed Scenarios Modal */}
      {selectedRunForFailures && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm p-4">
          <div className="bg-gray-800 text-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-auto p-8 pt-12 relative">
            <button
              onClick={() => setSelectedRunForFailures(null)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white text-xl"
            >
              ✖
            </button>
            <h2 className="text-2xl font-semibold mb-4 text-center">
              Failed Scenarios for Run {selectedRunForFailures.runId}
            </h2>

            {selectedRunForFailures.failureScenarios.length ? (
              <ul className="list-disc pl-6 space-y-2 max-h-[70vh] overflow-auto">
                {selectedRunForFailures.failureScenarios.map(
                  (scen: any, idx: number) => (
                    <li key={idx} className="text-gray-300">
                      {scen}
                    </li>
                  )
                )}
              </ul>
            ) : (
              <p className="text-gray-400 text-center">
                No failed scenarios found.
              </p>
            )}
          </div>
          <div className="mt-6 text-right">
            <button
              onClick={() => setSelectedRunForFailures(null)}
              className="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
