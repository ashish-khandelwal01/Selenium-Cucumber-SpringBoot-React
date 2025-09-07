import React from "react";
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

  const handleSave = async () => {
    const result = await saveFile();
    if (result.success) {
      alert(result.message);
    } else {
      alert(result.message);
    }
  };

  if (loading && files.length === 0) {
    return <div>Loading feature files...</div>;
  }

  return (
    <div style={{ display: "flex", height: "90vh" }}>
      {/* Sidebar with file list */}
      <div style={{ width: "200px", borderRight: "1px solid #ccc", padding: "10px" }}>
        <h3>Feature Files</h3>
        {error && <div style={{ color: "red", fontSize: "12px" }}>{error}</div>}
        {files.map(file => (
          <div
            key={file}
            style={{
              cursor: "pointer",
              margin: "5px 0",
              color: file === selectedFile ? "blue" : "black",
              opacity: loading ? 0.5 : 1
            }}
            onClick={() => !loading && loadFile(file)}
          >
            {file}
          </div>
        ))}
      </div>

      {/* Editor */}
      <div style={{ flexGrow: 1 }}>
        {selectedFile ? (
          <>
            <Editor
              height="calc(100% - 50px)"
              language="gherkin"
              theme="vs-light"
              value={content}
              onChange={updateContent}
              loading={loading}
            />
            <button
              onClick={handleSave}
              disabled={!selectedFile || saving}
              style={{
                margin: "10px",
                opacity: saving ? 0.5 : 1
              }}
            >
              {saving ? "Saving..." : "Save"}
            </button>
          </>
        ) : (
          <div style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "100%",
            color: "#666"
          }}>
            Select a feature file to edit
          </div>
        )}
      </div>
    </div>
  );
};

export default Feature;