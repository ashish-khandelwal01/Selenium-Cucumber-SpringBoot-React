package com.framework.apiserver.listener;

import com.framework.apiserver.event.JobStatusChangedEvent;
import com.framework.apiserver.service.impl.JobTrackingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobStatusEventListener {

    private final JobTrackingServiceImpl jobTrackingService;

    @EventListener
    public void handleJobStatusChanged(JobStatusChangedEvent event) {
        // Broadcast the updated job to all SSE clients
        jobTrackingService.broadcastJobUpdate(event.getJobTracking());
    }
}
