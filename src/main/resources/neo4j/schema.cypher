CREATE CONSTRAINT miae_product_product_id_unique IF NOT EXISTS
FOR (n:PRODUCT) REQUIRE n.productId IS UNIQUE;

CREATE CONSTRAINT miae_revision_revision_id_unique IF NOT EXISTS
FOR (n:REVISION) REQUIRE n.revisionId IS UNIQUE;

CREATE CONSTRAINT miae_component_component_id_unique IF NOT EXISTS
FOR (n:COMPONENT) REQUIRE n.componentId IS UNIQUE;

CREATE CONSTRAINT miae_supplier_supplier_id_unique IF NOT EXISTS
FOR (n:SUPPLIER) REQUIRE n.supplierId IS UNIQUE;

CREATE CONSTRAINT miae_inventory_inventory_id_unique IF NOT EXISTS
FOR (n:INVENTORY) REQUIRE n.inventoryId IS UNIQUE;

CREATE CONSTRAINT miae_purchase_order_purchase_order_id_unique IF NOT EXISTS
FOR (n:PURCHASE_ORDER) REQUIRE n.purchaseOrderId IS UNIQUE;

CREATE CONSTRAINT miae_work_order_work_order_id_unique IF NOT EXISTS
FOR (n:WORK_ORDER) REQUIRE n.workOrderId IS UNIQUE;

CREATE CONSTRAINT miae_sales_order_sales_order_id_unique IF NOT EXISTS
FOR (n:SALES_ORDER) REQUIRE n.salesOrderId IS UNIQUE;

CREATE CONSTRAINT miae_customer_customer_id_unique IF NOT EXISTS
FOR (n:CUSTOMER) REQUIRE n.customerId IS UNIQUE;
