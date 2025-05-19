/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module dashboardApi
 */
 
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/dashboard'; // or your deployed Spring Boot server


/**
 * Fetches all test runs from the dashboard API.
 *
 * @function getAllRuns
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing all runs.
 */
export const getAllRuns = () => 
    axios.get(`${BASE_URL}/runs`);

/**
 * Fetches the latest test runs from the dashboard API.
 *
 * @function getLatestRuns
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the latest runs.
 */
export const getLatestRuns = () => 
    axios.get(`${BASE_URL}/latest`);

/**
 * Retrieves dashboard statistics.
 *
 * @function getStats
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing dashboard statistics.
 */
export const getStats = () => 
    axios.get(`${BASE_URL}/stats`);

/**
 * Fetches a specific test run by its ID.
 *
 * @function getRunById
 * @param {string|number} runId - The unique identifier of the test run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the test run details.
 */
export const getRunById = (runId) => 
    axios.get(`${BASE_URL}/runs/${runId}`);

/**
 * Retrieves a weekly summary from the dashboard API.
 *
 * @function getWeeklySummary
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing the weekly summary.
 */
export const getWeeklySummary = () => 
    axios.get(`${BASE_URL}/weekly-summary`);

/**
 * Retrieves pass/fail pie chart data from the dashboard API.
 *
 * @function getPassFailPie
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing pass/fail pie chart data.
 */
export const getPassFailPie = () => 
    axios.get(`${BASE_URL}/pass-fail-pie`);

/**
 * Retrieves data about the top test failures from the dashboard API.
 *
 * @function getTopFailures
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the response containing top failure data.
 */
export const getTopFailures = () => 
    axios.get(`${BASE_URL}/top-failures`);