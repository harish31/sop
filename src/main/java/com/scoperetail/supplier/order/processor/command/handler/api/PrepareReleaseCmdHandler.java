package com.scoperetail.supplier.order.processor.command.handler.api;

import java.util.Optional;

import com.scoperetail.oms.schema.OutboundCustomerOrder;
import com.scoperetail.order.persistence.entity.SupplierOrder;

public interface PrepareReleaseCmdHandler {

  OutboundCustomerOrder createOutboundOrder(SupplierOrder order);

  Optional<SupplierOrder> readSupplierOrder(Integer orderId);

  void updateSupplierOrder(Integer orderId);
}
