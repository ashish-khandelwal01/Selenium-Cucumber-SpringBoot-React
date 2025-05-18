package com.framework.apiserver.utilities;

import com.framework.apiserver.dto.RunInfo;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    /**
     * Generates a unique run ID based on the current date and time.
     *
     * @return A string representing the run ID.
     */
    public static String generateRunId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "run-" + LocalDateTime.now().format(formatter);
    }

    /**
     * Retrieves the most recently modified report folder from the specified base directory
     * excluding the "target" folder.
     *
     * <p>This method scans the subdirectories of the given base directory and identifies
     * the one with the most recent modification timestamp.</p>
     *
     * @param baseDirPath The path of the base directory to search.
     * @return The absolute path of the most recently modified folder, or null if no subdirectories exist.
     */
    public String getMostRecentReportFolder(String baseDirPath) {
        File baseDir = new File(baseDirPath);
        File[] subDirs = baseDir.listFiles(file -> file.isDirectory() && !file.getName().equals("target"));
        if (subDirs == null || subDirs.length == 0) return null;

        return Arrays.stream(subDirs)
                .max(Comparator.comparingLong(File::lastModified))
                .map(File::getAbsolutePath)
                .orElse(null);
    }

    /**
     * Moves a report folder to a new directory named after the specified run ID.
     *
     * <p>This method moves the contents of the source directory to a destination directory
     * under the "reports" folder, named after the provided run ID.</p>
     *
     * @param sourceDir The path of the source directory to move.
     * @param runId The unique identifier for the destination folder.
     * @throws IOException If an I/O error occurs during the move operation.
     */
    public void moveReportToRunIdFolder(String sourceDir, String runId) throws IOException {
        Path dest = Paths.get("reports", runId);
        FileUtils.moveDirectory(new File(sourceDir), dest.toFile());
    }

    /**
     * Writes run information to a JSON file in the corresponding run ID folder.
     *
     * <p>This method creates a JSON object containing details about the run, such as
     * run ID, tags, start and end times, duration, and status. The JSON file is saved
     * in the "reports/{runId}" directory with the name "run-info.json".</p>
     *
     * @param runInfo The RunInfo object containing details about the run.
     * @throws IOException If an I/O error occurs during the file write operation.
     */
    public void writeRunInfo(RunInfo runInfo) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("runId", runInfo.getRunId());
        obj.put("tags", runInfo.getTags());
        obj.put("Start Time", runInfo.getStartTime());
        obj.put("End Time", runInfo.getEndTime());
        obj.put("Duration in Seconds", runInfo.getDurationSeconds());
        obj.put("Total", runInfo.getTotal());
        obj.put("Passed", runInfo.getPassed());
        obj.put("Failed", runInfo.getFailed());
        obj.put("status", runInfo.getStatus());

        Path path = Paths.get("reports", runInfo.getRunId(), "run-info.json");
        Files.write(path, obj.toString(4).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compresses the report folder for the specified run ID into a zip file.
     *
     * <p>This method creates a zip file containing all the files and subdirectories
     * of the "reports/{runId}" folder. The zip file is saved in the "reports" directory
     * with the name "{runId}.zip".</p>
     *
     * @param runId The unique identifier of the report folder to compress.
     * @throws IOException If an I/O error occurs during the compression process.
     */
    public void zipReportFolder(String runId) throws IOException {
        Path source = Paths.get("reports", runId);
        Path zipPath = Paths.get("reports", runId + ".zip");

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(source).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(source.relativize(path).toString());
                try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                } catch (IOException e) {
                    baseClass.failLog("Zip creation failed. Error: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Moves Cucumber report files to the corresponding run ID folder.
     *
     * <p>This method moves the "cucumber-reports.html" and "cucumber-reports.json" files
     * from the "target" directory to the "reports/{runId}" folder.</p>
     *
     * @param runId The unique identifier of the destination folder.
     * @throws IOException If an I/O error occurs during the file move operation.
     */
    public void moveCucumberReportsToRunIdFolder(String runId) throws IOException {
        Path destCucumberReportHtml = Paths.get("reports", runId, "cucumber-reports.html");
        Path destCucumberJson = Paths.get("reports", runId, "cucumber-reports.json");
        FileUtils.moveFile(new File("target/cucumber-reports.html"), destCucumberReportHtml.toFile());
        FileUtils.moveFile(new File("target/cucumber-reports.json"), destCucumberJson.toFile());
    }

    /**
     * Deletes a file at the specified file path.
     *
     * <p>This method checks if the file exists at the given path and deletes it.
     * If an exception occurs during the process, it logs the error using the BaseClass.</p>
     *
     * @param filePath The path of the file to be deleted.
     */
    public void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            baseClass.failLog("Error in deleting file: " + e.getMessage());
        }
    }
}