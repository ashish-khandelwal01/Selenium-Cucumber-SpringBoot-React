/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module reportApi
 */
 
import { createApi } from "./createApi";

const reportsApi = createApi("/reports");

/**
 * Downloads a report as a ZIP file for a specific test run.
 *
 * @function downloadReportZip
 * @param {string|number} runId - The unique identifier of the test run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the ZIP file.
 */
export const downloadReportZip = (runId) =>
    reportsApi.get(`/${runId}/download`, { responseType: "blob" });

/**
 * Retrieves a list of all available reports.
 *
 * @function listReports
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the list of reports.
 */
export const listReports = () =>
    reportsApi.get(`/list`);

/**
 * Views a specific report for a given test run.
 *
 * @function viewReport
 * @param {string|number} runId - The unique identifier of the test run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the report details.
 */
export const viewReport = (runId) =>
    reportsApi.get(`/${runId}/view`, { responseType: "blob" });