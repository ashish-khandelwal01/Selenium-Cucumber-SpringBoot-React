import React, { useRef, useState, useEffect } from "react";
import Editor from "@monaco-editor/react";
import { useFeatures } from "@/hooks/useFeatureFile";
import Button from "@/components/ui/button";
import { X, AlertCircle } from 'lucide-react';

const Feature = () => {
  const {
    files,
    selectedFile,
    content,
    loading,
    saving,
    error,
    loadFile,
    saveFile,
    createFile,
    updateContent
  } = useFeatures();

  const editorRef = useRef<any>(null);
  const monacoRef = useRef<any>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Monaco editor setup: Gherkin highlighting
  const handleEditorWillMount = (monaco: any) => {
    monacoRef.current = monaco;

    monaco.languages.register({ id: "gherkin" });
    monaco.languages.setMonarchTokensProvider("gherkin", {
      tokenizer: {
        root: [
          [/Feature:.*/, "keyword.feature"],
          [/Rule:.*/, "keyword.rule"],
          [/Background:.*/, "keyword.background"],
          [/Scenario Outline:.*/, "keyword.scenario-outline"],
          [/Scenario Template:.*/, "keyword.scenario-outline"],
          [/Scenario:.*/, "keyword.scenario"],
          [/Examples:.*/, "keyword.examples"],
          [/\b(Given|When|Then|And|But)\b/, "keyword.step"],
          [/\*/, "keyword.step"],
          [/@\w+(\.\w+)*/, "tag"],
          [/#.*/, "comment"],
          [/"[^"]*"/, "string"],
          [/"""/, "string.multiline"],
          [/\|/, "table.delimiter"],
          [/\d+/, "number"],
          [/\<[^>]+\>/, "parameter"],
        ],
      },
    });

    // Define comprehensive theme colors
    monaco.editor.defineTheme("gherkin-theme", {
      base: "vs",
      inherit: true,
      rules: [
        { token: "keyword.feature", foreground: "0000FF", fontStyle: "bold" },
        { token: "keyword.rule", foreground: "4B0082", fontStyle: "bold" },
        { token: "keyword.scenario", foreground: "008000", fontStyle: "bold" },
        { token: "keyword.scenario-outline", foreground: "008000", fontStyle: "bold" },
        { token: "keyword.background", foreground: "800080", fontStyle: "bold" },
        { token: "keyword.examples", foreground: "800080", fontStyle: "bold" },
        { token: "keyword.step", foreground: "FF4500", fontStyle: "bold" },
        { token: "tag", foreground: "FF69B4", fontStyle: "italic" },
        { token: "comment", foreground: "808080", fontStyle: "italic" },
        { token: "string", foreground: "8B4513" },
        { token: "string.multiline", foreground: "8B4513", fontStyle: "italic" },
        { token: "table.delimiter", foreground: "000000", fontStyle: "bold" },
        { token: "number", foreground: "0000FF" },
        { token: "parameter", foreground: "CD853F", fontStyle: "italic" },
      ],
      colors: {}
    });
  };

  // Editor mount
  const handleEditorMount = (editor: any) => {
    editorRef.current = editor;
    runValidation(content, editor);

    // Clean up on unmount
    editor.onDidDispose(() => {
      // Cleanup if needed
    });
  };

  // On content change
  const handleContentChange = (value: string | undefined) => {
    updateContent(value || "");
    if (editorRef.current) {
      runValidation(value || "", editorRef.current);
    }
  };

  // FIXED Comprehensive Gherkin validation
  const runValidation = (text: string, editor: any) => {
    const monaco = monacoRef.current;
    if (!monaco) return;

    const markers: any[] = [];

    if (!text.trim()) {
      monaco.editor.setModelMarkers(editor.getModel(), "gherkin", []);
      return;
    }

    const lines = text.split('\n');
    let hasFeature = false;
    let hasScenario = false;
    let inMultilineString = false;
    let inTable = false;
    let inFeatureDescription = false;
    let previousTableColumns = 0;
    let featureLine = 0;
    let insideScenarioBlock = false;
    let hasSeenStepsInCurrentBlock = false;

    lines.forEach((line, index) => {
      const lineNum = index + 1;
      const trimmedLine = line.trim();
      const originalLine = line;

      // Skip empty lines
      if (!trimmedLine) {
        inTable = false;
        return;
      }

      // Handle multiline strings
      if (trimmedLine === '"""') {
        inMultilineString = !inMultilineString;
        inTable = false;
        return;
      }

      if (inMultilineString) {
        return;
      }

      // Skip comments
      if (trimmedLine.startsWith('#')) {
        inTable = false;
        return;
      }

      // Feature validation
      if (trimmedLine.startsWith('Feature:')) {
        hasFeature = true;
        featureLine = lineNum;
        inTable = false;
        inFeatureDescription = true;
        insideScenarioBlock = false;
        hasSeenStepsInCurrentBlock = false;

        if (!trimmedLine.match(/^Feature:\s+.+/)) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: line.length + 1,
            message: "Feature must have a description",
            severity: monaco.MarkerSeverity.Error,
          });
        }
        return;
      }

      // Handle feature description lines
      if (inFeatureDescription && (trimmedLine.startsWith('As ') || trimmedLine.startsWith('I want') || trimmedLine.startsWith('So that'))) {
        return;
      }

      // Rule validation
      if (trimmedLine.startsWith('Rule:')) {
        inTable = false;
        inFeatureDescription = false;
        insideScenarioBlock = false;
        hasSeenStepsInCurrentBlock = false;

        if (!trimmedLine.match(/^Rule:\s+.+/)) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: line.length + 1,
            message: "Rule must have a description",
            severity: monaco.MarkerSeverity.Error,
          });
        }
        return;
      }

      // Background validation
      if (trimmedLine.startsWith('Background:')) {
        inTable = false;
        inFeatureDescription = false;
        insideScenarioBlock = true;
        hasSeenStepsInCurrentBlock = false;
        return;
      }

      // Scenario validation
      if (trimmedLine.match(/^(Scenario|Scenario Outline|Scenario Template):/)) {
        hasScenario = true;
        inTable = false;
        inFeatureDescription = false;
        insideScenarioBlock = true;
        hasSeenStepsInCurrentBlock = false;

        if (!trimmedLine.match(/^(Scenario|Scenario Outline|Scenario Template):\s+.+/)) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: line.length + 1,
            message: "Scenario must have a description",
            severity: monaco.MarkerSeverity.Error,
          });
        }
        return;
      }

      if (trimmedLine.startsWith('Examples:')) {
        inTable = false;
        inFeatureDescription = false;
        return;
      }

      // Tag validation
      if (trimmedLine.startsWith('@')) {
        inTable = false;
        inFeatureDescription = false;

        const tags = trimmedLine.split(/\s+/);
        tags.forEach(tag => {
          if (!tag.match(/^@[\w\.-]+$/)) {
            markers.push({
              startLineNumber: lineNum,
              startColumn: originalLine.indexOf(tag) + 1,
              endLineNumber: lineNum,
              endColumn: originalLine.indexOf(tag) + tag.length + 1,
              message: `Invalid tag format: ${tag}. Tags should be @tagname`,
              severity: monaco.MarkerSeverity.Error,
            });
          }
        });
        return;
      }

      // Step validation
      if (trimmedLine.match(/^(Given|When|Then|And|But|\*)\s/)) {
        inTable = false;
        inFeatureDescription = false;
        hasSeenStepsInCurrentBlock = true;

        if (!trimmedLine.match(/^(Given|When|Then|And|But|\*)\s+.+/)) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: line.length + 1,
            message: "Step must have a description",
            severity: monaco.MarkerSeverity.Error,
          });
        }

        // Check for proper indentation
        const stepIndent = originalLine.search(/\S/);
        if (stepIndent === 0) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: 5,
            message: "Steps should be indented",
            severity: monaco.MarkerSeverity.Warning,
          });
        }
        return;
      }

      // Table validation
      if (trimmedLine.startsWith('|') && trimmedLine.endsWith('|')) {
        const columns = trimmedLine.split('|').filter(cell => cell.trim() !== '').length;

        if (inTable && previousTableColumns > 0 && columns !== previousTableColumns) {
          markers.push({
            startLineNumber: lineNum,
            startColumn: 1,
            endLineNumber: lineNum,
            endColumn: line.length + 1,
            message: `Table row has ${columns} columns, but previous rows have ${previousTableColumns}`,
            severity: monaco.MarkerSeverity.Error,
          });
        }

        inTable = true;
        previousTableColumns = columns;
        return;
      } else {
        inTable = false;
        previousTableColumns = 0;
      }

      // Check for unrecognized syntax
      if (trimmedLine &&
          !trimmedLine.match(/^(Feature|Rule|Scenario|Scenario Outline|Scenario Template|Background|Examples|Given|When|Then|And|But|\*|@|\|)/) &&
          !trimmedLine.match(/^\s*\<.*\>\s*$/) &&
          !trimmedLine.match(/^(As |I want|So that)/) &&
          !inMultilineString) {

        let severity = monaco.MarkerSeverity.Warning;
        let message = "Unrecognized Gherkin syntax. This line doesn't match any known Gherkin keywords.";

        if (insideScenarioBlock) {
          severity = monaco.MarkerSeverity.Error;
          message = hasSeenStepsInCurrentBlock
            ? `INVALID SYNTAX: After steps in a scenario, only valid Gherkin keywords (Given/When/Then/And/But), tables (|...|), docstrings ("""), or Examples are allowed. Found: "${trimmedLine}"`
            : `INVALID SYNTAX: Inside scenario blocks, only valid Gherkin keywords are allowed. Found: "${trimmedLine}"`;
        }

        markers.push({
          startLineNumber: lineNum,
          startColumn: 1,
          endLineNumber: lineNum,
          endColumn: line.length + 1,
          message: message,
          severity: severity,
        });
      }
    });

    // Structural validations
    if (text.trim() && !hasFeature) {
      markers.push({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: 1,
        endColumn: 1,
        message: "Feature file must start with a 'Feature:' declaration",
        severity: monaco.MarkerSeverity.Error,
      });
    }

    if (hasFeature && !hasScenario) {
      markers.push({
        startLineNumber: featureLine,
        startColumn: 1,
        endLineNumber: featureLine,
        endColumn: 1,
        message: "Feature should contain at least one Scenario",
        severity: monaco.MarkerSeverity.Warning,
      });
    }

    monaco.editor.setModelMarkers(editor.getModel(), "gherkin", markers);
  };

  // Check for validation errors
  const hasValidationErrors = () => {
    if (!monacoRef.current || !editorRef.current) return false;

    const markers = monacoRef.current.editor.getModelMarkers({ owner: "gherkin" });
    return markers.some((m: any) => m.severity === monacoRef.current.MarkerSeverity.Error);
  };

  const getValidationSummary = () => {
    if (!monacoRef.current || !editorRef.current) return { errors: 0, warnings: 0 };

    const markers = monacoRef.current.editor.getModelMarkers({ owner: "gherkin" });
    const errors = markers.filter((m: any) => m.severity === monacoRef.current.MarkerSeverity.Error).length;
    const warnings = markers.filter((m: any) => m.severity === monacoRef.current.MarkerSeverity.Warning).length;

    return { errors, warnings };
  };

  // Save with validation
  const handleSave = async () => {
    if (!monacoRef.current || !editorRef.current) {
      alert("Editor not ready!");
      return;
    }

    const summary = getValidationSummary();

    if (summary.errors > 0) {
      const markers = monacoRef.current.editor.getModelMarkers({ owner: "gherkin" });
      const errorMarkers = markers.filter((m: any) =>
        m.severity === monacoRef.current.MarkerSeverity.Error
      );

      const errorMessages = errorMarkers.slice(0, 5).map((m: any) =>
        `Line ${m.startLineNumber}: ${m.message}`
      ).join('\n');

      const moreErrors = errorMarkers.length > 5 ? `\n... and ${errorMarkers.length - 5} more errors` : '';

      alert(`❌ CANNOT SAVE - Please fix the following Gherkin errors:\n\n${errorMessages}${moreErrors}`);
      return;
    }

    const result = await saveFile();
    if (result.success) {
      alert(`✅ ${result.message}${summary.warnings > 0 ? `\n⚠️  Note: ${summary.warnings} warning(s) found` : ''}`);
    } else {
      alert(`❌ Error: ${result.message}`);
    }
  };

  if (loading && files.length === 0) return <div>Loading feature files...</div>;

  return (
    <div className="flex h-[90vh] bg-gray-900 text-gray-100">
      {/* Sidebar */}
      <div className="w-[250px] border-r border-gray-700 p-3 overflow-auto bg-gray-800">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-100">Feature Files</h3>
          <button
            onClick={() => {
              console.log("Create button clicked!"); // Debug log
              setShowCreateModal(true);
            }}
            className="px-3 py-1.5 text-sm bg-green-600 hover:bg-green-700 text-white rounded-md flex items-center gap-1 transition-colors font-medium border border-green-500 shadow-sm"
            style={{ minWidth: '70px' }} // Ensure minimum width
          >
            <span className="font-bold">+</span>
            <span>Create</span>
          </button>
        </div>
        {error && <div className="text-red-400 text-sm mb-2">{error}</div>}
        {files.length === 0 && !loading && (
          <div className="text-gray-400 text-sm">No feature files found</div>
        )}
        {files.map(file => (
          <div
            key={file}
            className={`cursor-pointer my-1 px-3 py-2 rounded-lg font-medium transition-colors text-sm ${file === selectedFile ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'} ${loading ? 'opacity-50' : ''}`}
            onClick={() => !loading && loadFile(file)}
          >
            <span className="mr-2">📄</span>{file}
          </div>
        ))}
      </div>

      {/* Editor */}
      <div className="flex-1 flex flex-col pb-5">
        {selectedFile ? (
          <>
            <div className="px-4 py-3 border-b border-gray-600 bg-gray-700 flex justify-between items-center">
              <span className="font-bold text-gray-100">📝 {selectedFile}</span>
              <div className="flex items-center gap-3">
                <ValidationSummary getValidationSummary={getValidationSummary} />
                <Button
                  onClick={handleSave}
                  size="sm"
                  className={`px-3 py-1.5 text-sm font-semibold flex items-center gap-2 focus:outline-none shadow ${saving ? 'bg-gray-600 text-gray-300 cursor-not-allowed' : hasValidationErrors() ? 'bg-red-600 text-white hover:bg-red-700' : ''}`}
                  disabled={!selectedFile || saving}
                >
                  {saving ? (
                    <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4l3-3-3-3v4a8 8 0 01-8 8z" />
                    </svg>
                  ) : null}
                  {saving ? "Saving..." : (hasValidationErrors() ? "🚫 Fix Errors First" : "💾 Save")}
                </Button>
              </div>
            </div>
            <Editor
              height="100%"
              language="gherkin"
              theme="gherkin-theme"
              value={content}
              onChange={handleContentChange}
              beforeMount={handleEditorWillMount}
              onMount={handleEditorMount}
              loading={loading}
              options={{
                fontSize: 14,
                lineNumbers: "on",
                minimap: { enabled: false },
                wordWrap: "on",
                scrollBeyondLastLine: true,
                automaticLayout: true,
                tabSize: 2,
                insertSpaces: true,
                renderWhitespace: "selection",
                showFoldingControls: "always",
                padding: { top: 0, bottom: 20 },
              }}
            />
          </>
        ) : (
          <div className="flex items-center justify-center h-full text-gray-400 text-lg">
            👈 Select a feature file to edit
          </div>
        )}
      </div>

      {/* Create Feature Modal */}
      {showCreateModal && (
        <CreateFeatureModal
          isOpen={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          onCreateFeature={createFile}
          onSelectNewFile={(fileName, content) => {
            updateContent(content);
            setShowCreateModal(false);
          }}
        />
      )}
    </div>
  );
};

// Validation summary component
const ValidationSummary = ({ getValidationSummary }: { getValidationSummary: () => { errors: number, warnings: number } }) => {
  const [summary, setSummary] = useState({ errors: 0, warnings: 0 });

  useEffect(() => {
    const interval = setInterval(() => {
      setSummary(getValidationSummary());
    }, 500);

    return () => clearInterval(interval);
  }, [getValidationSummary]);

  if (summary.errors === 0 && summary.warnings === 0) {
    return (
      <span className="text-xs px-3 py-1 rounded-full bg-green-700 text-green-100 border border-green-600 font-semibold">✅ Valid</span>
    );
  }

  return (
    <div className="flex gap-2">
      {summary.errors > 0 && (
        <span className="text-xs px-3 py-1 rounded-full bg-red-700 text-red-100 border border-red-600 font-semibold">
          ❌ {summary.errors} error{summary.errors !== 1 ? 's' : ''}
        </span>
      )}
      {summary.warnings > 0 && (
        <span className="text-xs px-3 py-1 rounded-full bg-yellow-700 text-yellow-100 border border-yellow-600 font-semibold">
          ⚠️ {summary.warnings} warning{summary.warnings !== 1 ? 's' : ''}
        </span>
      )}
    </div>
  );
};

// Create Feature Modal Component - Updated to match Running Jobs Modal styling
const CreateFeatureModal = ({ isOpen, onClose, onCreateFeature, onSelectNewFile }) => {
  const [featureName, setFeatureName] = useState('');
  const [featureContent, setFeatureContent] = useState(`Feature: New feature
  As a user
  I want to do something
  So that I can achieve my goal

  Scenario: First scenario
    Given I have a precondition
    When I perform an action
    Then I should see the expected result`);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');

  const handleCreate = async () => {
    if (!featureName.trim()) {
      setError('Feature name is required');
      return;
    }

    // Add .feature extension if not present
    let fileName = featureName.trim();
    if (!fileName.endsWith('.feature')) {
      fileName += '.feature';
    }

    setCreating(true);
    setError('');

    try {
      const result = await onCreateFeature(fileName, featureContent);
      if (result?.success) {
        // Clear form and load the new file
        onSelectNewFile(fileName, featureContent);
        handleClose();
      } else {
        setError(result?.message || 'Failed to create feature file');
      }
    } catch (err) {
      setError('Failed to create feature file');
    } finally {
      setCreating(false);
    }
  };

  const handleClose = () => {
    setFeatureName('');
    setFeatureContent(`Feature: New feature
  As a user
  I want to do something
  So that I can achieve my goal

  Scenario: First scenario
    Given I have a precondition
    When I perform an action
    Then I should see the expected result`);
    setError('');
    onClose();
  };

  if (!isOpen) return null;

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b">
            <div className="flex items-center space-x-2">
              <span className="text-2xl">📝</span>
              <h2 className="text-xl font-semibold text-gray-900">Create New Feature File</h2>
            </div>
            <button
              onClick={handleClose}
              className="p-2 hover:bg-gray-100 rounded-full transition-colors"
            >
              <X className="w-5 h-5 text-gray-500" />
            </button>
          </div>

          {/* Content */}
          <div className="p-6 space-y-4 overflow-auto max-h-[calc(90vh-180px)]">
            {/* Feature Name Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Feature File Name
              </label>
              <input
                type="text"
                value={featureName}
                onChange={(e) => setFeatureName(e.target.value)}
                placeholder="e.g., user-login or user-login.feature"
                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:bg-white text-gray-800 placeholder-gray-400"
                disabled={creating}
              />
              <p className="text-xs text-gray-500 mt-2">
                📁 .feature extension will be added automatically if not provided
              </p>
            </div>

            {/* Feature Content */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Feature Content
              </label>
              <textarea
                value={featureContent}
                onChange={(e) => setFeatureContent(e.target.value)}
                rows={12}
                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:bg-white text-gray-800 font-mono text-sm resize-none leading-relaxed"
                disabled={creating}
                placeholder="Enter your Gherkin feature content here..."
                style={{
                  lineHeight: '1.6',
                  fontFamily: 'ui-monospace, SFMono-Regular, "SF Mono", Monaco, Consolas, "Liberation Mono", "Courier New", monospace'
                }}
              />
              <div className="mt-2 text-xs text-gray-500 flex items-center space-x-4">
                <span>💡 Tip: Use proper Gherkin syntax with Feature, Scenario, Given/When/Then steps</span>
              </div>
            </div>

            {/* Error Display */}
            {error && (
              <div className="flex items-center space-x-2 p-3 bg-red-50 border border-red-200 rounded-md text-red-700">
                <AlertCircle className="w-4 h-4 text-red-500 flex-shrink-0" />
                <span className="text-sm">{error}</span>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end space-x-3 p-6 border-t bg-gray-50">
            <button
              onClick={handleClose}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-100 transition-colors"
              disabled={creating}
            >
              Cancel
            </button>
            <button
              onClick={handleCreate}
              className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded flex items-center gap-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={!featureName.trim() || creating}
            >
              {creating ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Creating...
                </>
              ) : (
                <>
                  <span>+</span>
                  Create Feature
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    );
  };

export default Feature;