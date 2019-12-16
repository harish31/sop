package com.scoperetail.supplier.order.processor.commons.constants;

public class SupplierOrderConstants {

  public static final String SUPPLIER_ORDER_ERROR_MESSAGE_TARGET = "parley.target[0].name";
  public static final String INBOUND_MESSAGE_SOURCE = "parley.source[0].name";
  public static final String RELEASE_INBOUND_MESSAGE_SOURCE = "parley.source[1].name";
  public static final String ORDER_VISIBILITY_SOURCE = "parley.source[3].name";

  public static final String SUPPLIER_ORDER_TARGET = "parley.target[1].name";
  public static final String TERMINATOR_TARGET = "parley.target[2].name";
  public static final String BO_MESSAGE_TARGET = "parley.target[3].name";
  public static final String SUPPLIER_ORDER_BO_MESSAGE_TARGET = "parley.target[3].name";

  public static final String CAMS_EVENT_INBOUND_MESSAGE_SOURCE = "parley.source[2].name";
  public static final String CAMS_EVENT_ERROR_MESSAGE_TARGET = "parley.target[4].name";
  public static final String CAMS_EVENT_BO_MESSAGE_TARGET = "parley.target[5].name";
  public static final String AUDIT_ORDER_TARGET = "parley.target[6].name";
  public static final String ORDER_VISIBILITY_BO = "parley.target[9].name";
  public static final String ORDER_VISIBILITY_ERROR = "parley.target[10].name";
  public static final String ORDER_VISIBILITY_OUTBOUND = "parley.target[11].name";
  public static final String ORDER_PREPRELEASE_ERROR_MESSAGE_TARGET = "parley.target[12].name";
  public static final String ORDER_PREPRELEASE_BO_MESSAGE_TARGET = "parley.target[13].name";

  public static final String ERROR_LOG_TARGET = "parley.target[8].name";

  public static final String CUSTOMER_ORDER_XSD = "customerOrder.xsd";
  public static final String ORDER_OUTBOUND_XSD = "order_outbound.xsd";
  public static final String ORDER_VISIBILITY_XSD = "order_visibility.xsd";
  public static final String CAMS_EVENT_XSD = "internal/cams_event.xsd";

  public static final String UI_SOURCE = "UI";
  public static final String SUPPLIER_SERVICE_ADDRESS_ATTRIBUTE = "address";
  public static final String ORDERS_CAN_NOT_BE_EDITED =
      "These orders can not be updated at this point because all or some of them can not be edited";
  
  public static final String ORIGINAL_PRODUCT_ID = "originalProductId";
  public static final String SUB_PRODUCT_ID = "subsProductId";
  public static final String ORIGINAL_PRODUCT_DESC = "productDesc";
  public static final String ASC_ORDER = "ASC";
  public static final String DESC_ORDER = "DESC";
  public static final String TOTAL_QUANTITY = "ole.currentItemQty";  
  public static final String DEFAULT_SUPPLIER_PRODUCT_ID = "1";  
}
