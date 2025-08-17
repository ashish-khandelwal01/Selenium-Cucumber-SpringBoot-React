package com.framework.apiserver.utilities;

import com.ibm.mq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Utility class for interacting with IBM MQ.
 * Provides methods to configure and send messages to an MQ queue.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Spring Framework for dependency injection and configuration</li>
 *   <li>IBM MQ classes for Java for MQ operations</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>mqhost: Hostname of the MQ server</li>
 *   <li>mqchannel: Channel name for MQ communication</li>
 *   <li>mqport: Port number of the MQ server</li>
 *   <li>mquser: Username for MQ authentication</li>
 *   <li>mqpassword: Password for MQ authentication</li>
 *   <li>mqqueueManager: Name of the MQ Queue Manager</li>
 *   <li>mqname: Name of the MQ queue</li>
 * </ul>
 *
 * @see MQEnvironment
 * @see MQQueueManager
 * @see MQQueue
 * @see MQMessage
 * @see MQPutMessageOptions
 */
@SuppressWarnings("deprecation")
@Component
public class MQUtil {

    @Autowired
    private BaseClass baseClass;

    @Value("${mqhost}")
    private String mqHost;

    @Value("${mqchannel}")
    private String mqChannel;

    @Value("${mqport}")
    private int mqPort;

    @Value("${mquser}")
    private String mqUser;

    @Value("${mqpassword}")
    private String mqPassword;

    @Value("${mqqueueManager}")
    private String mqQueueManager;

    @Value("${mqname}")
    private String mqQueueName;

    private MQPutMessageOptions putMessageOptions;
    private int openOptions;

    /**
     * Initializes MQ options after the bean is constructed.
     * Configures the message options and open options for MQ operations.
     */
    @PostConstruct
    public void init() {
        putMessageOptions = new MQPutMessageOptions();
        putMessageOptions.options = MQC.MQPMO_DEFAULT_CONTEXT;
        openOptions = MQC.MQOO_OUTPUT;
    }

    /**
     * Sends a message to the configured MQ queue.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Set MQ environment properties</li>
     *   <li>Connect to the MQ Queue Manager</li>
     *   <li>Access the specified queue</li>
     *   <li>Create and configure the MQ message</li>
     *   <li>Write the message to the queue</li>
     *   <li>Close the queue and disconnect the Queue Manager</li>
     * </ol>
     *
     * @param message The message to be sent to the MQ queue.
     */
    public void writeMsgIntoMQ(String message) {
        // Set MQ environment properties
        MQEnvironment.hostname = mqHost;
        MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES_CLIENT);
        MQEnvironment.channel = mqChannel;
        MQEnvironment.port = mqPort;
        MQEnvironment.userID = mqUser;
        MQEnvironment.password = mqPassword;

        MQQueueManager queueManager = null;
        MQQueue queue = null;
        MQMessage mqMessage = null;

        try {
            // Connect to MQ Queue Manager
            queueManager = new MQQueueManager(mqQueueManager);

            // Access queue
            queue = queueManager.accessQueue(mqQueueName, openOptions, null, null, null);

            // Create message
            mqMessage = new MQMessage();
            mqMessage.persistence = MQC.MQPER_PERSISTENT;
            mqMessage.format = MQC.MQFMT_STRING;
            mqMessage.correlationId = MQC.MQCI_NONE;
            mqMessage.messageId = MQC.MQMI_NONE;
            mqMessage.report = MQC.MQAT_IMS;

            // Write message string
            mqMessage.writeString(message);

            // Put message to queue
            queue.put(mqMessage, putMessageOptions);

            // Clear message and close queue
            mqMessage.clearMessage();
            queue.close();

            // Disconnect queue manager
            queueManager.disconnect();
        } catch (Exception e) {
            baseClass.failLog("Unable to write message into MQ: " + e.getMessage());
        } finally {
            try {
                if (queue != null) queue.close();
            } catch (Exception ignored) {}
            try {
                if (queueManager != null) queueManager.disconnect();
            } catch (Exception ignored) {}
        }
    }
}