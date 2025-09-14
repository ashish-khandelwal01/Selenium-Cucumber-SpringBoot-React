import React, { useRef, useState, useEffect, useCallback } from 'react';
import { Card, CardContent } from '../components/ui/card';
import { useTags } from '../hooks/useTags';
import { useAsyncTestRun } from '../hooks/useAsyncTestRun';
import { useAsyncJobManager } from '../hooks/useAsyncJobManager';
import { runTests } from '../api/testExecutionApi';
import { useDataSheet } from "@/hooks/useDataSheetUpdate";
import Button from "@/components/ui/button";
import { X, AlertCircle, Info } from 'lucide-react';
import ExcelDataModal from '@/components/ExcelDataModal';

const RunTestsPage = () => {
  const { tags, loading, error } = useTags();
  const [selectedTag, setSelectedTag] = useState('');
  const [running, setRunning] = useState(false);
  const [message, setMessage] = useState('');
  const [isAsync, setIsAsync] = useState(false);

  // Async job management
  const handleExternalMessage = useCallback((msg: { type: 'success' | 'error'; text: string }) => {
    setMessage(msg.text);
    // Auto-hide message after 4 seconds
    setTimeout(() => setMessage(''), 4000);
  }, []);

  const {
    asyncJobs,
    addAsyncJob,
    cancelAsyncJob,
    hasActiveJobs,
  } = useAsyncJobManager(() => {}, handleExternalMessage);

  const { isLoading: asyncLoading, runAsyncTests } = useAsyncTestRun({
    onMessage: (msg) => {
      setMessage(msg.text);
      setTimeout(() => setMessage(''), 4000);
    },
    onAsyncJobCreated: (jobId, tag) => {
      addAsyncJob(jobId, tag);
    },
  });

  const runTestsExecution = async () => {
    if (!selectedTag) {
      setMessage('Please select a tag.');
      setTimeout(() => setMessage(''), 4000);
      return;
    }

    setMessage('');

    if (isAsync) {
      // Use async hook for async execution
      await runAsyncTests(selectedTag);
    } else {
      // Original sync execution
      setRunning(true);
      try {
        await runTests(selectedTag);
        setMessage('Test run triggered successfully!');
        setTimeout(() => setMessage(''), 4000);
      } catch (err) {
        setMessage('Failed to trigger test run.');
        console.error(err);
        setTimeout(() => setMessage(''), 4000);
      } finally {
        setRunning(false);
      }
    }
  };

const {
  sheets,
  selectedSheet,
  content,
  loading: loadingSheets,
  saving,
  error: errorSheets,
  setSelectedSheet,
  loadSheet,
  saveSheet,
  updateContent,
  clearSelection,
} = useDataSheet();

  const rows = content;

  const [showDataSheetModal, setShowDataSheetModal] = useState(false);

  const isCurrentlyRunning = running || asyncLoading;

  return (
    <main className="p-6 bg-gray-900 min-h-screen text-gray-100">
      {/* Feedback Message */}
      {message && (
        <div
          className={`fixed top-6 left-1/2 transform -translate-x-1/2 px-6 py-3 rounded shadow-lg font-semibold max-w-lg w-full text-center z-50 ${
            message.includes('successfully') || message.includes('✅')
              ? 'bg-green-700 text-green-100'
              : 'bg-red-700 text-red-100'
          }`}
          role="alert"
        >
          {message}
        </div>
      )}

      <Card className="bg-gray-800 border-gray-700">
        <CardContent className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-semibold text-gray-100">Run Tests by Tag</h2>

            {/* Show active async jobs count */}
            {hasActiveJobs && (
              <div className="bg-blue-800 text-blue-100 px-3 py-1 rounded-full text-sm">
                {asyncJobs.size} async job{asyncJobs.size > 1 ? 's' : ''} running
              </div>
            )}
          </div>

          {loading && <p className="text-gray-400">Loading tags...</p>}
          {error && <p className="text-red-400">{error}</p>}

          {!loading && !error && (
            <>
              <select
                value={selectedTag}
                onChange={(e) => setSelectedTag(e.target.value)}
                className="w-full p-3 mb-4 border border-gray-600 rounded bg-gray-700 text-gray-100 focus:border-blue-500 focus:outline-none"
                disabled={isCurrentlyRunning}
              >
                <option value="">Select a tag</option>
                {tags.map((tag, idx) => (
                  <option key={idx} value={tag.name}>
                    {tag.name}
                  </option>
                ))}
              </select>

              {/* Async Checkbox + Excel Sheet Editor */}
              <div className="flex items-center justify-between mb-6 gap-4">
                {/* Left side: Async Checkbox */}
                <div className="flex items-center gap-2">
                  <input
                    id="async-checkbox"
                    type="checkbox"
                    checked={isAsync}
                    onChange={() => setIsAsync(!isAsync)}
                    className="form-checkbox text-blue-500 h-4 w-4 bg-gray-700 border-gray-600 rounded focus:ring-blue-500"
                    disabled={isCurrentlyRunning}
                  />
                  <label htmlFor="async-checkbox" className="text-sm text-gray-300">
                    Run asynchronously?
                  </label>
                </div>

                <div className="flex items-center gap-2">
                  <h2 className="text-sm font-semibold text-gray-100 whitespace-nowrap">
                    Data Sheet Editor
                  </h2>
                  <select
                    value={selectedSheet ?? ""}
                    onChange={e => loadSheet(e.target.value)}
                    className="p-2 border border-gray-600 rounded bg-gray-700 text-gray-100 focus:border-blue-500 focus:outline-none"
                  >
                    <option value="">-- Select a sheet --</option>
                    {sheets.map(sheet => (
                      <option key={sheet} value={sheet}>
                        {sheet}
                      </option>
                    ))}
                  </select>
                  <button
                      onClick={() => {
                          if (!selectedSheet) {
                            setMessage("Please select a sheet first!");
                            setTimeout(() => setMessage(""), 4000);
                            return;
                          }
                          setShowDataSheetModal(true);
                        }}
                      className="px-3 py-1.5 text-sm bg-green-600 hover:bg-green-700 text-white rounded-md flex items-center gap-1 transition-colors font-medium border border-green-500 shadow-sm"
                      style={{ minWidth: '70px' }}
                    >
                      <span className="font-bold">+</span>
                      <span>Update Data</span>
                    </button>
                    {selectedSheet && (
                        <Button
                        variant="destructive"
                        size="icon"
                        onClick={clearSelection}
                        className="p-1"
                        >
                        <X className="h-4 w-4" />
                        <span className="sr-only">Clear Selection</span>
                        </Button>
                    )}
                </div>
              </div>


              <button
                onClick={runTestsExecution}
                disabled={isCurrentlyRunning}
                className="w-full px-6 py-3 rounded bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed transition-colors relative"
              >
                {isCurrentlyRunning ? (
                  <div className="flex items-center justify-center gap-2">
                    <svg
                      className="animate-spin h-4 w-4 text-white"
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
                    {isAsync ? 'Triggering Async Run...' : 'Running...'}
                  </div>
                ) : (
                  `Run Tests ${isAsync ? '(Async)' : '(Sync)'}`
                )}
              </button>
            </>
          )}

          {/* Active Async Jobs Display */}
          {hasActiveJobs && (
            <div className="mt-6 space-y-3">
              <h3 className="text-lg font-medium text-gray-200 border-b border-gray-600 pb-2">
                Active Async Jobs
              </h3>
              {Array.from(asyncJobs.entries()).map(([jobId, job]) => (
                <div
                  key={jobId}
                  className="flex items-center justify-between bg-gray-700 rounded-lg p-4 border border-gray-600"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 bg-blue-400 rounded-full animate-pulse"></div>
                    <div>
                      <div className="text-sm">
                        <span className="text-gray-300">Job ID: </span>
                        <code className="font-mono text-blue-300">{jobId}</code>
                      </div>
                      <div className="text-xs text-gray-400">
                        Tag: <span className="text-gray-300">{job.runId}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="text-sm">
                      <span className="text-gray-400">Status: </span>
                      <strong className="text-blue-300">{job.status}</strong>
                    </div>
                    <button
                      onClick={() => cancelAsyncJob(jobId)}
                      className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm font-medium transition-colors"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
        {/* Data Sheet Modal */}
      <ExcelDataModal
        isOpen={showDataSheetModal}
        onClose={() => setShowDataSheetModal(false)}
        sheetName={selectedSheet}      // the sheet currently selected
        rows={rows}                    // 2D array from your hook
        updateContent={updateContent}  // updates the hook content
        saveSheet={saveSheet}          // saves data through your hook
      />
    </main>

  );
};

export default RunTestsPage;