import React, { useState, useEffect } from 'react';
import { X, Clock, User, Tag, Play, AlertCircle } from 'lucide-react';
import { getActiveJobs } from '../api/jobTrackingApi';

const RunningJobsModal = ({ isOpen, onClose, totalJobs }) => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      fetchActiveJobs();
    }
  }, [isOpen]);

  const fetchActiveJobs = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getActiveJobs();
      setJobs(response.data || []);
    } catch (err) {
      setError('Failed to fetch active jobs');
      console.error('Error fetching active jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'RUNNING':
        return <Play className="w-4 h-4 text-green-500" />;
      case 'PENDING':
        return <Clock className="w-4 h-4 text-yellow-500" />;
      default:
        return <AlertCircle className="w-4 h-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'RUNNING':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const getJobTypeColor = (type) => {
    return type === 'ASYNC' ? 'bg-blue-100 text-blue-800' : 'bg-purple-100 text-purple-800';
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-6xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div className="flex items-center space-x-2">
            <Clock className="w-6 h-6 text-blue-500" />
            <h2 className="text-xl font-semibold">Running Jobs ({totalJobs})</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-auto max-h-[calc(90vh-120px)]">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              <span className="ml-2 text-gray-600">Loading jobs...</span>
            </div>
          ) : error ? (
            <div className="text-center py-8">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <p className="text-red-600 font-medium">{error}</p>
              <button
                onClick={fetchActiveJobs}
                className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
              >
                Retry
              </button>
            </div>
          ) : jobs.length === 0 ? (
            <div className="text-center py-12">
              <Clock className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-600 mb-2">No Jobs Running</h3>
              <p className="text-gray-500">All quiet on the automation front!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {/* Refresh Button */}
              <div className="flex justify-end mb-4">
                <button
                  onClick={fetchActiveJobs}
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors flex items-center space-x-2"
                >
                  <Clock className="w-4 h-4" />
                  <span>Refresh</span>
                </button>
              </div>

              {/* Jobs Table */}
              <div className="overflow-x-auto">
                <table className="w-full border-collapse bg-white">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="text-left p-4 font-medium text-gray-700">Job ID</th>
                      <th className="text-left p-4 font-medium text-gray-700">Type</th>
                      <th className="text-left p-4 font-medium text-gray-700">Status</th>
                      <th className="text-left p-4 font-medium text-gray-700">Tag</th>
                      <th className="text-left p-4 font-medium text-gray-700">Run ID</th>
                      <th className="text-left p-4 font-medium text-gray-700">Created By</th>
                      <th className="text-left p-4 font-medium text-gray-700">Started</th>
                      <th className="text-left p-4 font-medium text-gray-700">Thread</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jobs.map((job, index) => (
                      <tr key={job.jobId} className={`border-b hover:bg-gray-50 ${index % 2 === 0 ? 'bg-white' : 'bg-gray-25'}`}>
                        <td className="p-4">
                          <div className="font-mono text-sm text-gray-800">
                            {job.jobId.substring(0, 8)}...
                          </div>
                          <div className="text-xs text-gray-500">
                            {job.jobId}
                          </div>
                        </td>
                        <td className="p-4">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getJobTypeColor(job.type)}`}>
                            {job.type}
                          </span>
                        </td>
                        <td className="p-4">
                          <div className="flex items-center space-x-2">
                            {getStatusIcon(job.status)}
                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(job.status)}`}>
                              {job.status}
                            </span>
                          </div>
                        </td>
                        <td className="p-4">
                          <div className="flex items-center space-x-1">
                            <Tag className="w-3 h-3 text-gray-400" />
                            <span className="text-sm text-gray-700">{job.tag || 'N/A'}</span>
                          </div>
                        </td>
                        <td className="p-4">
                          <span className="font-mono text-sm text-gray-700">
                            {job.runId || 'N/A'}
                          </span>
                        </td>
                        <td className="p-4">
                          <div className="flex items-center space-x-1">
                            <User className="w-3 h-3 text-gray-400" />
                            <span className="text-sm text-gray-700">{job.createdBy || 'System'}</span>
                          </div>
                        </td>
                        <td className="p-4">
                          <span className="text-sm text-gray-700">
                            {formatDateTime(job.startTime)}
                          </span>
                        </td>
                        <td className="p-4">
                          <span className="font-mono text-xs text-gray-500">
                            {job.threadName || 'N/A'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Summary */}
              <div className="mt-6 p-4 bg-blue-50 rounded-lg">
                <div className="flex items-center space-x-4 text-sm">
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                    <span className = "text-sm text-gray-700">Running: {jobs.filter(j => j.status === 'RUNNING').length}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                    <span className = "text-sm text-gray-700">Pending: {jobs.filter(j => j.status === 'PENDING').length}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                    <span className = "text-sm text-gray-700">Async: {jobs.filter(j => j.type === 'ASYNC').length}</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <div className="w-3 h-3 bg-purple-500 rounded-full"></div>
                    <span className = "text-sm text-gray-700">Sync: {jobs.filter(j => j.type === 'SYNC').length}</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RunningJobsModal;