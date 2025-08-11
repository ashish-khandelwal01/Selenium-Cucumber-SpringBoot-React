/**
 * API module for interacting with the failure history endpoints of the test dashboard backend.
 * Provides functions to fetch failed run histories and paginated failure history data.
 *
 * @module failureHistoryApi
 */

import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/history';


/**
 * Fetches the history of failed runs from the API.
 *
 * @function fetchFailureHistory
 * @returns {Promise<Object>} A promise that resolves to the response data containing failed runs.
 */
export const fetchFailureHistory = () =>
    axios.get(`${BASE_URL}/failed-runs`);


/**
 * Fetches failure history data by pages from the API.
 *
 * @param {Object} params - The parameters for fetching failure history.
 * @param {number} params.page - The page number to fetch.
 * @param {number} params.size - The number of items per page.
 * @returns {Promise<Object>} A promise that resolves to the API response containing failure history data.
 */
export const fetchFailureHistoryByPages = ({ page, size }) => {
  return axios.get(`${BASE_URL}/failed-runs/pages`, {
    params: {
      page,
      size,
    },
  });
};
