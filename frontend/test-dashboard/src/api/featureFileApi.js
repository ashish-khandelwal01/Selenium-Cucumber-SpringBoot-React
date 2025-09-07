/**
 * API module for interacting with the feature controller backend.
 * Provides functions to get all the feature file list, get a feature file,
 * update a feature file.
 *
 * @module featureFileApi
 */

import { createApi } from "./createApi";

const featuresApi = createApi("/features");

/**
 * Fetches the list of Feature files from the API.
 *
 * @returns {Promise<Object>} A promise that resolves to the response data containing a List of feature files.
 */
export const listFeatures = () =>
    featuresApi.get(``);

/**
 * Fetches the feature file string from the API.
 *
 * @function viewFeatureFile
 * @param {string} featureFileName - The name of the feature file to retrieve.
 * @returns {Promise<Object>} A promise that resolves to the response data containing feature file.
 */
export const viewFeatureFile = (featureFileName) =>
    featuresApi.get(`/${featureFileName}`);

/**
 * Updates the feature file content via the API.
 *
 * @function updateFeatureFile
 * @param {string} featureFileName - The name of the feature file to update.
 * @param {string} content - The new content for the feature file.
 * @returns {Promise<Object>} A promise that puts data in the backend.
 */
export const updateFeatureFile = (featureFileName, content) =>
    featuresApi.put(`/${featureFileName}`, { content });