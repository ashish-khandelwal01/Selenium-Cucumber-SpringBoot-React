import { useState, useEffect, useCallback } from "react";
import { listFeatures, viewFeatureFile, updateFeatureFile } from "@/api/featureFileApi";

export function useFeatures() {
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // Fetch all feature files
  const fetchFiles = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listFeatures();
      setFiles(response.data || []);
    } catch (err) {
      setError("Failed to load feature files");
      console.error("Error fetching files:", err);
      setFiles([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load content of a specific file
  const loadFile = useCallback(async (fileName) => {
    if (!fileName) return;

    setLoading(true);
    setError(null);
    try {
      const response = await viewFeatureFile(fileName);
      setContent(response.data || "");
      setSelectedFile(fileName);
    } catch (err) {
      setError(`Failed to load file: ${fileName}`);
      console.error("Error loading file:", err);
      setContent("");
    } finally {
      setLoading(false);
    }
  }, []);

  // Save current file content
  const saveFile = useCallback(async () => {
    if (!selectedFile || !content) return;

    setSaving(true);
    setError(null);
    try {
      await updateFeatureFile(selectedFile, content);
      return { success: true, message: "File saved successfully!" };
    } catch (err) {
      const errorMessage = `Failed to save file: ${selectedFile}`;
      setError(errorMessage);
      console.error("Error saving file:", err);
      return { success: false, message: errorMessage };
    } finally {
      setSaving(false);
    }
  }, [selectedFile, content]);

  // Update content in state
  const updateContent = useCallback((newContent) => {
    setContent(newContent);
  }, []);

  // Clear current selection
  const clearSelection = useCallback(() => {
    setSelectedFile(null);
    setContent("");
    setError(null);
  }, []);

  // Initialize - fetch files on mount
  useEffect(() => {
    fetchFiles();
  }, [fetchFiles]);

  return {
    // State
    files,
    selectedFile,
    content,
    loading,
    saving,
    error,

    // Actions
    fetchFiles,
    loadFile,
    saveFile,
    updateContent,
    clearSelection
  };
}