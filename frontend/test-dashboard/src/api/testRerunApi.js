/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module testRerunApi
 */
 
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/tests/rerun'; // or your deployed Spring Boot server

/**
 * Sends a POST request to rerun tests for a specific test run.
 *
 * @function rerunTests
 * @param {string|number} runId - The unique identifier of the test run to rerun.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the rerun request.
 */
export const rerunTests = (runId) =>
    axios.post(`${BASE_URL}`, null, {
        params: { runId },
    });
    
/**
 * Sends a POST request to rerun only the failed tests for a specific test run.
 *
 * @function rerunFailedTests
 * @param {string|number} runId - The unique identifier of the test run whose failed tests are to be rerun.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the failed tests rerun request.
 */
export const rerunFailedTests = (runId) =>
    axios.post(`${BASE_URL}/failed`, null, {
        params: { runId },
    });