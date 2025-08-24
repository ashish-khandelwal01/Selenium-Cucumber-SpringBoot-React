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
export const createSSEConnection = async (onMessage, onError, onOpen) => {
    const token = getAuthToken();

    if (!token) {
        const error = new Error('No authentication token found');
        if (onError) onError(error);
        return null;
    }

    let controller = new AbortController();
    let isConnected = false;

    const connect = async () => {
        try {
            const response = await fetch(`${BASE_URL}/updates`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Accept': 'text/event-stream',
                    'Cache-Control': 'no-cache',
                },
                signal: controller.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            isConnected = true;
            if (onOpen) onOpen();

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';

            while (isConnected && !controller.signal.aborted) {
                const { done, value } = await reader.read();

                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');

                // Keep the last incomplete line in buffer
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('event: job-status-update')) {
                        // Next line should contain the data
                        continue;
                    } else if (line.startsWith('data: ')) {
                        try {
                            const data = line.slice(6); // Remove 'data: ' prefix
                            if (data.trim()) {
                                const parsedData = JSON.parse(data);
                                if (onMessage) onMessage(parsedData);
                            }
                        } catch (parseError) {
                            console.error('Failed to parse SSE data:', parseError);
                        }
                    }
                }
            }
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('Fetch SSE error:', error);
                isConnected = false;
                if (onError) onError(error);
            }
        }
    };

    // Start the connection
    connect();

    return {
        close: () => {
            isConnected = false;
            controller.abort();
        },
        readyState: isConnected ? 1 : 0, // 1 = OPEN, 0 = CONNECTING/CLOSED
        // EventSource-like properties for compatibility
        onopen: null,
        onerror: null,
        addEventListener: () => {} // Stub for compatibility
    };
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

    const connect = async () => {
        if (eventSource) {
            eventSource.close();
        }

        try {
            eventSource = await createSSEConnection(
                onMessage,
                (error) => {
                    console.log('SSE connection lost:', error);
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
                    console.log('SSE connection established');
                    isConnected = true;
                    if (onConnectionChange) onConnectionChange(true);

                    // Clear any pending reconnection
                    if (reconnectTimeout) {
                        clearTimeout(reconnectTimeout);
                        reconnectTimeout = null;
                    }
                }
            );
        } catch (error) {
            console.error('Failed to create SSE connection:', error);
            isConnected = false;
            if (onConnectionChange) onConnectionChange(false);

            // Attempt reconnection on connection creation failure
            if (shouldReconnect && !reconnectTimeout) {
                reconnectTimeout = setTimeout(() => {
                    reconnectTimeout = null;
                    connect();
                }, reconnectDelay);
            }
        }
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