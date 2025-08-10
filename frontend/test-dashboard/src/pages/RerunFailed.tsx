import { useState, useCallback } from 'react';
import { useFailureHistoryRuns } from '@/hooks/useFailureHistoryRuns';
import { useTestRunFailedManager } from '@/hooks/useTestRunFailedManager';
import { useAsyncJobManager } from '@/hooks/useAsyncJobManager'; // optional, if async jobs needed
import { formatDuration } from '@/utils/RunCardUtil';

export default function FailedTestRunHistoryPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const { failures, totalPages, loading, refetch } = useFailureHistoryRuns(page, size);

  const {
    autoRefresh,
    setAutoRefresh,
    selectedRunForRerun,
    setSelectedRunForRerun,
    isAsync,
    setIsAsync,
    isLoading,
    selectedRunForFailures,
    setSelectedRunForFailures,
    message,
    handleRerunFailed,
    canRerun,
    handleExternalMessage,
  } = useTestRunFailedManager({
    onMessage: (msg) => {
      console.log('FailedManager message:', msg);
    },
    onAsyncJobCreated: (jobId, runId) => {
      addAsyncJob(jobId, runId);
    },
    onRefetchNeeded: refetch,
  });

  const {
    asyncJobs,
    addAsyncJob,
    cancelAsyncJob,
    getAsyncJobForRun,
    hasActiveJobs,
  } = useAsyncJobManager(refetch, handleExternalMessage);

  const handleRowClick = useCallback(
    (run: any) => {
      const asyncJob = getAsyncJobForRun(run.runId);
      if (asyncJob) return;

      if (!canRerun(run)) {
        alert('⚠️ Rerun tests cannot be rerun again.');
        return;
      }
      setSelectedRunForRerun(run);
    },
    [getAsyncJobForRun, canRerun, setSelectedRunForRerun]
  );

  const handleFailedScenariosClick = useCallback(
    (e: React.MouseEvent, run: any) => {
      e.stopPropagation();
      const asyncJob = getAsyncJobForRun(run.runId);
      if (!asyncJob) {
        setSelectedRunForFailures(run);
      }
    },
    [getAsyncJobForRun, setSelectedRunForFailures]
  );

  return (
    <div className="p-6 bg-gray-900 min-h-screen text-gray-100 relative">
      <h1 className="text-3xl font-bold mb-6">Failed Test Run History</h1>

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

      <div className="mb-6 flex items-center gap-4 relative">
        <label className="inline-flex items-center cursor-pointer select-none">
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={() => setAutoRefresh(!autoRefresh)}
            disabled={hasActiveJobs}
            className="form-checkbox text-blue-500 h-5 w-5"
          />
          <span className="ml-2 text-gray-300 text-sm">
            Auto Refresh (every 60s)
          </span>
        </label>

        {hasActiveJobs && (
          <div className="bg-blue-800 text-blue-100 px-3 py-1 rounded-full text-sm">
            {asyncJobs.size} async job{asyncJobs.size > 1 ? 's' : ''} running
          </div>
        )}

        <div className="flex items-center gap-4 ml-auto">
          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Page Size
            </label>
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
      </div>

      {loading ? (
        <p className="text-gray-400">Loading...</p>
      ) : (
        <div className="overflow-x-auto rounded-lg shadow border border-gray-700">
          <table className="min-w-full bg-gray-800 text-sm">
            <thead className="bg-gray-700 text-gray-300 uppercase tracking-wider text-xs">
              <tr>
                <th className="px-6 py-3 text-left border-b">Run ID</th>
                <th className="px-6 py-3 text-left border-b">Failed Count</th>
                <th className="px-6 py-3 text-left border-b">Failed Scenarios</th>
                <th className="px-6 py-3 text-left border-b">Actions</th>
              </tr>
            </thead>
            <tbody>
              {failures.map((run) => {
                const asyncJob = getAsyncJobForRun(run.runId);
                const hasAsyncJob = asyncJob !== null;

                return (
                  <tr
                    key={run.runId}
                    className={`relative hover:bg-gray-700 transition cursor-pointer ${
                      hasAsyncJob ? 'bg-gray-800' : ''
                    }`}
                    style={{
                      backdropFilter: hasAsyncJob ? 'blur(2px)' : 'none',
                    }}
                    onClick={() => handleRowClick(run)}
                  >
                    <td
                      className={`px-6 py-4 border-b font-mono text-yellow-300 ${
                        hasAsyncJob ? 'opacity-50' : ''
                      }`}
                    >
                      {run.runId}
                    </td>
                    <td
                      className={`px-6 py-4 border-b ${
                        hasAsyncJob ? 'opacity-50' : ''
                      }`}
                    >
                      {run.failureScenarios?.length ? (
                        <button
                          onClick={(e) => handleFailedScenariosClick(e, run)}
                          disabled={hasAsyncJob}
                          className={`bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded text-xs ${
                            hasAsyncJob ? 'opacity-50 cursor-not-allowed' : ''
                          }`}
                        >
                          View ({run.failureScenarios.length})
                        </button>
                      ) : (
                        <span className="text-gray-500 text-xs">—</span>
                      )}
                    </td>
                    <td className="px-6 py-4 border-b">
                      {hasAsyncJob ? (
                        <div className="flex items-center gap-3 bg-gray-700 rounded-lg p-3 border border-gray-600">
                          <div className="flex items-center gap-2">
                            <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse"></div>
                            <span className="text-xs text-gray-200">
                              Job:{' '}
                              <code className="font-mono text-blue-300">
                                {asyncJob.jobId}
                              </code>
                            </span>
                          </div>
                          <div className="text-xs">
                            Status:{' '}
                            <strong className="text-blue-300">
                              {asyncJob.status}
                            </strong>
                          </div>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              cancelAsyncJob(asyncJob.jobId);
                            }}
                            className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-xs font-medium transition-colors"
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <span className="text-gray-500 text-xs">—</span>
                      )}
                    </td>
                  </tr>
                );
              })}
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
              Are you sure you want to rerun failed test run{' '}
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
                onClick={handleRerunFailed}
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
            <h2 className="text-xl font-semibold mb-6 text-center border-b border-gray-700 pb-3">
              Failed Scenarios for Run ID:{' '}
              <span className="text-yellow-300">
                {selectedRunForFailures.runId}
              </span>
            </h2>

            {selectedRunForFailures.failureScenarios?.length > 0 ? (
              <ul className="space-y-3 max-h-[400px] overflow-y-auto px-2 scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-gray-800">
                {selectedRunForFailures.failureScenarios.map(
                  (scenario: string, index: number) => (
                    <li
                      key={index}
                      className="flex items-start gap-2 bg-gray-700 p-3 rounded-lg shadow-sm hover:bg-gray-600"
                    >
                      <span className="text-red-400">❌</span>
                      <span>{scenario}</span>
                    </li>
                  )
                )}
              </ul>
            ) : (
              <p className="text-gray-400 text-center">No failed scenarios.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
