package com.framework.apiserver.utilities;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Map;
import java.util.Set;

/**
 * DataProvider is a Spring-managed component that reads JSON files
 * and converts them into data-driven testing format.
 *
 * <p>It provides methods to extract data from JSON arrays and parse JSON files
 * into objects for use in testing scenarios.</p>
 *
 * @see JsonObject
 * @see JsonArray
 * @see JsonElement
 * @see JsonParser
 * @see Logger
 * @see FileReader
 *
 * @author ashish-khandelwal01
 */
@Component
public class DataProvider {

    private static final Logger logger = LoggerFactory.getLogger(DataProvider.class);

    /**
     * The default file path for the JSON data file, injected from application properties.
     */
    @Value("${dataprovider.file-path}")
    private String defaultDataFilePath;

    /**
     * Reads a JSON file and retrieves data from a specified JSON array.
     *
     * @param jsonArrayName The name of the JSON array to extract.
     * @return A 2D Object array representing test data.
     */
    public Object[][] getDataProvider(String jsonArrayName) {
        return getDataProvider(defaultDataFilePath, jsonArrayName);
    }

    /**
     * Reads a JSON file and retrieves data from a specified JSON array.
     *
     * @param dataFilePath   Path to the JSON file.
     * @param jsonArrayName  JSON array name inside the file.
     * @return A 2D Object array representing the data.
     */
    public Object[][] getDataProvider(String dataFilePath, String jsonArrayName) {
        JsonObject jsonObject = getJsonObject(dataFilePath);
        if (jsonObject == null || !jsonObject.has(jsonArrayName)) {
            logger.error("Missing or invalid JSON array: {}", jsonArrayName);
            return new Object[0][0];
        }

        JsonArray testData = jsonObject.getAsJsonArray(jsonArrayName);
        if (testData.isEmpty()) return new Object[0][0];

        int rowCount = testData.size();
        int colCount = ((JsonObject) testData.get(0)).size();
        Object[][] data = new Object[rowCount][colCount];

        for (int i = 0; i < rowCount; i++) {
            Set<Map.Entry<String, JsonElement>> entrySet = testData.get(i).getAsJsonObject().entrySet();
            int j = 0;
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                JsonElement element = entry.getValue();
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isBoolean()) {
                        data[i][j] = primitive.getAsBoolean();
                    } else if (primitive.isNumber()) {
                        data[i][j] = primitive.getAsInt(); // Or getAsDouble
                    } else {
                        data[i][j] = primitive.getAsString();
                    }
                } else {
                    data[i][j] = element.toString(); // For nested structures
                }
                j++;
            }
        }

        return data;
    }

    /**
     * Parses a JSON file into a JsonObject.
     *
     * @param dataFilePath Path to the JSON file.
     * @return JsonObject or null on failure.
     */
    private JsonObject getJsonObject(String dataFilePath) {
        try (FileReader reader = new FileReader(dataFilePath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            logger.error("Error reading/parsing JSON from '{}': {}", dataFilePath, e.getMessage());
            return null;
        }
    }
}