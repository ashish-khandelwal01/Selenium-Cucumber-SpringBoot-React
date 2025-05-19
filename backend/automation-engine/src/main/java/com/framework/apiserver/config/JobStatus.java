package com.framework.apiserver.config;

/**
 * Enum representing the status of a job.
 *
 * <p>This enum is used to track the current state of a job in the system.
 * The possible states are:
 * <ul>
 *   <li>PENDING - The job is created but not yet started.</li>
 *   <li>RUNNING - The job is currently in progress.</li>
 *   <li>COMPLETED - The job has finished successfully.</li>
 *   <li>FAILED - The job has encountered an error and did not complete successfully.</li>
 * </ul>
 */
public enum JobStatus {
    PENDING,    // The job is created but not yet started.
    RUNNING,    // The job is currently in progress.
    COMPLETED,  // The job has finished successfully.
    FAILED,      // The job has encountered an error and did not complete successfully.
    CANCELLED   // The job has been canceled by the user or system.
}