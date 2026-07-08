package com.miae.graph.node;

import java.time.LocalDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Node entity representing a work order in the manufacturing neo4j graph.
 */
@Node("WORK_ORDER")
public class WorkOrderNode {

    @Id
    private String workOrderId;
    private String status;
    private long remainingQuantity;
    private LocalDate plannedCompletionDate;
    private String priority;
    private String uom;
    private String materialAvailabilityStatus;

    public WorkOrderNode() {
    }

    public WorkOrderNode(String workOrderId,
                         String status,
                         long remainingQuantity,
                         LocalDate plannedCompletionDate,
                         String priority,
                         String uom,
                         String materialAvailabilityStatus) {
        this.workOrderId = workOrderId;
        this.status = status;
        this.remainingQuantity = remainingQuantity;
        this.plannedCompletionDate = plannedCompletionDate;
        this.priority = priority;
        this.uom = uom;
        this.materialAvailabilityStatus = materialAvailabilityStatus;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public String getStatus() {
        return status;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public LocalDate getPlannedCompletionDate() {
        return plannedCompletionDate;
    }

    public String getPriority() {
        return priority;
    }

    public String getUom() {
        return uom;
    }

    public String getMaterialAvailabilityStatus() {
        return materialAvailabilityStatus;
    }
}
