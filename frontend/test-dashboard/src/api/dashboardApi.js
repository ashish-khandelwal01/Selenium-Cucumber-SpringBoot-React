/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module dashboardApi
 */
 
import { createApi } from "./createApi";

const dashboardApi = createApi("/dashboard");


/**
 * Fetches all test runs from the dashboard API.
 *
 * @function getAllRuns
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing all runs.
 */
export const getAllRuns = () => 
    dashboardApi.get(`/runs`);


/**
 * Fetches all test runs from the dashboard API with Pagination.
 *
 * @function getAllRuns
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing all runs.
 */
export const getAllRunsByPages = ({ page, size }) => {
  return dashboardApi.get(`/runs/pages`, {
    params: {
      page,
      size,
    },
  });
};


/**
 * Fetches the latest test runs from the dashboard API.
 *
 * @function getLatestRuns
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the latest runs.
 */
export const getLatestRuns = () => 
    dashboardApi.get(`/latest`);

/**
 * Retrieves dashboard statistics.
 *
 * @function getStats
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing dashboard statistics.
 */
export const getStats = () => 
    dashboardApi.get(`/stats`);

/**
 * Fetches a specific test run by its ID.
 *
 * @function getRunById
 * @param {string|number} runId - The unique identifier of the test run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the test run details.
 */
export const getRunById = (runId) => 
    dashboardApi.get(`/runs/${runId}`);

/**
 * Retrieves a weekly summary from the dashboard API.
 *
 * @function getWeeklySummary
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the weekly summary.
 */
export const getWeeklySummary = () => 
    dashboardApi.get(`/weekly-summary`);

/**
 * Retrieves pass/fail pie chart data from the dashboard API.
 *
 * @function getPassFailPie
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing pass/fail pie chart data.
 */
export const getPassFailPie = () => 
    dashboardApi.get(`/pass-fail-pie`);

/**
 * Retrieves data about the top test failures from the dashboard API.
 *
 * @function getTopFailures
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing top failure data.
 */
export const getTopFailures = () => 
    dashboardApi.get(`/top-failures`);