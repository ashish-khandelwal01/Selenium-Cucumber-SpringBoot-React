/**
 * API module for interacting with the job tracking backend.
 * Provides functions to get job status summary, active jobs, get job by job id,
 * get job by tag, get job by run id and cancel job.
 *
 * @module jobTrackingApi
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/jobs'; // or your deployed Spring Boot server

/**
 * Retrieves a summary of job statuses.
 *
 * @function getJobStatusSummary
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job status summary.
 */
export const getJobStatusSummary = () =>
    axios.get(`${BASE_URL}/status`);

/**
 * Retrieves a list of active jobs.
 *
 * @function getActiveJobs
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing active jobs.
 */
export const getActiveJobs = () =>
    axios.get(`${BASE_URL}/active`);

/**
 * Retrieves job details by job ID.
 *
 * @function getJobByJobId
 * @param {string|number} jobId - The unique identifier of the job.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job details.
 */
export const getJobByJobId = (jobId) =>
    axios.get(`${BASE_URL}/${jobId}`);

/**
 * Retrieves jobs by tag.
 *
 * @function getJobByTag
 * @param {string} tag - The tag to filter jobs.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing jobs with the specified tag.
 */
export const getJobByTag = (tag) =>
    axios.get(`${BASE_URL}/by-tag/${tag}`);

/**
 * Retrieves job details by run ID.
 *
 * @function getJobByRunId
 * @param {string|number} runId - The unique identifier of the run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job details for the run.
 */
export const getJobByRunId = (runId) =>
    axios.get(`${BASE_URL}/by-run/${runId}`);

/**
 * Cancels a job by its ID.
 *
 * @function cancelJob
 * @param {string|number} jobId - The unique identifier of the job to cancel.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the cancel request.
 */
export const cancelJob = (jobId) =>
    axios.post(`${BASE_URL}/${jobId}/cancel`, null, {
        params: { jobId: jobId },
    });