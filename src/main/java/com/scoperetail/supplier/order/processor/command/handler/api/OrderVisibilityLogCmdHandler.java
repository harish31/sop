package com.scoperetail.supplier.order.processor.command.handler.api;

import com.scoperetail.oms.schema.OrderLog;

public interface OrderVisibilityLogCmdHandler {

  OrderLog createOutboundOrderVisibilityLog(Integer orderId);
}
