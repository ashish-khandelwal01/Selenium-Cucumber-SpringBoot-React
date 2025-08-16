package com.framework.apiserver.event;

import com.framework.apiserver.entity.JobTracking;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event representing a change in the status of a job.
 */
@Data
@AllArgsConstructor
public class JobStatusChangedEvent {

    /**
     * The JobTracking entity associated with the job whose status has changed.
     */
    private JobTracking jobTracking;

    /**
     * The action performed on the job, indicating the status change.
     * Possible values: "CREATED", "UPDATED", "COMPLETED", "CANCELLED".
     */
    private String action;
}