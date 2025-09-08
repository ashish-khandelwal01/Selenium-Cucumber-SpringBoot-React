/**
 * API module for interacting with the Data file controller backend.
 * Provides functions to get the data from the data sheet, select a data sheet,
 * and update the data sheet file.
 *
 * @module excelDataApi
 */

import { createApi } from "./createApi";

const dataApi = createApi("/data");

/**
 * Fetches the list of sheets available in the data file from the API.
 *
 * @returns {Promise<Object>} A promise that resolves to the response data containing a List of data sheet files.
 */
export const listSheets = () =>
    dataApi.get(`/excel/sheets`);

/**
 * Fetches the data from the data sheet file from the API.
 *
 * @function viewSheetData
 * @param {string} sheetName - The data from the data sheet to retrieve.
 * @returns {Promise<Object>} A promise that resolves to the response data from the data sheet.
 */
export const viewSheetData = (sheetName) =>
    dataApi.get(`/excel/${sheetName}`);

/**
 * Edit the data sheet file content via the API.
 *
 * @function editSheetDataFile
 * @param {string} sheetName - The name of the sheet to update.
 * @param {Array<Array<string>>} dataRows - The new content for the data sheet file as a 2D array.
 * @returns {Promise<Object>} A promise that puts data in the backend.
 */
export const editSheetDataFile = (sheetName, dataRows) => {
    console.log("Sending data to API:", dataRows);
    // The backend expects the data directly as the body, not wrapped in an object
    return dataApi.post(`/excel/${sheetName}`, dataRows);
}
