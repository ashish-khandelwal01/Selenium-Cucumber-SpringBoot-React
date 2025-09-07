import { useState, useEffect, useCallback } from "react";
import { listFeatures, viewFeatureFile, updateFeatureFile, createFeatureFile } from "@/api/featureFileApi";

export function useFeatures() {
  // All useState calls first - in consistent order
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

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

  const createFile = useCallback(async (fileName, fileContent) => {
    // Use parameters instead of state to avoid dependency issues
    const nameToUse = fileName || selectedFile;
    const contentToUse = fileContent || content;

    if (!nameToUse || !contentToUse) return;

    setSaving(true);
    setError(null);
    try {
      await createFeatureFile(nameToUse, contentToUse);
      // Refresh file list after creating
      await fetchFiles();
      return { success: true, message: "File created successfully!" };
    } catch (err) {
      const errorMessage = `Failed to create file: ${nameToUse}`;
      setError(errorMessage);
      console.error("Error creating file:", err);
      return { success: false, message: errorMessage };
    } finally {
      setSaving(false);
    }
  }, [selectedFile, content, fetchFiles]);

  const updateContent = useCallback((newContent) => {
    setContent(newContent);
  }, []);

  const clearSelection = useCallback(() => {
    setSelectedFile(null);
    setContent("");
    setError(null);
  }, []);

  // useEffect calls last - in consistent order
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
    createFile,
    updateContent,
    clearSelection
  };
}