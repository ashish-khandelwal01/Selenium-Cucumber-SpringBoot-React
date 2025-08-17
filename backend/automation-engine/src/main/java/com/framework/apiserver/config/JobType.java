package com.framework.apiserver.config;

/**
 * Enumeration representing the type of a job.
 * Defines whether a job is asynchronous (ASYNC) or synchronous (SYNC).
 */
public enum JobType {
    /**
     * Represents an asynchronous job type.
     */
    ASYNC,

    /**
     * Represents a synchronous job type.
     */
    SYNC
}