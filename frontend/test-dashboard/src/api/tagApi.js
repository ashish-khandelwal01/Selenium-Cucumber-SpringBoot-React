/**
 * API module for interacting with the tags.
 * Provides functions to save all new tags and then return them.
 *
 * @module tagApi
 */
 
import { createApi } from "./createApi";

const tagsApi = createApi("/");

/**
 * Sends a POST request to get a list of tags.
 *
 * @function getTags
 * @param {string|string[]} tags - The tag or list of tags to filter the tests to run.
 * @returns {Promise<import('axios').AxiosResponse>} A promise that resolves with the response of the test run request.
 */
export const getTags = () =>
    tagsApi.get('tags');