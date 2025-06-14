/**
 * API module for interacting with the test dashboard backend.
 * Provides functions to run tests asynchronously, rerun tests, fetch job statuses,
 * cancel jobs, and retrieve dashboard statistics and summaries.
 *
 * @module asyncTestApi
 */
 
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/tests'; // or your deployed Spring Boot server

/**
 * Initiates an asynchronous test run with the specified tags.
 *
 * @function runAsync
 * @param {string|string[]} tags - The tag or list of tags to filter the tests to run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the async test run request.
 */
export const runAsync = (tags) => 
    axios.post(`${BASE_URL}/async-run`, null, {
        params: { tags },
    });

/**
 * Sends a POST request to asynchronously rerun tests filtered by the provided tags.
 *
 * @function rerunAsync
 * @param {string|string[]} runId - The unique identifier of the test run to rerun.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the rerun request.
 */
export const rerunAsync = (runId) => 
    axios.post(`${BASE_URL}/async-rerun`, null, {
        params: { runId },
    });

/**
 * Sends a POST request to asynchronously rerun failed tests filtered by the provided tags.
 *
 * @function rerunFailedAsync
 * @param {string|string[]} runId - The unique identifier of the test run to rerun.
 * @returns {Promise<import('axios').AxiosResponse>} Axios promise resolving to the server response.
 */
export const rerunFailedAsync = (runId) => 
    axios.post(`${BASE_URL}/async-rerun/failed`, null, {
        params: { runId },
    });

/**
 * Retrieves the status of an asynchronous job by its ID.
 *
 * @function getAsyncJobStatus
 * @param {string|number} jobId - The unique identifier of the job whose status is to be fetched.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves to the Axios response containing the job status.
 */
export const getAsyncJobStatus = (jobId) => 
    axios.get(`${BASE_URL}/status/${jobId}`);

/**
 * Cancels an asynchronous job by its ID.
 *
 * @function cancelAsyncJob
 * @param {string|number} jobId - The unique identifier of the job to cancel.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the delete request.
 */
export const cancelAsyncJob = (jobId) => 
    axios.delete(`${BASE_URL}/cancel/${jobId}`);