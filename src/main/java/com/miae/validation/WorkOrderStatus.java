package com.miae.validation;

/**
 * Enumeration representing the various statuses that a work order can have within the Manufacturing Impact Copilot application. These statuses help in tracking the progress and state of work orders throughout their lifecycle.
 */
public enum WorkOrderStatus {
    CREATED,
    RELEASED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
