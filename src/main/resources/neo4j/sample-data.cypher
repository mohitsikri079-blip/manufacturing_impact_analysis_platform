MERGE (p:PRODUCT {productId: 'P100'})
SET p.code = 'SENSOR-100',
    p.name = 'Industrial Sensor';

MERGE (revA:REVISION {revisionId: 'P100-REV-A'})
SET revA.code = 'A',
    revA.status = 'APPROVED',
    revA.productId = 'P100';

MERGE (revB:REVISION {revisionId: 'P100-REV-B'})
SET revB.code = 'B',
    revB.status = 'APPROVED',
    revB.productId = 'P100';

MERGE (pcbA:COMPONENT {componentId: 'PCB-A'});
MERGE (pcbB:COMPONENT {componentId: 'PCB-B'});
MERGE (screw:COMPONENT {componentId: 'SCREW'});

MERGE (supplier:SUPPLIER {supplierId: 'SUP-ABC'})
SET supplier.supplierName = 'ABC Electronics';

MERGE (invA:INVENTORY {inventoryId: 'INV-PCBA-WH1'})
SET invA.warehouse = 'WH1',
    invA.quantity = 500;

MERGE (invB:INVENTORY {inventoryId: 'INV-PCBB-WH1'})
SET invB.warehouse = 'WH1',
    invB.quantity = 250;

MERGE (po:PURCHASE_ORDER {purchaseOrderId: 'PO-100'})
SET po.openQuantity = 1000,
    po.supplierId = 'SUP-ABC';

MERGE (wo:WORK_ORDER {workOrderId: 'WO-1001'})
SET wo.status = 'RELEASED',
    wo.remainingQuantity = 50,
    wo.priority = 'HIGH',
    wo.plannedCompletionDate = date('2026-02-20');

MERGE (so:SALES_ORDER {salesOrderId: 'SO-100'})
SET so.openQuantity = 25,
    so.orderValue = 50000.0,
    so.priority = 'CRITICAL',
    so.productId = 'P100';

MERGE (customer:CUSTOMER {customerId: 'CUST-100'})
SET customer.customerName = 'Acme Corp';

MERGE (p)-[:HAS_REVISION]->(revA);
MERGE (p)-[:HAS_REVISION]->(revB);

MERGE (revA)-[revAUsesPcbA:USES_COMPONENT]->(pcbA)
SET revAUsesPcbA.quantity = 1;

MERGE (revA)-[revAUsesScrew:USES_COMPONENT]->(screw)
SET revAUsesScrew.quantity = 4;

MERGE (revB)-[revBUsesPcbB:USES_COMPONENT]->(pcbB)
SET revBUsesPcbB.quantity = 1;

MERGE (revB)-[revBUsesScrew:USES_COMPONENT]->(screw)
SET revBUsesScrew.quantity = 4;

MERGE (pcbA)-[pcbASuppliedBy:SUPPLIED_BY]->(supplier)
SET pcbASuppliedBy.leadTimeDays = 15;

MERGE (pcbB)-[pcbBSuppliedBy:SUPPLIED_BY]->(supplier)
SET pcbBSuppliedBy.leadTimeDays = 15;

MERGE (invA)-[:STOCKS]->(pcbA);
MERGE (invB)-[:STOCKS]->(pcbB);
MERGE (po)-[:PURCHASES]->(pcbA);
MERGE (wo)-[:BUILDS]->(revB);
MERGE (so)-[:ORDERS]->(p);
MERGE (customer)-[:PLACED]->(so);
