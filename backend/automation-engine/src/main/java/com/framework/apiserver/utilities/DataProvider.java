package com.framework.apiserver.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

/**
 * DataProvider is a utility class that provides methods to read JSON data
 * and convert it into a format suitable for data-driven testing.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see BaseClass
 * @see JsonObject
 * @see JsonArray
 * @see JsonElement
 * @see JsonParser
 * @see FileReader
 * @see Map
 * @see Set
 *
 * @author ashish-khandelwal01
 */
public class DataProvider extends BaseClass {

    /**
     * Reads a JSON file and retrieves data from a specified JSON array.
     * Converts the JSON array into a two-dimensional Object array for use in data-driven testing.
     *
     * @param data_file_path The path to the JSON file containing the data.
     * @param json_array_name The name of the JSON array to retrieve data from.
     * @return A two-dimensional Object array containing the data from the JSON array.
     */
    public Object[][] getDataProvider(String data_file_path, String json_array_name) {
        JsonObject jsonObject = getJsonObject(data_file_path);
        JsonArray testData = jsonObject.getAsJsonArray(json_array_name);
        Object[][] data = new Object[testData.size()][((JsonObject) testData.get(0)).size()];
        for (int i = 0; i < testData.size(); i++) {
            Set<Map.Entry<String, JsonElement>> entrySet = testData.get(i).getAsJsonObject().entrySet();
            int j = 0;
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getValue().getAsString().matches("^([+-]?[0-9]\\d|0)$")) {
                    data[i][j] = entry.getValue().getAsInt();
                } else if (entry.getValue().getAsString().matches("^(?i)(true|false)$")) {
                    data[i][j] = entry.getValue().getAsBoolean();
                } else {
                    data[i][j] = entry.getValue().getAsString();
                }
                j++;
            }
        }
        return data;
    }

    /**
     * Reads a JSON file and parses it into a JsonObject.
     *
     * @param data_file_path The path to the JSON file to read.
     * @return A JsonObject representing the contents of the JSON file.
     *         Returns null if the file is not found or cannot be parsed.
     */
    public JsonObject getJsonObject(String data_file_path) {
        JsonObject jsonObject = null;
        try {
            jsonObject = (JsonObject) new JsonParser().parse(new FileReader(data_file_path));
        } catch (FileNotFoundException e) {
            failLog("File not found: " + data_file_path);
        }
        return jsonObject;
    }
}