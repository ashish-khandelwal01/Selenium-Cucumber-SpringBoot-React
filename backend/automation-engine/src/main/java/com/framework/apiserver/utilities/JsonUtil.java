package com.framework.apiserver.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.framework.apiserver.utilities.BaseClass;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonUtil provides utility methods for working with JSON data.
 * It includes methods for reading, parsing, updating, and extracting data from JSON files or objects.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>RestAssured JsonPath for JSON path operations</li>
 *   <li>Google Gson for JSON formatting</li>
 *   <li>Spring ResourceLoader for resolving file paths</li>
 *   <li>BaseClass for logging errors</li>
 * </ul>
 *
 * @see JsonPath
 * @see Gson
 * @see JSONObject
 * @see JSONParser
 * @see ResourceLoader
 * @see Component
 * @see BaseClass
 *
 * <p>Author: ashish-khandelwal01</p>
 */
@Slf4j
@Component
public class JsonUtil {

    private final ResourceLoader resourceLoader;
    private final BaseClass baseClass;

    /**
     * Constructs a JsonUtil instance with the required dependencies.
     *
     * @param resourceLoader The Spring ResourceLoader for resolving file paths.
     * @param baseClass      The BaseClass for logging errors.
     */
    @Autowired
    public JsonUtil(ResourceLoader resourceLoader, BaseClass baseClass) {
        this.resourceLoader = resourceLoader;
        this.baseClass = baseClass;
    }

    /**
     * Retrieves a string value from a JSON file using a JSON path.
     *
     * @param filePath      The path to the JSON file.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The string value, or null if an error occurs.
     */
    public String getStringDataFromJson(String filePath, String jsonPathValue) {
        try (Reader reader = new FileReader(resolvePath(filePath))) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getString(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving String data from JSON");
            return null;
        }
    }

    /**
     * Retrieves a string value from a JSONObject using a JSON path.
     *
     * @param jsonObject    The JSONObject to extract the value from.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The string value, or null if an error occurs.
     */
    public String getStringDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getString(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving String data from JSONObject");
            return null;
        }
    }

    /**
     * Retrieves an integer value from a JSON file using a JSON path.
     *
     * @param filePath      The path to the JSON file.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The integer value, or 0 if an error occurs.
     */
    public int getIntDataFromJson(String filePath, String jsonPathValue) {
        try (Reader reader = new FileReader(resolvePath(filePath))) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getInt(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving Integer data from JSON");
            return 0;
        }
    }

    /**
     * Retrieves an integer value from a JSONObject using a JSON path.
     *
     * @param jsonObject    The JSONObject to extract the value from.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The integer value, or 0 if an error occurs.
     */
    public int getIntDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getInt(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving Integer data from JSONObject");
            return 0;
        }
    }

    /**
     * Retrieves a boolean value from a JSON file using a JSON path.
     *
     * @param filePath      The path to the JSON file.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The boolean value, or false if an error occurs.
     */
    public boolean getBooleanDataFromJson(String filePath, String jsonPathValue) {
        try (Reader reader = new FileReader(resolvePath(filePath))) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getBoolean(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving Boolean data from JSON");
            return false;
        }
    }

    /**
     * Retrieves a boolean value from a JSONObject using a JSON path.
     *
     * @param jsonObject    The JSONObject to extract the value from.
     * @param jsonPathValue The JSON path to extract the value.
     * @return The boolean value, or false if an error occurs.
     */
    public boolean getBooleanDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getBoolean(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving Boolean data from JSONObject");
            return false;
        }
    }

    /**
     * Retrieves a list of string values from a JSON file using a JSON path.
     *
     * @param filePath      The path to the JSON file.
     * @param jsonPathValue The JSON path to extract the values.
     * @return A list of string values, or null if an error occurs.
     */
    public List<String> getListDataFromJson(String filePath, String jsonPathValue) {
        try (Reader reader = new FileReader(resolvePath(filePath))) {
            JsonPath jsonPath = new JsonPath(reader);
            return jsonPath.getList(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving List data from JSON");
            return null;
        }
    }

    /**
     * Retrieves a list of string values from a JSONObject using a JSON path.
     *
     * @param jsonObject    The JSONObject to extract the values from.
     * @param jsonPathValue The JSON path to extract the values.
     * @return A list of string values, or null if an error occurs.
     */
    public List<String> getListDataFromJson(JSONObject jsonObject, String jsonPathValue) {
        try {
            JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
            return jsonPath.getList(jsonPathValue);
        } catch (Exception e) {
            baseClass.failLog("Error retrieving List data from JSONObject");
            return null;
        }
    }

    /**
     * Updates a JSON payload by replacing a value at a specified JSON path.
     *
     * @param jsonObject    The JSONObject to update.
     * @param jsonPathValue The JSON path to locate the value to replace.
     * @param value         The new value to set.
     * @return The updated JSONObject, or null if an error occurs.
     */
    public JSONObject updatePayload(JSONObject jsonObject, String jsonPathValue, String value) {
        try {
            String updatedPayload = jsonObject.toString().replace(jsonPathValue, value);
            return (JSONObject) new JSONParser().parse(updatedPayload);
        } catch (Exception e) {
            baseClass.failLog("Error updating JSON payload");
            return null;
        }
    }

    /**
     * Reads a JSON file and parses it into a JSONObject.
     *
     * @param filePath The path to the JSON file.
     * @return The parsed JSONObject, or null if an error occurs.
     */
    public JSONObject jsonReader(String filePath) {
        try (FileReader reader = new FileReader(resolvePath(filePath))) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (IOException | ParseException e) {
            baseClass.failLog("Error reading JSON file");
            return null;
        }
    }

    /**
     * Parses a JSON string into a JSONObject.
     *
     * @param jsonString The JSON string to parse.
     * @return The parsed JSONObject, or null if an error occurs.
     */
    public JSONObject stringToJson(String jsonString) {
        try {
            return (JSONObject) new JSONParser().parse(jsonString);
        } catch (ParseException e) {
            baseClass.failLog("Error parsing JSON string");
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
     * Retrieves a list of string values from a list of JSONObjects using a JSON path.
     *
     * @param jsonObjectList The list of JSONObjects to extract values from.
     * @param jsonPathValue  The JSON path to extract the values.
     * @return A list of string values.
     */
    public List<String> getStringValueFromListOfJsonObject(List<JSONObject> jsonObjectList, String jsonPathValue) {
        List<String> jsonValueList = new ArrayList<>();
        for (JSONObject jsonObject : jsonObjectList) {
            try {
                JsonPath jsonPath = new JsonPath(jsonObject.toJSONString());
                jsonValueList.add(jsonPath.getString(jsonPathValue));
            } catch (Exception e) {
                baseClass.failLog("Error retrieving value from list of JSONObjects");
            }
        }
        return jsonValueList;
    }

    /**
     * Resolves the absolute path of a file located in the classpath.
     *
     * @param filePath The relative path to the file.
     * @return The absolute path to the file.
     * @throws IOException If the file cannot be resolved.
     */
    private String resolvePath(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        return resource.getFile().getAbsolutePath();
    }
}