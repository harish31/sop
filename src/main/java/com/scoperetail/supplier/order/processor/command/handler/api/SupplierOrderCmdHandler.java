package com.scoperetail.supplier.order.processor.command.handler.api;

import java.util.List;

import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.commons.manage.order.dto.request.OrderUpdateRequest;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.internal.schema.AuditOrders;
import com.scoperetail.supplier.order.processor.command.model.CustomerOrder;

public interface SupplierOrderCmdHandler {

  void createSupplierOrder(
      AuditOrders auditOrders, CustomerOrder customerOrder, List<OlcmEvent> events)
      throws Exception;

  BaseResponse updateSupplierOrders(
      AuditOrders auditOrders,
      List<OrderUpdateRequest> orderUpdateRequests,
      List<OlcmEvent> events,
      String userId);

  BaseResponse updateSupplierOrder(
      AuditOrders auditOrders, OrderUpdateRequest orderUpdateRequest, List<OlcmEvent> events);

  void sendEventToOlcm(List<OlcmEvent> events);

  void setIsLockedForEdit(List<Integer> orderIds, Character isLocked);
}
