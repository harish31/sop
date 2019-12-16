package com.scoperetail.supplier.order.processor.query.resources;

import com.scoperetail.common.rest.client.model.sop.ScheduleInfo;
import com.scoperetail.supplier.order.processor.query.handler.SchedulerInfoQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SupplierScheduleInfoController {

  @Autowired private SchedulerInfoQueryHandler schedulerInfoQueryHandler;

  @GetMapping(
      path = "/v1/scheduleinfo/orders/{orderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ScheduleInfo getSchedulerInfo(
      @PathVariable(name = "orderId", required = true) Integer orderId) {
    return schedulerInfoQueryHandler.getSchedulerInfo(orderId);
  }
}
