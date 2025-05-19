/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module testExecutionApi
 */
 
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/tests'; // or your deployed Spring Boot server

/**
 * Sends a POST request to run tests with the specified tags.
 *
 * @function runTests
 * @param {string|string[]} tags - The tag or list of tags to filter the tests to run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the test run request.
 */
export const runTests = (tags) =>
    axios.post(`${BASE_URL}/run`, null, {
        params: { tags },
    });