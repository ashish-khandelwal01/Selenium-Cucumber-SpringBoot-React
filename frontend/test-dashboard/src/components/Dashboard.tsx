import React, { useEffect, useMemo, useState } from 'react';
import { Card, CardContent } from './ui/card';
import { useTestRuns } from '../hooks/useTestRuns';
import { usePassFailPie } from '../hooks/usePassFailPie';
import { useTestResults } from '../hooks/useTestResults';
import { useListReports } from '../hooks/useListTestRuns';
import { useAllTestRuns } from '../hooks/useAllTestRuns';
import { useActiveJobTracking } from '../hooks/useActiveJobTracking';
import RunningJobsModal from './RunningJobsModal';
import TestRunCard from './TestRunCard';
import { formatDuration } from "../utils/RunCardUtil";
import {
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

const Dashboard = () => {
  const { runs, loading, error, fetchLatestRuns } = useTestRuns();
  const {
      total: activeJobsTotal,
      asyncJobs,
      syncJobs,
      loading: loadingJobs,
      error: errorJobs,
      isConnected,
      fetchActiveJobs,
      reconnect
    } = useActiveJobTracking();
  const { total, loading: loadingTotal, fetchRuns } = useAllTestRuns();
  const {
    runs: runs_pie,
    loading: loading_pie,
    error: error_pie,
    total: total_pie,
    fetchPassFailPie,
  } = usePassFailPie();

  const {
    runsDaily,
    loading: loading_testResults,
    error: error_testResults,
    fetchDailySummary,
  } = useTestResults();

  const {
    runs: runsReportList,
    loading: loadingReportList,
    error: errorReportList,
    fetchRunList,
  } = useListReports();

  const [isJobsModalOpen, setIsJobsModalOpen] = useState(false);

  useEffect(() => {
    fetchLatestRuns();
    fetchActiveJobs();
    fetchRuns();
    fetchDailySummary();
    fetchPassFailPie();
    fetchRunList();
  }, [fetchLatestRuns, fetchActiveJobs, fetchRuns, fetchDailySummary, fetchPassFailPie, fetchRunList]);

  const COLORS = ['#3554a5', '#ef4444'];

  const averageExecutionTime = useMemo(() => {
    if (!runsReportList || runsReportList.length === 0) return 0;
    const totalDuration = runsReportList.reduce(
      (acc, run) => acc + run.durationSeconds,
      0
    );
    return (totalDuration / runsReportList.length).toFixed(2);
  }, [runsReportList]);

  const failedToday = useMemo(() => {
    if (!runsReportList || runsReportList.length === 0) return 0;
    const today = new Date();
    const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    return runsReportList.filter((run) => {
      const runDate = new Date(run.startTime);
      return runDate >= todayStart && run.status.includes('Failures');
    }).length;
  }, [runsReportList]);

  return (
    <main className="flex-1 p-6 space-y-6 overflow-auto">
      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4">
            Total Runs
            <br />
            <span className="text-2xl font-bold">{total}</span>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            Failures Today
            <br />
            <span className="text-2xl font-bold">{failedToday}</span>
          </CardContent>
        </Card>
        <Card
          className="cursor-pointer hover:shadow-lg hover:bg-gray-50 transition-all duration-200 relative"
          onClick={() => setIsJobsModalOpen(true)}
        >
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                Running Jobs
                <br />
                <span className="text-2xl font-bold">{activeJobsTotal}</span>
                {/* Connection status indicator */}
                <div className="flex items-center mt-1 space-x-2">
                  <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
                  <span className="text-xs text-gray-500">
                    {isConnected ? 'Live' : 'Disconnected'}
                  </span>
                </div>
              </div>
              <div className="text-gray-400 hover:text-gray-600">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>
            {activeJobsTotal > 0 && (
              <div className="mt-2 pt-2 border-t border-gray-200">
                <div className="flex space-x-4 text-xs text-gray-600">
                  <span>Async: {asyncJobs}</span>
                  <span>Sync: {syncJobs}</span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            Average Execution Time
            <br />
            <span className="text-2xl font-bold">
              {formatDuration(Number(averageExecutionTime))}
            </span>
          </CardContent>
        </Card>
      </div>

      <div className="flex gap-4">
        <Card className="w-[70%]">
          <CardContent className="p-4">
            <h3 className="text-lg font-semibold mb-2">Test Results (This Week)</h3>
            {loading_testResults ? (
              <p>Loading chart...</p>
            ) : error_testResults ? (
              <p className="text-red-500">Error loading test results.</p>
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={runsDaily}>
                  <XAxis dataKey="date" stroke="#ccc" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="passed" stroke="#3b82f6" strokeWidth={2} name="Passed" />
                  <Line type="monotone" dataKey="failed" stroke="#ef4444" strokeWidth={2} name="Failed" />
                </LineChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card className="w-[30%]">
          <CardContent className="p-4">
            <h3 className="text-lg font-semibold mb-2">Results Breakdown</h3>
            {loading_pie && <p>Loading pie chart...</p>}
            {error_pie && <p className="text-red-500">Error loading chart</p>}
            {!loading_pie && !error_pie && (
              <div className="flex items-center">
                <div className="w-2/3">
                  <ResponsiveContainer width="100%" height={250}>
                    <PieChart>
                      <Pie
                        data={runs_pie}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={90}
                        startAngle={90}
                        endAngle={450}
                        stroke="none"
                      >
                        {runs_pie.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value, name) => [`${value}%`, name]} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="w-1/3 pl-4">
                  <ul className="space-y-2">
                    {runs_pie.map((entry, index) => (
                      <li key={index} className="flex items-center space-x-2">
                        <span
                          className="w-3 h-3 rounded-full inline-block"
                          style={{ backgroundColor: COLORS[index % COLORS.length] }}
                        ></span>
                        <span style={{ color: COLORS[index % COLORS.length] }}>
                          {entry.name}
                        </span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent className="p-4">
          <h3 className="text-lg font-semibold mb-4">Recent Activity</h3>
          {loading ? (
            <p>Loading...</p>
          ) : error ? (
            <p className="text-red-500">Error: {error}</p>
          ) : (
            <table className="w-full table-auto text-left">
              <thead className="text-sm text-gray-400">
                <tr>
                  <th className="pb-2">Run ID</th>
                  <th className="pb-2">Status</th>
                  <th className="pb-2">Triggered By</th>
                  <th className="pb-2">Duration</th>
                  <th className="pb-2">Started At</th>
                  <th className="pb-2"></th>
                </tr>
              </thead>
              <tbody>
                {runs.map((run) => (
                  <TestRunCard key={run.runId} {...run} />
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>

      {/* Running Jobs Modal */}
      <RunningJobsModal
        isOpen={isJobsModalOpen}
        onClose={() => setIsJobsModalOpen(false)}
        totalJobs={activeJobsTotal}
      />
    </main>
  );
};

export default Dashboard;