import type { TestRun } from '../types/TestRun';
import type { BackendStatus } from '../types/TestRun';
import { Button } from './ui/button';
import { formatDuration, formatDate } from '../utils/RunCardUtil';

type UIStatus = 'RUNNING' | 'PASSED' | 'FAILED' | 'CANCELLED' | 'PENDING';

const mapBackendStatusToUIStatus = (status: BackendStatus): UIStatus => {
  if (status === 'Execution Successful' || status === 'Rerun Successful') return 'PASSED';
  if (status === 'Execution Completed with Failures' || status === 'Rerun Completed with Failures') return 'FAILED';
  if (status === 'RUNNING') return 'RUNNING';
  if (status === 'CANCELLED') return 'CANCELLED';
  return 'FAILED';
};

const statusTextColor = {
  RUNNING: 'text-blue-600',
  PASSED: 'text-green-600',
  FAILED: 'text-red-600',
  CANCELLED: 'text-gray-600',
  PENDING: 'text-yellow-600',
};

type TestRunCardProps = TestRun;

const TestRunCard = ({
  runId,
  status,
  triggeredBy,
  durationSeconds,
  startTime,
  onView,
}: TestRunCardProps) => {
  const uiStatus = mapBackendStatusToUIStatus(status);
  const formattedDuration = formatDuration(durationSeconds);
  const formattedStartTime = formatDate(startTime);

  return (
    <tr className="border-t border-gray-200 text-sm">
      <td className="py-2">{runId}</td>
      <td className={`py-2 font-medium ${statusTextColor[uiStatus]}`}>{uiStatus}</td>
      <td className="py-2">{triggeredBy}</td>
      <td className="py-2">{formattedDuration}</td>
      <td className="py-2">{formattedStartTime}</td>
      <td className="py-2">
          <button size="sm"
            onClick={() => onView({ runId, status, triggeredBy, durationSeconds, startTime })}
          >
            View
          </button>
      </td>
    </tr>
  );
};

export default TestRunCard;
