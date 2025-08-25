/**
 * API module for interacting with the job tracking backend.
 * Provides functions to get job status summary, active jobs, get job by job id,
 * get job by tag, get job by run id and cancel job.
 *
 * @module jobTrackingApi
 */

import { createApi } from "./createApi";

const jobsApi = createApi("/jobs");
// Use relative URL to leverage Vite proxy
const BASE_URL = '/api/jobs';

/**
 * Get authentication token from localStorage
 */
const getAuthToken = () => {
    try {
        const authData = JSON.parse(localStorage.getItem("authTokens"));
        return authData?.token;
    } catch (error) {
        console.error('Failed to get auth token:', error);
        return null;
    }
};

/**
 * Creates a fetch-based SSE connection for real-time job updates.
 *
 * @function createSSEConnection
 * @param {Function} onMessage - Callback function to handle incoming messages
 * @param {Function} onError - Callback function to handle connection errors
 * @param {Function} onOpen - Callback function to handle connection open
 * @returns {Promise<Object>} The SSE connection object with close method
 */
export const createSSEConnection = (onMessage, onError, onOpen) => {
    const eventSource = new EventSource(`${BASE_URL}/updates`);

    eventSource.onopen = onOpen || (() => {});
    eventSource.onerror = onError || (() => {});

    eventSource.addEventListener('job-status-update', (event) => {
        try {
            const data = JSON.parse(event.data);
            if (onMessage) onMessage(data);
        } catch (error) {
            console.error('Failed to parse SSE message:', error);
        }
    });

    return eventSource;
};

/**
 * Retrieves a summary of job statuses.
 *
 * @function getJobStatusSummary
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job status summary.
 */
export const getJobStatusSummary = () =>
    jobsApi.get(`/status`);

/**
 * Retrieves a list of active jobs.
 *
 * @function getActiveJobs
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing active jobs.
 */
export const getActiveJobs = () =>
    jobsApi.get(`/active`);

/**
 * Retrieves job details by job ID.
 *
 * @function getJobByJobId
 * @param {string|number} jobId - The unique identifier of the job.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job details.
 */
export const getJobByJobId = (jobId) =>
    jobsApi.get(`/${jobId}`);

/**
 * Retrieves jobs by tag.
 *
 * @function getJobByTag
 * @param {string} tag - The tag to filter jobs.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing jobs with the specified tag.
 */
export const getJobByTag = (tag) =>
    jobsApi.get(`/by-tag/${tag}`);

/**
 * Retrieves job details by run ID.
 *
 * @function getJobByRunId
 * @param {string|number} runId - The unique identifier of the run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response containing job details for the run.
 */
export const getJobByRunId = (runId) =>
    jobsApi.get(`/by-run/${runId}`);

/**
 * Cancels a job by its ID.
 *
 * @function cancelJob
 * @param {string|number} jobId - The unique identifier of the job to cancel.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the cancel request.
 */
export const cancelJob = (jobId) =>
    jobsApi.post(`/${jobId}/cancel`, null, {
        params: { jobId: jobId },
    });

/**
 * Utility function to handle SSE connection with automatic reconnection.
 *
 * @function createReconnectingSSE
 * @param {Function} onMessage - Callback for messages
 * @param {Function} onConnectionChange - Callback for connection status changes
 * @param {number} reconnectDelay - Delay before reconnection in milliseconds (default: 3000)
 * @returns {Object} Object with connect, disconnect, and isConnected methods
 */
export const createReconnectingSSE = (onMessage, onConnectionChange, reconnectDelay = 3000) => {
    let eventSource = null;
    let isConnected = false;
    let reconnectTimeout = null;
    let shouldReconnect = true;

    const connect = () => {
        if (eventSource) {
            eventSource.close();
        }

        eventSource = createSSEConnection(
            onMessage,
            () => {
                isConnected = false;
                if (onConnectionChange) onConnectionChange(false);

                // Attempt reconnection
                if (shouldReconnect && !reconnectTimeout) {
                    reconnectTimeout = setTimeout(() => {
                        reconnectTimeout = null;
                        connect();
                    }, reconnectDelay);
                }
            },
            () => {
                isConnected = true;
                if (onConnectionChange) onConnectionChange(true);

                // Clear any pending reconnection
                if (reconnectTimeout) {
                    clearTimeout(reconnectTimeout);
                    reconnectTimeout = null;
                }
            }
        );
    };

    const disconnect = () => {
        shouldReconnect = false;

        if (reconnectTimeout) {
            clearTimeout(reconnectTimeout);
            reconnectTimeout = null;
        }

        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }

        isConnected = false;
        if (onConnectionChange) onConnectionChange(false);
    };

    return {
        connect,
        disconnect,
        isConnected: () => isConnected
    };
};