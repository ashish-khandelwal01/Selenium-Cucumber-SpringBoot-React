package com.framework.apiserver.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.apiserver.dto.RunInfo;
import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.service.TestRunInfoService;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;
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
     * Executes a test case run using JUnitCore with the specified tag and run ID.
     *
     * <p>This method constructs a command to execute a test run with the following steps:</p>
     * <ul>
     *   <li>Sets the `run.id` and `cucumber.filter.tags` system properties.</li>
     *   <li>Specifies the current classpath for the Java process.</li>
     *   <li>Uses `JUnitCore` to run the `TestRunner` class.</li>
     *   <li>Starts the process and waits for it to complete.</li>
     * </ul>
     *
     * <p>The method inherits the I/O of the current process to display logs in the console.</p>
     *
     * @param tag         The tag to filter test cases to be executed.
     * @param runId       The unique identifier for the test run.
     * @param failedReport  the path of the failed report file.
     * @throws IOException          If an I/O error occurs during process execution.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to complete.
     */
    public static void testCaseRun(String tag, String runId, Path failedReport) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-Drun.id=" + runId);
        if(tag != null && !tag.isEmpty()) {
            command.add("-Dcucumber.filter.tags=" + tag);
        }else{
            command.add("-Dcucumber.feature.path=" + failedReport.toAbsolutePath());
        }
        command.add("-cp");
        command.add(System.getProperty("java.class.path")); // current classpath
        if(tag != null && !tag.isEmpty()) {
            command.add("com.framework.apiserver.testrunner.TestRunner");
        }else{
            command.add("com.framework.apiserver.testrunner.TestFailedRunner");
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
    }

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
     * @param runId       The unique identifier for the run.
     * @return The absolute path of the most recently modified folder, or null if no subdirectories exist.
     */
    public String getReportFolderWithRunId(String baseDirPath, String runId) {
        if (runId == null || runId.isEmpty()) {
            System.err.println("System property 'run.id' is not set.");
            return null;
        }

        try (Stream<Path> pathStream = Files.walk(Paths.get(baseDirPath))) {
            Optional<Path> matchedPath = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .filter(path -> path.getParent().toString().contains("Reports"))
                    .filter(path -> {
                        try {
                            String html = Files.readString(path);
                            Document doc = Jsoup.parse(html);
                            String text = doc.text();
                            return text.contains(runId);
                        } catch (IOException e) {
                            System.err.println("Error processing file: " + path + " - " + e.getMessage());
                            return false;
                        }
                    })
                    .map(path -> path.getParent()) // get the folder where the match was found
                    .findFirst();

            return matchedPath.map(Path::toString).orElse(null);

        } catch (IOException e) {
            System.err.println("Error walking the file tree: " + e.getMessage());
            return null;
        }
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
        File source = new File(sourceDir);
        Path dest = Paths.get("reports", runId, source.getName());
        FileUtils.moveDirectory(source, dest.toFile());
        deleteDirectory(source.getParent());
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

    /**
     * Extracts the paths and line numbers of failed scenarios from the cucumber report.
     *
     * <p>This method reads the `cucumber-report.json` file for the specified run ID
     * and identifies scenarios that failed. It constructs a list of paths with line
     * numbers for rerunning the failed scenarios.</p>
     *
     * @param runId The unique identifier of the test run.
     * @return A list of strings representing the paths and line numbers of failed scenarios.
     */
    public List<String> extractFailedScenarioPathsWithLineNumbers(String report_base_path, String runId) {
        List<String> rerunList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Path cucumberJsonPath = Path.of(report_base_path, runId, "cucumber-reports.json");

        try {
            JsonNode root = mapper.readTree(cucumberJsonPath.toFile());
            for (JsonNode feature : root) {
                String uri = feature.get("uri").asText();
                for (JsonNode element : feature.get("elements")) {
                    if (element.get("type").asText().equals("scenario")) {
                        boolean hasFailure = false;
                        for (JsonNode step : element.get("steps")) {
                            String result = step.get("result").get("status").asText();
                            if ("failed".equals(result)) {
                                hasFailure = true;
                                break;
                            }
                        }
                        if (hasFailure) {
                            int line = element.get("line").asInt();
                            // Construct path relative to your test resources
                            rerunList.add(uri + ":" + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading cucumber report: " + e.getMessage());
        }

        return rerunList;
    }

    /**
     * Creates a run information file and saves it to the database.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Creates and populates a `RunInfo` object and a `TestRunInfoEntity` object with the provided parameters.</li>
     *   <li>Extracts failed scenario paths from the cucumber report and associates them with the `TestRunInfoEntity` object.</li>
     *   <li>Saves the `TestRunInfoEntity` object to the database using the `TestRunInfoService`.</li>
     *   <li>Moves the most recent report folder to a directory named after the run ID.</li>
     *   <li>Moves cucumber report files to the run ID folder.</li>
     *   <li>Writes the run information to a JSON file in the run ID folder.</li>
     *   <li>Compresses the run ID folder into a zip file.</li>
     * </ul>
     *
     * <p>If any exception occurs during the process, it logs an error message.</p>
     *
     * @param testRunInfoService The service used to save the run information to the database.
     * @param tag The tag associated with the test run.
     * @param runId The unique identifier for the test run.
     * @param startTime The start time of the test run.
     * @param endTime The end time of the test run.
     * @param durationSeconds The duration of the test run in seconds.
     * @throws IOException If an I/O error occurs during file operations.
     */
    public HashMap<String, Object> createRunInfoFileAndDb(TestRunInfoService testRunInfoService, String tag, String runId,
                                       LocalDateTime startTime, LocalDateTime endTime, long durationSeconds){
        try {
            HashMap<String, Object> result = new HashMap<>();
            HashMap<String, Integer> results = readCucumberJsonForResults(runId);
            int failureCount = results.get("failed");
            int total = results.get("total");
            int passed = total - failureCount;
            System.out.println("Test execution completed with " + failureCount + " failures.");
            String status = failureCount == 0
                    ? "Execution Successful"
                    : "Execution Completed with Failures: " + failureCount;
            result.put("status", status);
            result.put("failureCount", failureCount);
            result.put("passed", passed);
            result.put("total", total);
            TestRunInfoEntity runInfoDb = new TestRunInfoEntity();
            String reportsDir = "reports";
            RunInfo runInfo = new RunInfo();
            runInfo.setRunId(runId);
            runInfoDb.setRunId(runId);
            runInfo.setTags(tag);
            runInfoDb.setTags(tag);
            runInfo.setStartTime(startTime);
            runInfoDb.setStartTime(startTime);
            runInfo.setEndTime(endTime);
            runInfoDb.setEndTime(endTime);
            runInfo.setDurationSeconds(durationSeconds);
            runInfoDb.setDurationSeconds(Integer.parseInt(String.valueOf(durationSeconds)));
            runInfo.setTotal(total);
            runInfoDb.setTotal(total);
            runInfo.setPassed(passed);
            runInfoDb.setPassed(passed);
            runInfo.setFailed(failureCount);
            runInfoDb.setFailed(failureCount);
            runInfo.setStatus(status);
            runInfoDb.setStatus(status);
            String latestReportFolder = getReportFolderWithRunId(".", runId);
            if (latestReportFolder != null) {
                moveReportToRunIdFolder(latestReportFolder, runId);
                writeRunInfo(runInfo);
                zipReportFolder(runId);
            }
            List<String> failures = extractFailedScenarioPathsWithLineNumbers(reportsDir, runId);
            runInfoDb.setFailureScenarios(failures);
            testRunInfoService.save(runInfoDb);
            System.out.println("✅ run-info.json imported to DB successfully.");
            return result;
        }catch(Exception e){
            System.err.println("❌ Failed to parse or insert run-info.json into DB.");
        }
        return null;
    }

    /**
     * Reads the Cucumber JSON report and extracts the total and failed scenario counts.
     *
     * <p>This method processes the `cucumber-reports.json` file located in the `target` directory
     * to calculate the total number of scenarios and the number of failed scenarios. It iterates
     * through the features, scenarios, and steps in the JSON structure to determine the status
     * of each scenario.</p>
     *
     * @return A {@link HashMap} containing:
     *         <ul>
     *           <li>`total` - The total number of scenarios.</li>
     *           <li>`failed` - The number of failed scenarios.</li>
     *         </ul>
     *         Returns `null` if an error occurs while reading the JSON file.
     */

    public HashMap<String, Integer> readCucumberJsonForResults(String runId){
        HashMap<String, Integer> result = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File("reports/"+runId+"/cucumber-reports.json"));
            // Process the JSON data as needed
            int totalScenarios = 0;
            int failedScenarios = 0;

            for (JsonNode feature : rootNode) {
                JsonNode elements = feature.get("elements");
                if (elements != null) {
                    for (JsonNode scenario : elements) {
                        totalScenarios++;
                        boolean hasFailed = false;

                        for (JsonNode step : scenario.get("steps")) {
                            String status = step.get("result").get("status").asText();
                            if ("failed".equalsIgnoreCase(status)) {
                                hasFailed = true;
                            }
                        }

                        if (hasFailed) {
                            failedScenarios++;
                        }
                    }
                }
            }
            result.put("total", totalScenarios);
            result.put("failed", failedScenarios);
            return result;
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
        return null;
    }
}