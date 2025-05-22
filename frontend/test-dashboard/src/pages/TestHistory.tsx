import { useState } from "react";
import { useTestRunHistory } from "@/hooks/useTestRunHistory";

export default function TestRunHistoryPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedScenarios, setSelectedScenarios] = useState<string[] | null>(null);

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
                <tr key={run.runId} className="hover:bg-gray-700 transition">
                  <td className="px-6 py-4 border-b">{run.runId}</td>
                  <td className="px-6 py-4 border-b">{run.tags?.length ? run.tags : "—"}</td>
                  <td className="px-6 py-4 border-b">
                    {new Date(run.startTime).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 border-b">{run.durationSeconds}</td>
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
                        onClick={() => setSelectedScenarios(run.failureScenarios)}
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

      {/* Modal for Failed Scenarios */}
      {selectedScenarios && (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
          <div className="bg-gray-800 border border-gray-600 rounded-lg shadow-lg p-6 w-96 max-h-[80vh] overflow-y-auto">
            <h2 className="text-lg font-semibold text-white mb-4">Failed Scenarios</h2>
            <ul className="list-disc list-inside space-y-2 text-gray-200 text-sm">
              {selectedScenarios.map((scenario, idx) => (
                <li key={idx}>{scenario}</li>
              ))}
            </ul>
            <button
              onClick={() => setSelectedScenarios(null)}
              className="mt-6 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
