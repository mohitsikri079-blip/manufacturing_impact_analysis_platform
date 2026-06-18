package com.miae.config;

import com.miae.api.dto.BomRequest;
import com.miae.api.dto.InventoryRequest;
import com.miae.api.dto.ProductRequest;
import com.miae.api.dto.PurchaseOrderRequest;
import com.miae.api.dto.RevisionRequest;
import com.miae.api.dto.SalesOrderRequest;
import com.miae.api.dto.SupplierMappingRequest;
import com.miae.api.dto.WorkOrderRequest;
import com.miae.service.BomProjectionService;
import com.miae.service.InventoryProjectionService;
import com.miae.service.OrderProjectionService;
import com.miae.service.ProductProjectionService;
import com.miae.service.RevisionProjectionService;
import com.miae.service.SupplierProjectionService;
import com.miae.validation.Priority;
import com.miae.validation.WorkOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Component for loading sample manufacturing graph data at application startup.
 */
@Component
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataLoader.class);

    private final boolean enabled;
    private final ProductProjectionService productService;
    private final RevisionProjectionService revisionService;
    private final BomProjectionService bomService;
    private final SupplierProjectionService supplierService;
    private final InventoryProjectionService inventoryService;
    private final OrderProjectionService orderService;

    public SampleDataLoader(
            @Value("${miae.sample-data.enabled}") boolean enabled,
            ProductProjectionService productService,
            RevisionProjectionService revisionService,
            BomProjectionService bomService,
            SupplierProjectionService supplierService,
            InventoryProjectionService inventoryService,
            OrderProjectionService orderService) {
        this.enabled = enabled;
        this.productService = productService;
        this.revisionService = revisionService;
        this.bomService = bomService;
        this.supplierService = supplierService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        productService.upsert(new ProductRequest("P100", "SENSOR-100", "Industrial Sensor"));
        revisionService.upsert(new RevisionRequest("P100-REV-A", "P100", "A", "APPROVED"));
        revisionService.upsert(new RevisionRequest("P100-REV-B", "P100", "B", "APPROVED"));
        bomService.upsert(new BomRequest("P100-REV-A", List.of(
                new BomRequest.BomComponentRequest("PCB-A", 1L),
                new BomRequest.BomComponentRequest("SCREW", 4L))));
        bomService.upsert(new BomRequest("P100-REV-B", List.of(
                new BomRequest.BomComponentRequest("PCB-B", 1L),
                new BomRequest.BomComponentRequest("SCREW", 4L))));
        supplierService.upsert(new SupplierMappingRequest("PCB-A", List.of(
                new SupplierMappingRequest.SupplierRequest("SUP-ABC", "ABC Electronics", 15))));
        supplierService.upsert(new SupplierMappingRequest("PCB-B", List.of(
                new SupplierMappingRequest.SupplierRequest("SUP-ABC", "ABC Electronics", 15))));
        inventoryService.upsert(new InventoryRequest("PCB-A", "WH1", 500L, "INV-PCBA-WH1"));
        inventoryService.upsert(new InventoryRequest("PCB-B", "WH1", 250L, "INV-PCBB-WH1"));
        orderService.upsertPurchaseOrder(new PurchaseOrderRequest("PO-100", "SUP-ABC", "PCB-A", 1000L));
        orderService.upsertWorkOrder(new WorkOrderRequest(
                "WO-1001",
                "P100-REV-B",
                WorkOrderStatus.RELEASED,
                50L,
                Priority.HIGH,
                LocalDate.of(2026, 2, 20)));
        orderService.upsertSalesOrder(new SalesOrderRequest(
                "SO-100",
                "CUST-100",
                "Acme Corp",
                "P100",
                25L,
                BigDecimal.valueOf(50000),
                Priority.CRITICAL));
        LOGGER.info("Sample manufacturing graph data loaded");
    }
}
