package com.framework.apiserver.utilities;

import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * SFTPUtil is a utility class for handling SFTP operations.
 * It provides methods to connect, upload, download, and disconnect from an SFTP server.
 */
public class SFTPUtil extends BaseClass {

    private Session session;
    private ChannelSftp channelSftp;

    /**
     * Connects to the SFTP server using the provided credentials.
     *
     * @param host The SFTP server host.
     * @param port The SFTP server port.
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @throws Exception If an error occurs during connection.
     */

    public void connect(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
    }

    /**
     * Uploads a file to the SFTP server.
     *
     * @param localFilePath The path to the local file.
     * @param remoteFilePath The path on the SFTP server where the file will be uploaded.
     * @throws Exception If an error occurs during file upload.
     */
    public void uploadFile(String localFilePath, String remoteFilePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(new File(localFilePath))) {
            channelSftp.put(fis, remoteFilePath);
        }
    }

    /**
     * Downloads a file from the SFTP server.
     *
     * @param remoteFilePath The path to the file on the SFTP server.
     * @param localFilePath The path where the file will be downloaded locally.
     * @throws Exception If an error occurs during file download.
     */
    public void downloadFile(String remoteFilePath, String localFilePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(new File(localFilePath))) {
            channelSftp.get(remoteFilePath, fos);
        }
    }

    /**
     * Disconnects from the SFTP server.
     */
    public void disconnect() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}