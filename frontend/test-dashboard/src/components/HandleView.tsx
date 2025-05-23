import { viewReport } from "@/api/reportApi";
 
const handleView = async (runId: string) => {
  try {
    const response = await viewReport(runId);
    const blob = new Blob([response.data], { type: "text/html" });
    const url = window.URL.createObjectURL(blob);
    window.open(url, "_blank");
  } catch (error) {
    console.error("View failed:", error);
    alert("Failed to view report.");
  }
};

export default handleView;