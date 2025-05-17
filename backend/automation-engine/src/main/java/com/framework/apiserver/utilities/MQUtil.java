package com.framework.apiserver.utilities;

import com.ibm.mq.*;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Utility class for interacting with IBM MQ.
 * <p>
 * This class provides methods to write messages into an MQ queue.
 * <p>
 * It uses configuration properties to establish a connection to the MQ server.
 */
public class MQUtil extends BaseClass {

    // IBM MQ Queue Manager instance
    static MQQueueManager queueManager;

    // Options for putting messages into the queue
    static MQPutMessageOptions putMessageOptions;

    // IBM MQ Queue instance
    static MQQueue queue;

    // IBM MQ Message instance
    static MQMessage mqMessage;

    // Options for opening the queue
    static int openOptions;

    /**
     * Properties object to load MQ configuration from a properties file.
     */
    public static Properties configProperties = new Properties();

    /**
     * FileInputStream to read the MQ properties file.
     */
    public FileInputStream configFile;

    /*
     * Instance initializer block to load the MQ properties file.
     * This block is executed when an instance of the class is created.
     */
    {
        try {
            // Load the properties file using the configured file path
            configFile = new FileInputStream(Constants.configFilePath);
            configProperties.load(configFile);
        } catch (Exception e) {
            // Log failure if the properties file cannot be loaded
            failLog("Unable to load sql.properties file");
        }
    }

    /**
     * Writes a message into the configured MQ queue.
     *
     * @param message The message to be written into the MQ queue.
     */
    public static void writeMsgIntoMQ(String message) {
        // Set MQ environment properties using the configuration
        MQEnvironment.hostname = configProperties.getProperty("mqhost");
        MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES_CLIENT);
        MQEnvironment.channel = configProperties.getProperty("mqchannel");
        MQEnvironment.port = Integer.parseInt(configProperties.getProperty("mqport"));
        MQEnvironment.userID = configProperties.getProperty("mquser");
        MQEnvironment.password = configProperties.getProperty("mqpassword");

        try {
            // Initialize MQ put message options
            putMessageOptions = new MQPutMessageOptions();
            putMessageOptions.options = MQC.MQPMO_DEFAULT_CONTEXT;

            // Connect to the MQ Queue Manager
            queueManager = new MQQueueManager(configProperties.getProperty("mqqueueManager"));

            // Open the queue for output
            openOptions = MQC.MQOO_OUTPUT;
            queue = queueManager.accessQueue(configProperties.getProperty("mqname"), openOptions, null, null, null);

            // Create and configure the MQ message
            mqMessage = new MQMessage();
            mqMessage.persistence = MQC.MQPER_PERSISTENT;
            mqMessage.format = MQC.MQFMT_STRING;
            mqMessage.correlationId = MQC.MQCI_NONE;
            mqMessage.messageId = MQC.MQMI_NONE;
            mqMessage.report = MQC.MQAT_IMS;

            // Write the message content to the MQ message
            mqMessage.writeString(message);

            // Put the message into the queue
            queue.put(mqMessage, putMessageOptions);

            // Clear the message and close the queue
            mqMessage.clearMessage();
            queue.close();

            // Disconnect from the queue manager
            queueManager.disconnect();
        } catch (Exception e) {
            // Log failure if unable to write the message into MQ
            failLog("Unable to write message into MQ");
        }
    }
}