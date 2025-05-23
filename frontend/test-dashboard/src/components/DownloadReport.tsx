import { downloadReportZip } from "@/api/reportApi"; 

const handleDownload = async (runId: string) => {
  try {
    const response = await downloadReportZip(runId);
    const blob = new Blob([response.data], { type: "application/zip" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `test-report-${runId}.zip`;
    a.click();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Download failed:", error);
    alert("Failed to download report.");
  }
};

export default handleDownload;

