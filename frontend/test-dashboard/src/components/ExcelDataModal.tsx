import { useState, useEffect } from "react";
import { X, AlertCircle, Info, Plus, Trash2 } from "lucide-react";

// Mock DataGrid component since react-data-grid isn't available
const DataGrid = ({ columns, rows, onRowsChange, onCellClick, style, headerRowHeight, rowHeight }) => {
  const [editingCell, setEditingCell] = useState(null);
  const [editValue, setEditValue] = useState("");

  const handleCellClick = (rowIndex, colKey) => {
    setEditingCell({ rowIndex, colKey });
    setEditValue(rows[rowIndex]?.[colKey] || "");
    onCellClick && onCellClick({ row: rows[rowIndex], column: { key: colKey } });
  };

  const handleCellSubmit = () => {
    if (editingCell) {
      const { rowIndex, colKey } = editingCell;
      const newRows = [...rows];
      newRows[rowIndex] = { ...newRows[rowIndex], [colKey]: editValue };
      onRowsChange(newRows, { indexes: [rowIndex] });
      setEditingCell(null);
      setEditValue("");
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleCellSubmit();
    } else if (e.key === 'Escape') {
      setEditingCell(null);
      setEditValue("");
    }
  };

  return (
    <div style={style} className="border border-gray-300 rounded-md overflow-auto bg-white">
      <table className="w-full border-collapse">
        <thead>
          <tr style={{ height: headerRowHeight }} className="bg-gray-100">
            {columns.map((col) => (
              <th
                key={col.key}
                className="border border-gray-300 px-3 py-2 text-left font-semibold text-sm text-gray-800"
                style={{ minWidth: col.minWidth, width: col.width }}
              >
                {col.name}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={row.id || rowIndex} style={{ height: rowHeight }} className="hover:bg-gray-50">
              {columns.map((col) => (
                <td
                  key={col.key}
                  className="border border-gray-200 px-3 py-2 cursor-pointer text-gray-900"
                  onClick={() => handleCellClick(rowIndex, col.key)}
                >
                  {editingCell?.rowIndex === rowIndex && editingCell?.colKey === col.key ? (
                    <input
                      type="text"
                      value={editValue}
                      onChange={(e) => setEditValue(e.target.value)}
                      onBlur={handleCellSubmit}
                      onKeyDown={handleKeyDown}
                      className="w-full bg-white border border-blue-400 rounded px-1 py-0 outline-none focus:ring-1 focus:ring-blue-500 text-gray-900"
                      autoFocus
                    />
                  ) : (
                    <span className="text-sm text-gray-900">{row[col.key] || ""}</span>
                  )}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

interface ExcelDataModalProps {
  isOpen: boolean;
  onClose: () => void;
  sheetName?: string;
  rows?: string[][];
  updateContent: (rows: string[][]) => void;
  saveSheet: (sheetName: string, rows: string[][]) => Promise<void>;
}

const ExcelDataModal = ({
  isOpen,
  onClose,
  sheetName,
  rows = [],
  updateContent,
  saveSheet,
}: ExcelDataModalProps) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [editableRows, setEditableRows] = useState<Record<string, string | number>[]>([]);
  const [headers, setHeaders] = useState<string[]>([]);

  // Parse and validate the data
  let parsedRows: string[][] = [];

  if (rows && rows.length > 0) {
    if (Array.isArray(rows)) {
      if (rows.length > 0 && Array.isArray(rows[0])) {
        parsedRows = rows.map(row => row.map(cell => String(cell || "")));
      } else {
        parsedRows = rows.map(item => [String(item)]);
      }
    }
  }

  // Update state when rows prop changes
  useEffect(() => {
    if (parsedRows.length > 0) {
      const newHeaders = parsedRows[0] || [];
      setHeaders(newHeaders);

      const rowObjects = parsedRows.slice(1).map((row, rowIndex) => {
        const obj: Record<string, string | number> = { id: rowIndex };
        row.forEach((val, colIndex) => {
          obj[colIndex.toString()] = val || "";
        });
        return obj;
      });
      setEditableRows(rowObjects);
    }
  }, [rows]);

  // Generate columns from headers
  const columns = headers.map((colName, i) => ({
    key: i.toString(),
    name: colName || `Column ${i + 1}`,
    editable: true,
    width: 150,
    minWidth: 100,
    resizable: true,
  }));

  // Early return if modal is not open
  if (!isOpen) return null;

  // Return empty data message if no data
  if (parsedRows.length === 0) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900">No Data</h2>
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 rounded-full transition-colors"
            >
              <X className="w-5 h-5 text-gray-500" />
            </button>
          </div>
          <p className="text-gray-500 mb-4">No data available to display for this sheet.</p>
          <button
            onClick={onClose}
            className="w-full px-4 py-2 bg-gray-500 hover:bg-gray-600 text-white rounded transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    );
  }

  const handleRowsChange = (updatedRows, changeInfo) => {

    setEditableRows(updatedRows);

    // Update parent component immediately
    const newContent = [
      headers,
      ...updatedRows.map((rowObj) =>
        columns.map((col) => rowObj[col.key]?.toString() || "")
      ),
    ];
    updateContent(newContent);
  };

  const addNewRow = () => {
    const newRow: Record<string, string | number> = { id: editableRows.length };
    headers.forEach((_, colIndex) => {
      newRow[colIndex.toString()] = "";
    });

    const newRows = [...editableRows, newRow];
    setEditableRows(newRows);

    const newContent = [
      headers,
      ...newRows.map((rowObj) =>
        columns.map((col) => rowObj[col.key]?.toString() || "")
      ),
    ];
    updateContent(newContent);
  };

  const deleteRow = (rowIndex: number) => {
    const newRows = editableRows.filter((_, index) => index !== rowIndex);
    // Re-assign IDs
    const updatedRows = newRows.map((row, index) => ({ ...row, id: index }));
    setEditableRows(updatedRows);

    const newContent = [
      headers,
      ...updatedRows.map((rowObj) =>
        columns.map((col) => rowObj[col.key]?.toString() || "")
      ),
    ];
    updateContent(newContent);
  };

  const addNewColumn = () => {
    const newColumnIndex = headers.length;
    const newColumnName = `Column ${newColumnIndex + 1}`;

    const newHeaders = [...headers, newColumnName];
    setHeaders(newHeaders);

    const newRows = editableRows.map(row => ({
      ...row,
      [newColumnIndex.toString()]: ""
    }));
    setEditableRows(newRows);

    const newContent = [
      newHeaders,
      ...newRows.map((rowObj) =>
        newHeaders.map((_, colIndex) => rowObj[colIndex.toString()]?.toString() || "")
      ),
    ];
    updateContent(newContent);
  };

  const handleSave = async () => {
    if (!sheetName) {
      setError("Sheet name is missing");
      return;
    }

    setSaving(true);
    setError("");
    try {
      const updatedRows = [
        headers,
        ...editableRows.map((rowObj) =>
          columns.map((col) => rowObj[col.key]?.toString() || "")
        ),
      ];

      await saveSheet(sheetName, updatedRows);
      updateContent(updatedRows);
      onClose();
    } catch (err) {
      console.error("Save error:", err);
      setError("Failed to save sheet");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-6xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div className="flex items-center space-x-2">
            <span className="text-2xl">📊</span>
            <h2 className="text-xl font-semibold text-gray-900">
              {sheetName ? `Editing Sheet: ${sheetName}` : "Excel Sheet Editor"}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4 overflow-auto max-h-[calc(90vh-180px)]">
          {/* Info Box */}
          <div className="flex items-start gap-3 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <Info className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
            <div className="text-sm text-blue-800">
              <p className="font-medium mb-1">💡 How to Edit:</p>
              <ul className="list-disc list-inside space-y-1">
                <li>Click on any cell to start editing</li>
                <li>Press <kbd className="px-1 py-0.5 bg-white rounded border">Enter</kbd> to save changes</li>
                <li>Press <kbd className="px-1 py-0.5 bg-white rounded border">Esc</kbd> to cancel editing</li>
                <li>Use the + buttons to add new rows or columns</li>
                <li>Click "Save Sheet" when done to save all changes</li>
              </ul>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-2 mb-4">
            <button
              onClick={addNewRow}
              className="flex items-center gap-2 px-3 py-2 bg-green-500 hover:bg-green-600 text-white rounded text-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              Add Row
            </button>
            <button
              onClick={addNewColumn}
              className="flex items-center gap-2 px-3 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded text-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              Add Column
            </button>
          </div>

          {/* Error Message */}
          {error && (
            <div className="flex items-center space-x-2 p-3 bg-red-50 border border-red-200 rounded-md text-red-700">
              <AlertCircle className="w-4 h-4 text-red-500 flex-shrink-0" />
              <span className="text-sm">{error}</span>
            </div>
          )}

          {/* DataGrid */}
          <div className="relative">
            <DataGrid
              columns={columns}
              rows={editableRows}
              onRowsChange={handleRowsChange}
              style={{ height: '400px', width: '100%' }}
              headerRowHeight={40}
              rowHeight={35}
            />

            {/* Row delete buttons */}
            <div className="absolute -right-8 top-10" style={{ height: '400px' }}>
              {editableRows.map((_, index) => (
                <div
                  key={index}
                  className="flex items-center justify-center"
                  style={{ height: '35px' }}
                >
                  <button
                    onClick={() => deleteRow(index)}
                    className="p-1 text-red-500 hover:text-red-700 hover:bg-red-50 rounded"
                    title="Delete row"
                  >
                    <Trash2 className="w-3 h-3" />
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-between items-center mt-4">
            <div className="text-sm text-gray-600">
              {editableRows.length} rows × {headers.length} columns
            </div>
            <div className="flex gap-2">
              <button
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-100 transition-colors"
                disabled={saving}
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded flex items-center gap-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={saving}
              >
                {saving ? (
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                ) : (
                  "Save Sheet"
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExcelDataModal;