package com.framework.apiserver.utilities;

import com.framework.apiserver.config.SftpProperties;
import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * SFTPUtil is a utility class for handling SFTP operations.
 * It provides methods to connect, upload, download, and disconnect from an SFTP server.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>SftpProperties for SFTP configuration</li>
 *   <li>JSch library for SFTP operations</li>
 *   <li>Spring Framework for dependency injection</li>
 * </ul>
 *
 * @see SftpProperties
 * @see JSch
 * @see ChannelSftp
 * @see Component
 */
@Component
public class SFTPUtil {

    private final SftpProperties props;
    private Session session;
    private ChannelSftp channelSftp;

    /**
     * Constructs an SFTPUtil instance with the required SftpProperties dependency.
     *
     * @param props The SftpProperties instance containing SFTP configuration details.
     */
    @Autowired
    public SFTPUtil(SftpProperties props) {
        this.props = props;
    }

    /**
     * Establishes a connection to the SFTP server using the provided configuration.
     *
     * @throws Exception If an error occurs during the connection process.
     */
    public void connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(props.getUsername(), props.getHost(), props.getPort());
        session.setPassword(props.getPassword());

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
     * @param localFilePath  The path to the local file.
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
     * @param localFilePath  The path where the file will be downloaded locally.
     * @throws Exception If an error occurs during file download.
     */
    public void downloadFile(String remoteFilePath, String localFilePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(new File(localFilePath))) {
            channelSftp.get(remoteFilePath, fos);
        }
    }

    /**
     * Disconnects from the SFTP server.
     * Ensures that both the SFTP channel and the session are properly closed.
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