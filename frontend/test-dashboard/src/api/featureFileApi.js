import { createApi } from "./createApi";

const featuresApi = createApi("/features");

export const listFeatures = () =>
    featuresApi.get(``);

export const viewFeatureFile = (featureFileName) =>
    featuresApi.get(`/${featureFileName}`);

export const updateFeatureFile = (featureFileName, content) =>
    featuresApi.put(`/${featureFileName}`, { content });