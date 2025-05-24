import { useState } from "react";
import { useTestRunHistory } from "@/hooks/useTestRunHistory";
import { formatDuration } from "@/utils/RunCardUtil";

export default function TestRunHistoryPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedRunForRerun, setSelectedRunForRerun] = useState<any | null>(null);
  const [selectedRunForFailures, setSelectedRunForFailures] = useState<any | null>(null);

  const { runs, totalPages, loading } = useTestRunHistory(page, size);

  return (
    <div className="p-6 bg-gray-900 min-h-screen text-gray-100 relative">
      <h1 className="text-3xl font-bold mb-6">Test Run History</h1>

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
          <label className="block text-sm text-gray-400 mb-1">Jump to Page</label>
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
                <th className="px-6 py-3 text-left border-b">Failed Scenarios</th>
              </tr>
            </thead>
            <tbody>
              {runs.map((run) => (
                <tr
                  key={run.runId}
                  className="hover:bg-gray-700 transition cursor-pointer"
                  onClick={() => setSelectedRunForRerun(run)}
                >
                  <td className="px-6 py-4 border-b font-mono text-yellow-300">{run.runId}</td>
                  <td className="px-6 py-4 border-b">{run.tags?.length ? run.tags: "—"}</td>
                  <td className="px-6 py-4 border-b">{new Date(run.startTime).toLocaleString()}</td>
                  <td className="px-6 py-4 border-b">{formatDuration(run.durationSeconds)}</td>
                  <td className="px-6 py-4 border-b">{run.total}</td>
                  <td className="px-6 py-4 border-b">{run.passed}</td>
                  <td className="px-6 py-4 border-b">{run.failed}</td>
                  <td className="px-6 py-4 border-b">
                    <span
                      className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${
                        run.status === "PASSED"
                          ? "bg-green-700 text-green-100"
                          : run.status === "FAILED"
                          ? "bg-red-700 text-red-100"
                          : "bg-gray-600 text-gray-200"
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
          onClick={() => setPage((prev) => (prev + 1 < totalPages ? prev + 1 : prev))}
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
              onClick={() => setSelectedRunForRerun(null)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white text-xl"
            >
              ✖
            </button>
            <h2 className="text-2xl font-semibold mb-4 text-center">Rerun Test</h2>
            <p className="mb-6 text-center text-gray-300">
              Are you sure you want to rerun test run{" "}
              <span className="text-yellow-300 font-mono">{selectedRunForRerun.runId}</span>?
            </p>
            <div className="flex justify-center gap-4">
              <button
                onClick={() => {
                  // TODO: call rerun API from your api file here
                  setSelectedRunForRerun(null);
                }}
                className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
              >
                Rerun
              </button>
              <button
                onClick={() => setSelectedRunForRerun(null)}
                className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Failed Scenarios Modal */}
      {selectedRunForFailures && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
          <div className="bg-gray-800 text-white rounded-xl shadow-2xl w-[700px] max-w-full p-8 pt-12 relative animate-fade-in">
            <button
              onClick={() => setSelectedRunForFailures(null)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white text-xl"
            >
              ✖
            </button>
            <h2 className="text-xl font-semibold mb-6 text-center border-b border-gray-700 pb-3">
              Failed Scenarios for Run ID:{" "}
              <span className="text-yellow-300">{selectedRunForFailures.runId}</span>
            </h2>
            {selectedRunForFailures.failureScenarios?.length > 0 ? (
              <ul className="space-y-3 max-h-[400px] overflow-y-auto px-2 scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-gray-800">
                {selectedRunForFailures.failureScenarios.map((scenario: string, index: number) => (
                  <li
                    key={index}
                    className="flex items-start gap-2 bg-gray-700 p-3 rounded-lg shadow-sm hover:bg-gray-600"
                  >
                    <span className="text-red-400">❌</span>
                    <span>{scenario}</span>
                  </li>
                ))}
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
