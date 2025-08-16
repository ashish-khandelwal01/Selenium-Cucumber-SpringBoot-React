package com.framework.apiserver.repository;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.config.JobType;
import com.framework.apiserver.entity.JobTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing JobTracking entities.
 * Provides methods for querying and manipulating job tracking data.
 */
@Repository
public interface JobTrackingRepository extends JpaRepository<JobTracking, String> {

    /**
     * Finds all JobTracking entities with statuses in the provided list.
     *
     * @param statuses A list of JobStatus values to filter by.
     * @return A list of JobTracking entities matching the specified statuses.
     */
    List<JobTracking> findByStatusIn(List<JobStatus> statuses);

    /**
     * Finds all active JobTracking entities with statuses in the provided list,
     * ordered by their start time in descending order.
     *
     * @param statuses A list of JobStatus values to filter by.
     * @return A list of JobTracking entities ordered by start time.
     */
    @Query("SELECT j FROM JobTracking j WHERE j.status IN :statuses ORDER BY j.startTime DESC")
    List<JobTracking> findActiveJobsOrderByStartTime(@Param("statuses") List<JobStatus> statuses);

    /**
     * Finds all JobTracking entities with the specified tag and statuses in the provided list.
     *
     * @param tag The tag to filter by.
     * @param statuses A list of JobStatus values to filter by.
     * @return A list of JobTracking entities matching the specified tag and statuses.
     */
    List<JobTracking> findByTagAndStatusIn(String tag, List<JobStatus> statuses);

    /**
     * Counts the number of active JobTracking entities with statuses in the provided list.
     *
     * @param statuses A list of JobStatus values to filter by.
     * @return The count of active JobTracking entities.
     */
    @Query("SELECT COUNT(j) FROM JobTracking j WHERE j.status IN :statuses")
    long countActiveJobs(@Param("statuses") List<JobStatus> statuses);

    /**
     * Counts the number of active JobTracking entities with statuses in the provided list
     * and a specific job type.
     *
     * @param statuses A list of JobStatus values to filter by.
     * @param type The JobType to filter by.
     * @return The count of active JobTracking entities matching the specified type and statuses.
     */
    @Query("SELECT COUNT(j) FROM JobTracking j WHERE j.status IN :statuses AND j.type = :type")
    long countActiveJobsByType(@Param("statuses") List<JobStatus> statuses, @Param("type") JobType type);

    /**
     * Deletes JobTracking entities with statuses in the provided list and an end time before the specified date.
     *
     * @param statuses A list of JobStatus values to filter by.
     * @param before The cutoff date for the end time.
     */
    void deleteByStatusInAndEndTimeBefore(List<JobStatus> statuses, LocalDateTime before);

    /**
     * Finds all JobTracking entities with the specified run ID and statuses in the provided list.
     *
     * @param runId The run ID to filter by.
     * @param statuses A list of JobStatus values to filter by.
     * @return A list of JobTracking entities matching the specified run ID and statuses.
     */
    List<JobTracking> findByRunIdAndStatusIn(String runId, List<JobStatus> statuses);
}