package com.framework.apiserver.utilities;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;

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
 * CommonUtils provides utility methods for various file operations,
 * date-time formatting, and other common functionalities.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see BaseClass
 * @see ZipFile
 * @see FilenameUtils
 * @see Files
 * @see Paths
 * @see DateFormat
 * @see SimpleDateFormat
 *
 * @author ashish-khandelwal01
 */
public class CommonUtils extends BaseClass {

    /**
     * Gets the current date and time in the specified format.
     *
     * @param format The desired date-time format.
     * @return The formatted date-time as a String.
     */
    public static String getDateTime(String format) {
        String dateTime = null;
        try {
            DateFormat df = new SimpleDateFormat(format);
            Date date_obj = new Date();
            dateTime = df.format(date_obj);
        } catch (Exception e) {
            failLog("Error in getting date and time: " + e.getMessage());
        }
        return dateTime;
    }

    /**
     * Converts a String to an Integer.
     *
     * @param number The String to convert.
     * @return The Integer value, or null if conversion fails.
     */
    public Integer convertStringToInt(String number) {
        Integer intValue = null;
        try {
            intValue = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            failLog("Error in converting string to integer: " + e.getMessage());
        }
        return intValue;
    }

    /**
     * Creates a new directory at the specified path.
     *
     * @param folder_path The relative path of the directory to create.
     * @return The absolute path of the created directory.
     */
    public String CreateNewDirectory(String folder_path) {
        String dirToCreate = System.getProperty("user.dir") + folder_path;
        try {
            File dir = new File(dirToCreate);
            if (!dir.exists()) {
                Path path = Paths.get(dirToCreate);
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            failLog("Error in creating directory: " + e.getMessage());
        }
        return dirToCreate;
    }

    /**
     * Creates a zip file from the specified folder.
     *
     * @param folder_path The path of the folder to zip.
     * @param zip_path The path where the zip file will be created.
     * @param zip_file_name The name of the zip file to create.
     * @return The name of the created zip file.
     */
    public String zip_file(String folder_path, String zip_path, String zip_file_name) {
        try {
            folder_path = get_path(System.getProperty("user.dir") + folder_path);
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                CreateNewDirectory(zip_path);
            }
            zip_path = get_path(System.getProperty("user.dir") + zip_path);
            zip_file_name = zip_file_name + ".zip";
            new ZipFile(zip_path + zip_file_name).addFolder(new File(folder_path));
        } catch (Exception ignored) {}
        return zip_file_name;
    }

    /**
     * Converts a Windows-style file path to a Unix-style path if necessary.
     *
     * @param windows_path The Windows-style file path.
     * @return The converted file path.
     */
    public String get_path(String windows_path) {
        String exp_path = null;
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                exp_path = windows_path;
            } else {
                exp_path = FilenameUtils.separatorsToUnix(windows_path);
            }
        } catch (Exception e) {
            failLog("Error in getting expected path: " + e.getMessage());
        }
        return exp_path;
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param directoryPath The path of the directory to delete.
     * @return true if the directory was successfully deleted, false otherwise.
     */
    public static boolean deleteDirectory(String directoryPath) {
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
     * Moves a file from one location to another.
     *
     * @param sourcePath The path of the file to move.
     * @param destinationPath The path to move the file to.
     * @throws IOException If an I/O error occurs.
     */
    public static void moveFile(String sourcePath, String destinationPath) throws IOException {
        Files.move(Paths.get(sourcePath), Paths.get(destinationPath), REPLACE_EXISTING);
    }

    /**
     * Reads the contents of a text file using StringBuilder and BufferedReader.
     *
     * @param filePath The path of the text file to read.
     * @return The contents of the file as a String.
     * @throws IOException If an I/O error occurs.
     */
    public static String readTextFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }
}