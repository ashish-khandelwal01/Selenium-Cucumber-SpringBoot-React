package com.framework.apiserver.utilities;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDBUtil is a utility class for interacting with MongoDB.
 * It provides methods to establish connections, retrieve collections, and query documents.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see ConnectionString
 * @see MongoClient
 * @see MongoCollection
 * @see Document
 * @see JSONObject
 */

public class MongoDBUtil extends BaseClass {

    /**
     * JsonUtil instance for JSON-related operations.
     */
    public static JsonUtil jsonUtil = new JsonUtil();

    /**
     * Properties object to load database configuration from a properties file.
     */
    public Properties dbProperties = new Properties();

    /**
     * FileInputStream to read the database properties file.
     */
    public FileInputStream dbFile;

    /**
     * ConnectionString object to store the MongoDB connection string.
     */
    static ConnectionString connectionString;

    // Static block to load the database properties file.
    {
        try {
            dbFile = new FileInputStream(Constants.sqlFilePath);
            dbProperties.load(dbFile);
        } catch (Exception e) {
            failLog("Unable to load sql.properties file");
        }
    }

    // Static block to initialize the MongoDB connection string.
    {
        try {
            connectionString = new ConnectionString(dbProperties.getProperty("mongoUrl"));
        } catch (Exception e) {
            failLog("Unable to connect to database");
        }
    }

    /**
     * Creates and returns a MongoClient instance using the connection string.
     *
     * @return A MongoClient instance.
     */
    public static MongoClient getMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return (MongoClient) MongoClients.create(settings);
    }

    /**
     * Closes the MongoDB client connection.
     *
     * @param mongoClient The MongoClient instance to close.
     */
    public static void disconnectMongoDB(MongoClient mongoClient) {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Retrieves a MongoCollection from the specified database and collection name.
     *
     * @param mongoClient The MongoClient instance.
     * @param dbName The name of the database.
     * @param collectionName The name of the collection.
     * @return The MongoCollection instance.
     */
    public static MongoCollection<Document> getMongoCollection(MongoClient mongoClient, String dbName, String collectionName) {
        return mongoClient.getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * Retrieves a single document from a MongoCollection based on a key-value pair.
     *
     * @param collection The MongoCollection to query.
     * @param key The key to filter the document.
     * @param value The value to filter the document.
     * @return The document as a JSONObject, or null if not found.
     */
    public static JSONObject getDocumentFromCollection(MongoCollection<Document> collection, String key, String value) {
        Document document = collection.find(eq(key, value)).first();
        if (document != null) {
            String document_string = document.toJson();
            return jsonUtil.stringToJson(document_string);
        }
        return null;
    }

    /**
     * Retrieves a list of documents from a MongoCollection based on a key-value pair (integer value).
     *
     * @param collection The MongoCollection to query.
     * @param key The key to filter the documents.
     * @param value The integer value to filter the documents.
     * @return A list of documents as JSONObjects.
     */
    public static List<JSONObject> getDocumentListFromCollection(MongoCollection<Document> collection, String key, int value) {
        FindIterable<Document> document = collection.find(eq(key, value));
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (Document doc : document) {
            jsonObjects.add(jsonUtil.stringToJson(doc.toJson()));
        }
        return jsonObjects;
    }

    /**
     * Retrieves a list of documents from a MongoCollection based on a key-value pair (string value).
     *
     * @param collection The MongoCollection to query.
     * @param key The key to filter the documents.
     * @param value The string value to filter the documents.
     * @return A list of documents as JSONObjects.
     */
    public static List<JSONObject> getDocumentListFromCollection(MongoCollection<Document> collection, String key, String value) {
        FindIterable<Document> document = collection.find(and(eq(key, value)));
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (Document doc : document) {
            jsonObjects.add(jsonUtil.stringToJson(doc.toJson()));
        }
        return jsonObjects;
    }
}