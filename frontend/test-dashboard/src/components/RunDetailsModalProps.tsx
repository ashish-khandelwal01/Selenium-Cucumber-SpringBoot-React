import { Card, CardContent } from './ui/card';
import { formatDuration } from '../utils/RunCardUtil';

interface RunDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  run: any; // or use your TestRun type
}

const RunDetailsModal = ({ isOpen, onClose, run }: RunDetailsModalProps) => {
  if (!isOpen || !run) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <Card className="w-[500px]">
        <CardContent className="p-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-semibold">Run Details - {run.runId}</h3>
            <button className="text-gray-500 hover:text-gray-700" onClick={onClose}>
              Close
            </button>
          </div>
          <div className="mt-4 space-y-2 text-sm">
            <p><strong>Status:</strong> {run.status}</p>
            <p><strong>Duration:</strong> {formatDuration(run.durationSeconds)}</p>
            <p><strong>Started At:</strong> {new Date(run.startTime).toLocaleString()}</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default RunDetailsModal;
