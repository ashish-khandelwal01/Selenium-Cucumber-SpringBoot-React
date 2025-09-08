import { useState, useEffect, useCallback } from "react";
import { listSheets, viewSheetData, editSheetDataFile } from "@/api/excelDataApi";

export function useDataSheet() {
  const [sheets, setSheets] = useState<string[]>([]);
  const [selectedSheet, setSelectedSheet] = useState<string | null>(null);
  const [content, setContent] = useState<any[][]>([]); // rows & columns
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch list of available sheets
  const fetchSheets = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listSheets();
      setSheets(response.data || []);
    } catch (err) {
      setError("Failed to load sheets");
      console.error("Error fetching sheets:", err);
      setSheets([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load data from a specific sheet
  const loadSheet = useCallback(async (sheetName: string) => {
    if (!sheetName) return;

    setLoading(true);
    setError(null);
    try {
      const response = await viewSheetData(sheetName);
      console.log("=== API RESPONSE DEBUG ===");
      console.log("Full response object:", response);
      console.log("Response.data:", response.data);
      console.log("Response.data type:", typeof response.data);
      console.log("Is response.data array?:", Array.isArray(response.data));
      
      if (response.data && response.data.length > 0) {
        console.log("First element of response.data:", response.data[0]);
        console.log("Is first element an array?:", Array.isArray(response.data[0]));
        console.log("Type of first element:", typeof response.data[0]);
      }
      
      // Directly use the response data since backend should return proper 2D array
      const processedData = response.data || [];
      
      console.log("Setting content to:", processedData);
      setContent(processedData);
      setSelectedSheet(sheetName);
    } catch (err) {
      setError(`Failed to load sheet: ${sheetName}`);
      console.error("Error loading sheet:", err);
      setContent([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Save data back to the sheet
  const saveSheet = useCallback(
    async (sheetName: string, data: any[][]) => {
      if (!sheetName || !data) return;

      setSaving(true);
      setError(null);
      try {
        await editSheetDataFile(sheetName, data);
        return { success: true, message: "Sheet saved successfully!" };
      } catch (err) {
        const errorMessage = `Failed to save sheet: ${sheetName}`;
        setError(errorMessage);
        console.error("Error saving sheet:", err);
        return { success: false, message: errorMessage };
      } finally {
        setSaving(false);
      }
    },
    []
  );

  const updateContent = useCallback((newContent: any[][]) => {
    setContent(newContent);
  }, []);

  const clearSelection = useCallback(() => {
    setSelectedSheet(null);
    setContent([]);
    setError(null);
  }, []);

  useEffect(() => {
    fetchSheets();
  }, [fetchSheets]);

  return {
    // State
    sheets,
    selectedSheet,
    content,
    loading,
    saving,
    error,

    // Actions
    setSelectedSheet,
    fetchSheets,
    loadSheet,
    saveSheet,
    updateContent,
    clearSelection,
  };
}
