import React, { useRef } from "react";
import Editor from "@monaco-editor/react";
import { useFeatures } from "@/hooks/useFeatureFile";

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
    <div style={{ display: "flex", height: "90vh" }}>
      {/* Sidebar */}
      <div style={{ width: "250px", borderRight: "1px solid #ccc", padding: "10px", overflow: "auto" }}>
        <h3 style={{ margin: "0 0 15px 0", fontSize: "16px" }}>Feature Files</h3>
        {error && <div style={{ color: "red", fontSize: "12px", marginBottom: "10px" }}>{error}</div>}
        {files.length === 0 && !loading && (
          <div style={{ color: "#666", fontSize: "12px" }}>No feature files found</div>
        )}
        {files.map(file => (
          <div
            key={file}
            style={{
              cursor: "pointer",
              margin: "5px 0",
              padding: "5px",
              borderRadius: "3px",
              color: file === selectedFile ? "white" : "black",
              backgroundColor: file === selectedFile ? "#007ACC" : "transparent",
              opacity: loading ? 0.5 : 1,
              fontSize: "14px"
            }}
            onClick={() => !loading && loadFile(file)}
          >
            📄 {file}
          </div>
        ))}
      </div>

      {/* Editor */}
      <div style={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
        {selectedFile ? (
          <>
            <div style={{
              padding: "10px",
              borderBottom: "1px solid #ccc",
              backgroundColor: "#f5f5f5",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center"
            }}>
              <span style={{ fontWeight: "bold" }}>📝 {selectedFile}</span>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <ValidationSummary getValidationSummary={getValidationSummary} />
                <button
                  onClick={handleSave}
                  disabled={!selectedFile || saving}
                  style={{
                    padding: "8px 16px",
                    backgroundColor: saving ? "#ccc" : (hasValidationErrors() ? "#dc3545" : "#007ACC"),
                    color: "white",
                    border: "none",
                    borderRadius: "4px",
                    cursor: saving ? "not-allowed" : "pointer",
                    fontSize: "14px"
                  }}
                >
                  {saving ? "Saving..." : (hasValidationErrors() ? "🚫 Fix Errors First" : "💾 Save")}
                </button>
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
                scrollBeyondLastLine: false,
                automaticLayout: true,
                tabSize: 2,
                insertSpaces: true,
                renderWhitespace: "selection",
                showFoldingControls: "always",
              }}
            />
          </>
        ) : (
          <div style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "100%",
            color: "#666",
            fontSize: "16px"
          }}>
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
      <span style={{
        fontSize: "12px",
        padding: "4px 8px",
        borderRadius: "12px",
        backgroundColor: "#d4edda",
        color: "#155724",
        border: "1px solid #c3e6cb"
      }}>
        ✅ Valid
      </span>
    );
  }

  return (
    <div style={{ display: "flex", gap: "5px" }}>
      {summary.errors > 0 && (
        <span style={{
          fontSize: "12px",
          padding: "4px 8px",
          borderRadius: "12px",
          backgroundColor: "#f8d7da",
          color: "#721c24",
          border: "1px solid #f5c6cb"
        }}>
          ❌ {summary.errors} error{summary.errors !== 1 ? 's' : ''}
        </span>
      )}
      {summary.warnings > 0 && (
        <span style={{
          fontSize: "12px",
          padding: "4px 8px",
          borderRadius: "12px",
          backgroundColor: "#fff3cd",
          color: "#856404",
          border: "1px solid #ffeaa7"
        }}>
          ⚠️ {summary.warnings} warning{summary.warnings !== 1 ? 's' : ''}
        </span>
      )}
    </div>
  );
};

export default Feature;