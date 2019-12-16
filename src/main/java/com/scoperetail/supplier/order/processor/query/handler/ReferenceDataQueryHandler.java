package com.scoperetail.supplier.order.processor.query.handler;

import java.util.List;

import com.scoperetail.commons.manage.order.dto.response.ChangeReasonResponse;
import com.scoperetail.commons.manage.order.dto.response.LineStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderTypeResponse;

public interface ReferenceDataQueryHandler {
  List<OrderTypeResponse> getAllOrderType();

  List<LineStatusResponse> getAllLineStatus();

  List<ChangeReasonResponse> getAllChangeReason();

  List<OrderStatusResponse> getAllOrderStatus();
}
