package com.framework.apiserver.utilities;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDBUtil provides utility methods for interacting with MongoDB.
 * It includes methods for initializing connections, retrieving collections,
 * and querying documents from MongoDB.
 */
@Component
public class MongoDBUtil {

    private final BaseClass baseClass;
    private final JsonUtil jsonUtil;

    private final String mongoUri;
    private ConnectionString connectionString;

    /**
     * Constructs a MongoDBUtil instance with the required dependencies.
     *
     * @param baseClass The BaseClass instance for logging.
     * @param jsonUtil  The JsonUtil instance for JSON operations.
     * @param mongoUri  The MongoDB connection URI.
     */
    @Autowired
    public MongoDBUtil(BaseClass baseClass, JsonUtil jsonUtil,
                       @Value("${mongo_uri}") String mongoUri) {
        this.baseClass = baseClass;
        this.jsonUtil = jsonUtil;
        this.mongoUri = mongoUri;

        if (mongoUri == null || mongoUri.isEmpty()) {
            baseClass.failLog("Mongo URI property 'spring.datasource.mongo_uri' is missing or empty");
        } else {
            this.connectionString = new ConnectionString(mongoUri);
        }
    }

    /**
     * Creates and returns a MongoClient instance using the configured connection string.
     *
     * @return A MongoClient instance, or null if the connection string is not configured.
     */
    public MongoClient getMongoClient() {
        if (connectionString == null) {
            baseClass.failLog("Mongo connection string not configured, cannot create client");
            return null;
        }
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }

    /**
     * Closes the given MongoClient instance.
     *
     * @param mongoClient The MongoClient instance to close.
     */
    public void disconnectMongoDB(MongoClient mongoClient) {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Retrieves a MongoCollection from the specified database and collection name.
     *
     * @param mongoClient    The MongoClient instance.
     * @param dbName         The name of the database.
     * @param collectionName The name of the collection.
     * @return The MongoCollection instance, or null if the MongoClient is null.
     */
    public MongoCollection<Document> getMongoCollection(MongoClient mongoClient, String dbName, String collectionName) {
        if (mongoClient == null) {
            baseClass.failLog("MongoClient is null when retrieving collection");
            return null;
        }
        return mongoClient.getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * Retrieves the first matching document from the specified collection as a JSONObject.
     *
     * @param collection The MongoCollection to query.
     * @param key        The key to match.
     * @param value      The value to match.
     * @return The matching document as a JSONObject, or null if no document is found.
     */
    public JSONObject getDocumentFromCollection(MongoCollection<Document> collection, String key, String value) {
        if (collection == null) {
            baseClass.failLog("MongoCollection is null in getDocumentFromCollection");
            return null;
        }
        Document doc = collection.find(eq(key, value)).first();
        if (doc != null) {
            return jsonUtil.stringToJson(doc.toJson());
        }
        return null;
    }

    /**
     * Retrieves a list of documents matching the specified integer key-value pair from the collection.
     *
     * @param collection The MongoCollection to query.
     * @param key        The key to match.
     * @param value      The integer value to match.
     * @return A list of matching documents as JSONObjects.
     */
    public List<JSONObject> getDocumentListFromCollection(MongoCollection<Document> collection, String key, int value) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        if (collection == null) {
            baseClass.failLog("MongoCollection is null in getDocumentListFromCollection");
            return jsonObjects;
        }
        FindIterable<Document> documents = collection.find(eq(key, value));
        for (Document doc : documents) {
            jsonObjects.add(jsonUtil.stringToJson(doc.toJson()));
        }
        return jsonObjects;
    }

    /**
     * Retrieves a list of documents matching the specified string key-value pair from the collection.
     *
     * @param collection The MongoCollection to query.
     * @param key        The key to match.
     * @param value      The string value to match.
     * @return A list of matching documents as JSONObjects.
     */
    public List<JSONObject> getDocumentListFromCollection(MongoCollection<Document> collection, String key, String value) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        if (collection == null) {
            baseClass.failLog("MongoCollection is null in getDocumentListFromCollection");
            return jsonObjects;
        }
        FindIterable<Document> documents = collection.find(and(eq(key, value)));
        for (Document doc : documents) {
            jsonObjects.add(jsonUtil.stringToJson(doc.toJson()));
        }
        return jsonObjects;
    }
}