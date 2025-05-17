package com.framework.apiserver.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonUtil is a utility class for handling JSON operations.
 * It provides methods to read, parse, update, and retrieve data from JSON files or objects.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see FileReader
 * @see JSONParser
 * @see JsonPath
 * @see Gson
 * @see JSONObject
 */
public class JsonUtil extends BaseClass {

    /**
     * Reads a JSON file and retrieves a String value based on the JSON path.
     *
     * @param fileName The path to the JSON file.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The String value from the JSON.
     */
    public String getStringDataFromJson(String fileName, String jsonPathValue) {
        try (Reader reader = new FileReader(fileName)) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getString(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving String data from JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a String value from a JSONObject based on the JSON path.
     *
     * @param jsonObject The JSONObject to read from.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The String value from the JSON.
     */
    public String getStringDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getString(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving String data from JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a JSON file and retrieves an Integer value based on the JSON path.
     *
     * @param fileName The path to the JSON file.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The Integer value from the JSON.
     */
    public int getIntDataFromJson(String fileName, String jsonPathValue) {
        try (Reader reader = new FileReader(fileName)) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getInt(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving Integer data from JSON: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves an Integer value from a JSONObject based on the JSON path.
     *
     * @param jsonObject The JSONObject to read from.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The Integer value from the JSON.
     */
    public int getIntDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getInt(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving Integer data from JSON: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Reads a JSON file and retrieves a List of String values based on the JSON path.
     *
     * @param fileName The path to the JSON file.
     * @param jsonPathValue The JSON path to retrieve the values.
     * @return A List of String values from the JSON.
     */
    public List<String> getListDataFromJson(String fileName, String jsonPathValue) {
        try (Reader reader = new FileReader(fileName)) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getList(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving List data from JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a List of String values from a JSONObject based on the JSON path.
     *
     * @param jsonObject The JSONObject to read from.
     * @param jsonPathValue The JSON path to retrieve the values.
     * @return A List of String values from the JSON.
     */
    public List<String> getListDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getList(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving List data from JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a JSON file and retrieves a Boolean value based on the JSON path.
     *
     * @param fileName The path to the JSON file.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The Boolean value from the JSON.
     */
    public boolean getBooleanDataFromJson(String fileName, String jsonPathValue) {
        try (Reader reader = new FileReader(fileName)) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getBoolean(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving Boolean data from JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a Boolean value from a JSONObject based on the JSON path.
     *
     * @param jsonObject The JSONObject to read from.
     * @param jsonPathValue The JSON path to retrieve the value.
     * @return The Boolean value from the JSON.
     */
    public boolean getBooleanDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getBoolean(jsonPathValue);
        } catch (Exception e) {
            failLog("Error retrieving Boolean data from JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a JSON object with a new value at the specified JSON path.
     *
     * @param jsonObject The JSONObject to update.
     * @param jsonPathValue The JSON path to update.
     * @param value The new value to set.
     * @return The updated JSONObject.
     */
    public JSONObject updatePayload(JSONObject jsonObject, String jsonPathValue, String value) {
        JSONObject json;
        try {
            String payload = jsonObject.toString().replace(jsonPathValue, value);
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(payload);
            return json;
        } catch (Exception e) {
            failLog("Error updating JSON payload: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a JSON file and parses it into a JSONObject.
     *
     * @param fileName The path to the JSON file.
     * @return The parsed JSONObject.
     */
    public JSONObject jsonReader(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (IOException | ParseException e) {
            failLog("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a JSON string into a JSONObject.
     *
     * @param jsonString The JSON string to convert.
     * @return The parsed JSONObject.
     */
    public JSONObject stringToJson(String jsonString) {
        try {
            return (JSONObject) new JSONParser().parse(jsonString);
        } catch (ParseException e) {
            failLog("Error parsing JSON string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Formats a JSONObject into a pretty-printed JSON string.
     *
     * @param jsonObject The JSONObject to format.
     * @return The pretty-printed JSON string.
     */
    public String prettyPrintJson(JSONObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonObject.toString());
        return gson.toJson(jsonElement);
    }

    /**
     * Retrieves a list of String values from a list of JSONObjects based on the JSON path.
     *
     * @param jsonObjectList The list of JSONObjects to read from.
     * @param jsonPathValue The JSON path to retrieve the values.
     * @return A List of String values from the JSONObjects.
     */
    public List<String> getStringValueFromListOfJsonObject(List<JSONObject> jsonObjectList, String jsonPathValue) {
        List<String> jsonValueList = new ArrayList<>();
        for(JSONObject jsonObject : jsonObjectList) {
            try {
                JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
                String jsonValue = jsonPath.getString(jsonPathValue);
                jsonValueList.add(jsonValue);
            } catch (Exception e) {
                failLog("Error retrieving String values from list of JSONObjects: " + e.getMessage());
            }
        }
        return jsonValueList;
    }
}