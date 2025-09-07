import React, { useRef } from "react";
import Editor from "@monaco-editor/react";
import { useFeatures } from "@/hooks/useFeatureFile";
import Button from "@/components/ui/button";

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
    updateContent
  } = useFeatures();

  const editorRef = useRef<any>(null);
  const monacoRef = useRef<any>(null);

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

    // Add Ctrl+S / Cmd+S shortcut for saving
    const saveShortcutHandler = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
        e.preventDefault();
        handleSave();
      }
    };
    editor.getDomNode()?.addEventListener('keydown', saveShortcutHandler);

    // Clean up on unmount
    editor.onDidDispose(() => {
      editor.getDomNode()?.removeEventListener('keydown', saveShortcutHandler);
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
    let insideScenarioBlock = false; // TRUE when inside Background/Scenario/Examples block
    let hasSeenStepsInCurrentBlock = false; // TRUE when we've seen steps in current scenario

    console.log("=== VALIDATION START ===");

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

        console.log(`Line ${lineNum}: FEATURE detected - Reset scenario context`);

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

      // Handle feature description lines (As a user, I want, So that)
      if (inFeatureDescription && (trimmedLine.startsWith('As ') || trimmedLine.startsWith('I want') || trimmedLine.startsWith('So that'))) {
        console.log(`Line ${lineNum}: Valid feature description line`);
        return; // These are valid feature description lines
      }

      // Rule validation (Gherkin 6)
      if (trimmedLine.startsWith('Rule:')) {
        inTable = false;
        inFeatureDescription = false;
        insideScenarioBlock = false;
        hasSeenStepsInCurrentBlock = false;

        console.log(`Line ${lineNum}: RULE detected - Reset scenario context`);

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
        insideScenarioBlock = true; // ENTER scenario block
        hasSeenStepsInCurrentBlock = false;

        console.log(`Line ${lineNum}: BACKGROUND detected - Enter scenario block`);
        return;
      }

      // Scenario validation - this starts a new scenario context
      if (trimmedLine.match(/^(Scenario|Scenario Outline|Scenario Template):/)) {
        hasScenario = true;
        inTable = false;
        inFeatureDescription = false;
        insideScenarioBlock = true; // ENTER scenario block
        hasSeenStepsInCurrentBlock = false; // Reset for new scenario

        console.log(`Line ${lineNum}: SCENARIO detected - Enter scenario block, reset steps`);

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

      // Examples validation - STAY in scenario block, don't reset anything!
      if (trimmedLine.startsWith('Examples:')) {
        inTable = false;
        inFeatureDescription = false;
        // DON'T change insideScenarioBlock or hasSeenStepsInCurrentBlock!

        console.log(`Line ${lineNum}: EXAMPLES detected - Stay in scenario block, steps=${hasSeenStepsInCurrentBlock}`);
        return;
      }

      // Tag validation - tags are scenario-related
      if (trimmedLine.startsWith('@')) {
        inTable = false;
        inFeatureDescription = false;
        // Tags don't affect scenario context

        console.log(`Line ${lineNum}: TAG detected`);

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
        hasSeenStepsInCurrentBlock = true; // Mark that we've seen steps

        console.log(`Line ${lineNum}: STEP detected - Mark hasSeenSteps=true`);

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

        // Check for proper indentation (steps should be indented)
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

        console.log(`Line ${lineNum}: TABLE ROW detected`);

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

      // Check for unrecognized syntax - CRITICAL SECTION
      if (trimmedLine &&
          !trimmedLine.match(/^(Feature|Rule|Scenario|Scenario Outline|Scenario Template|Background|Examples|Given|When|Then|And|But|\*|@|\|)/) &&
          !trimmedLine.match(/^\s*\<.*\>\s*$/) && // parameter placeholders
          !trimmedLine.match(/^(As |I want|So that)/) && // Feature description lines
          !inMultilineString) {

        // Default to warning
        let severity = monaco.MarkerSeverity.Warning;
        let message = "Unrecognized Gherkin syntax. This line doesn't match any known Gherkin keywords.";

        // STRICT ERROR inside any scenario block
        if (insideScenarioBlock) {
          severity = monaco.MarkerSeverity.Error;
          message = hasSeenStepsInCurrentBlock
            ? `INVALID SYNTAX: After steps in a scenario, only valid Gherkin keywords (Given/When/Then/And/But), tables (|...|), docstrings ("""), or Examples are allowed. Found: "${trimmedLine}"`
            : `INVALID SYNTAX: Inside scenario blocks, only valid Gherkin keywords are allowed. Found: "${trimmedLine}"`;
        }

        console.log(`Line ${lineNum}: INVALID SYNTAX - "${trimmedLine}" - insideScenarioBlock=${insideScenarioBlock}, hasSeenSteps=${hasSeenStepsInCurrentBlock}, severity=${severity === monaco.MarkerSeverity.Error ? 'ERROR' : 'WARNING'}`);

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

    console.log(`=== VALIDATION END - Found ${markers.length} markers ===`);
    console.log('Markers:', markers.map(m => `Line ${m.startLineNumber}: ${m.message} (${m.severity === monaco.MarkerSeverity.Error ? 'ERROR' : 'WARNING'})`));

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

    console.log("SAVE ATTEMPT - Errors:", summary.errors, "Warnings:", summary.warnings);

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
        <h3 className="mb-4 text-lg font-semibold text-gray-100">Feature Files</h3>
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
    </div>
  );
};

// Validation summary component
const ValidationSummary = ({ getValidationSummary }: { getValidationSummary: () => { errors: number, warnings: number } }) => {
  const [summary, setSummary] = React.useState({ errors: 0, warnings: 0 });

  React.useEffect(() => {
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

export default Feature;

