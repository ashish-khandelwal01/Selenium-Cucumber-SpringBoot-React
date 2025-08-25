import React from 'react';
import { X, Clock, Play, CheckCircle, XCircle, AlertCircle, Tag, Calendar, Timer, FileText, Download, Eye } from 'lucide-react';
import { formatDuration } from '../utils/RunCardUtil';
import { useTestRunHistory } from '@/hooks/useTestRunHistory';
import handleDownload from '@/components/DownloadReport';
import handleView from '@/components/HandleView';

interface RunDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  run: any;
}

const RunDetailsModal = ({ isOpen, onClose, run }: RunDetailsModalProps) => {
  const page = 0;
  const size = 10;
  const { runs } = useTestRunHistory(page, size);

  if (!isOpen || !run) return null;

  const currentRun = runs?.find((r: any) => r.runId === run.runId) || run;

  const getStatusIcon = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
      case 'SUCCESS':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'FAILED':
      case 'ERROR':
        return <XCircle className="w-5 h-5 text-red-500" />;
      case 'RUNNING':
        return <Play className="w-5 h-5 text-blue-500" />;
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-500" />;
      default:
        return <AlertCircle className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
      case 'SUCCESS':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
      case 'ERROR':
        return 'bg-red-100 text-red-800';
      case 'RUNNING':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDateTime = (dateString: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const getPassedPercentage = () => {
    if (!currentRun.total || currentRun.total === 0) return 0;
    return Math.round((currentRun.passed / currentRun.total) * 100);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-5xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div className="flex items-center space-x-3">
            <FileText className="w-6 h-6 text-blue-500" />
            <div>
              <h2 className="text-xl text-gray-600 font-semibold">Run Details</h2>
              <p className="text-base text-gray-600 font-mono">{run.runId}</p>
            </div>
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
          <div className="space-y-6">
            {/* Status Section */}
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-medium text-gray-800">Status Overview</h3>
                <div className="flex items-center space-x-2">
                  {getStatusIcon(run.status)}
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(run.status)}`}>
                    {run.status}
                  </span>
                </div>
              </div>

              {/* Test Results Summary */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="bg-white p-3 rounded-lg border">
                  <div className="text-2xl font-bold text-gray-800">{currentRun.total || 0}</div>
                  <div className="text-sm text-gray-600">Total Tests</div>
                </div>
                <div className="bg-white p-3 rounded-lg border">
                  <div className="text-2xl font-bold text-green-600">{currentRun.passed || 0}</div>
                  <div className="text-sm text-gray-600">Passed</div>
                </div>
                <div className="bg-white p-3 rounded-lg border">
                  <div className="text-2xl font-bold text-red-600">{currentRun.failed || 0}</div>
                  <div className="text-sm text-gray-600">Failed</div>
                </div>
                <div className="bg-white p-3 rounded-lg border">
                  <div className="text-2xl font-bold text-blue-600">{getPassedPercentage()}%</div>
                  <div className="text-sm text-gray-600">Success Rate</div>
                </div>
              </div>
            </div>

            {/* Details Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Timing Information */}
              <div className="space-y-4">
                <h3 className="text-lg font-medium text-gray-800 border-b pb-2">Timing Information</h3>

                <div className="space-y-3">
                  <div className="flex items-start space-x-3">
                    <Calendar className="w-4 h-4 text-gray-400 mt-1" />
                    <div>
                      <div className="text-sm font-medium text-gray-700">Started At</div>
                      <div className="text-sm text-gray-600">{formatDateTime(run.startTime)}</div>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3">
                    <Calendar className="w-4 h-4 text-gray-400 mt-1" />
                    <div>
                      <div className="text-sm font-medium text-gray-700">Ended At</div>
                      <div className="text-sm text-gray-600">{formatDateTime(currentRun.endTime)}</div>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3">
                    <Timer className="w-4 h-4 text-gray-400 mt-1" />
                    <div>
                      <div className="text-sm font-medium text-gray-700">Duration</div>
                      <div className="text-sm text-gray-600">{formatDuration(run.durationSeconds)}</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Additional Information */}
              <div className="space-y-4">
                <h3 className="text-lg font-medium text-gray-800 border-b pb-2">Additional Information</h3>

                <div className="space-y-3">
                  <div className="flex items-start space-x-3">
                    <Tag className="w-4 h-4 text-gray-400 mt-1" />
                    <div>
                      <div className="text-sm font-medium text-gray-700">Tags</div>
                      <div className="text-sm text-gray-600">
                        {currentRun.tags?.length ? currentRun.tags : '—'}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Actions Section */}
            <div className="bg-blue-50 rounded-lg p-4">
              <h3 className="text-lg font-medium text-gray-800 mb-4">Reports & Actions</h3>
              <div className="flex space-x-4">
                <button
                  onClick={() => handleView(run.runId)}
                  className="flex items-center space-x-2 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                >
                  <Eye className="w-4 h-4" />
                  <span>View Report</span>
                </button>
                <button
                  onClick={() => handleDownload(run.runId)}
                  className="flex items-center space-x-2 px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
                >
                  <Download className="w-4 h-4" />
                  <span>Download Report</span>
                </button>
              </div>
            </div>

            {/* Progress Bar */}
            {currentRun.total > 0 && (
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="flex justify-between text-sm text-gray-600 mb-2">
                  <span>Test Progress</span>
                  <span>{currentRun.passed}/{currentRun.total} passed</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-green-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${getPassedPercentage()}%` }}
                  ></div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RunDetailsModal;