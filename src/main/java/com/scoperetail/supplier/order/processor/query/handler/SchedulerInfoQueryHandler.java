package com.scoperetail.supplier.order.processor.query.handler;

import com.scoperetail.common.rest.client.model.sop.ScheduleInfo;

public interface SchedulerInfoQueryHandler {

  ScheduleInfo getSchedulerInfo(Integer orderId);
}
