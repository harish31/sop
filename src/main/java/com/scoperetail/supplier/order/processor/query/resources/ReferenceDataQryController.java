package com.scoperetail.supplier.order.processor.query.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.commons.manage.order.dto.response.ChangeReasonResponse;
import com.scoperetail.commons.manage.order.dto.response.LineStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderTypeResponse;
import com.scoperetail.supplier.order.processor.query.handler.ReferenceDataQueryHandler;

@RestController
@CrossOrigin
public class ReferenceDataQryController {
  @Autowired private ReferenceDataQueryHandler referenceDataQueryHandler;

  @GetMapping(path = "/v1/order/ordertypes", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<OrderTypeResponse> getAllOrderType() {
    return referenceDataQueryHandler.getAllOrderType();
  }

  @GetMapping(path = "/v1/order/orderstatus", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<OrderStatusResponse> getAllOrderStatus() {
    return referenceDataQueryHandler.getAllOrderStatus();
  }

  @GetMapping(path = "/v1/order/linestatus", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<LineStatusResponse> getAllLineStatus() {
    return referenceDataQueryHandler.getAllLineStatus();
  }

  @GetMapping(path = "/v1/order/changereason", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ChangeReasonResponse> getAllChangeReason() {
    return referenceDataQueryHandler.getAllChangeReason();
  }
}
