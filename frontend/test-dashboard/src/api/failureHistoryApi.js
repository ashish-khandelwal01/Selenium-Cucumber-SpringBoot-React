/**
 * API module for interacting with the failure history endpoints of the test dashboard backend.
 * Provides functions to fetch failed run histories and paginated failure history data.
 *
 * @module failureHistoryApi
 */

import { createApi } from "./createApi";

const historyApi = createApi("/history");


/**
 * Fetches the history of failed runs from the API.
 *
 * @function fetchFailureHistory
 * @returns {Promise<Object>} A promise that resolves to the response data containing failed runs.
 */
export const fetchFailureHistory = () =>
    historyApi.get(`/failed-runs`);


/**
 * Fetches failure history data by pages from the API.
 *
 * @param {Object} params - The parameters for fetching failure history.
 * @param {number} params.page - The page number to fetch.
 * @param {number} params.size - The number of items per page.
 * @returns {Promise<Object>} A promise that resolves to the API response containing failure history data.
 */
export const fetchFailureHistoryByPages = ({ page, size }) => {
  return historyApi.get(`/failed-runs/pages`, {
    params: {
      page,
      size,
    },
  });
};
