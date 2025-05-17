package com.framework.apiserver.utilities;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * CommonUtils provides utility methods for various file operations and formatting.
 *
 * <p>It includes methods for date-time formatting, string-to-integer conversion,
 * directory creation, file zipping, path manipulation, directory deletion,
 * file movement, and reading text files.</p>
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>BaseClass for logging errors</li>
 *   <li>Zip4j for zipping files</li>
 *   <li>Apache Commons IO for path manipulation</li>
 * </ul>
 *
 * @see BaseClass
 * @see ZipFile
 * @see FilenameUtils
 * @see Files
 * @see Paths
 * @see DateFormat
 * @see SimpleDateFormat
 * @see BufferedReader
 * @see File
 * @see IOException
 *
 * @author ashish-khandelwal01
 */
@Component
public class CommonUtils {

    @Autowired
    private BaseClass baseClass;

    /**
     * Returns the current date and time formatted according to the specified format.
     *
     * @param format The date-time format string.
     * @return The formatted date-time string, or null if an error occurs.
     */
    public String getDateTime(String format) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            return df.format(new Date());
        } catch (Exception e) {
            baseClass.failLog("Error in getting date and time: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a string to an integer.
     *
     * @param number The string to convert.
     * @return The integer value, or null if the string is not a valid number.
     */
    public Integer convertStringToInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            baseClass.failLog("Error in converting string to integer: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new directory at the specified path.
     *
     * @param folderPath The relative path of the directory to create.
     * @return The absolute path of the created directory.
     */
    public String createNewDirectory(String folderPath) {
        String dirToCreate = System.getProperty("user.dir") + folderPath;
        try {
            File dir = new File(dirToCreate);
            if (!dir.exists()) {
                Files.createDirectories(Paths.get(dirToCreate));
            }
        } catch (IOException e) {
            baseClass.failLog("Error in creating directory: " + e.getMessage());
        }
        return dirToCreate;
    }

    /**
     * Zips the contents of a folder into a zip file.
     *
     * @param folderPath The path of the folder to zip.
     * @param zipPath The path where the zip file will be created.
     * @param zipFileName The name of the zip file (without extension).
     * @return The name of the created zip file.
     */
    public String zipFile(String folderPath, String zipPath, String zipFileName) {
        try {
            folderPath = getPath(System.getProperty("user.dir") + folderPath);
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                createNewDirectory(zipPath);
            }
            zipPath = getPath(System.getProperty("user.dir") + zipPath);
            zipFileName = zipFileName + ".zip";
            new ZipFile(zipPath + zipFileName).addFolder(new File(folderPath));
        } catch (Exception e) {
            baseClass.failLog("Error in zipping folder: " + e.getMessage());
        }
        return zipFileName;
    }

    /**
     * Converts a file path to the appropriate format based on the operating system.
     *
     * @param originalPath The original file path.
     * @return The formatted file path, or null if an error occurs.
     */
    public String getPath(String originalPath) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return originalPath;
            } else {
                return FilenameUtils.separatorsToUnix(originalPath);
            }
        } catch (Exception e) {
            baseClass.failLog("Error in getting expected path: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param directoryPath The path of the directory to delete.
     * @return True if the directory was successfully deleted, false otherwise.
     */
    public boolean deleteDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.getAbsolutePath());
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    /**
     * Moves a file from the source path to the destination path.
     *
     * @param sourcePath The path of the file to move.
     * @param destinationPath The path where the file will be moved.
     */
    public void moveFile(String sourcePath, String destinationPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destinationPath), REPLACE_EXISTING);
        } catch (IOException e) {
            baseClass.failLog("Error in moving file: " + e.getMessage());
        }
    }

    /**
     * Reads the contents of a text file.
     *
     * @param filePath The path of the file to read.
     * @return The contents of the file as a string.
     */
    public String readTextFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            baseClass.failLog("Error in reading file: " + e.getMessage());
        }
        return content.toString();
    }
}