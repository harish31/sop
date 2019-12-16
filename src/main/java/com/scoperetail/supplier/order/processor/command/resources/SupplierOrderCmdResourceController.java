package com.scoperetail.supplier.order.processor.command.resources;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.commons.manage.order.dto.request.OrderUpdateRequest;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.commons.util.JwtUtils;
import com.scoperetail.supplier.order.processor.command.handler.api.SupplierOrderCmdHandler;

@RestController
@CrossOrigin
@Validated
public class SupplierOrderCmdResourceController {
  @Autowired private SupplierOrderCmdHandler supplierOrderCmdHandler;

  @PatchMapping(
      path = "/v1/manage/orders",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse updateSupplierOrders(
      @Valid @RequestBody List<OrderUpdateRequest> orderUpdateRequests,
      @RequestHeader(value = "x-auth-token", required = false) String token) {
    final List<OlcmEvent> events = new ArrayList<>();
    final BaseResponse response =
        supplierOrderCmdHandler.updateSupplierOrders(
            null, orderUpdateRequests, events, JwtUtils.getUserId(token));
    sendEventToOlcm(response, events);
    return response;
  }

  @PatchMapping(
      path = "/v1/manage/order",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse updateSupplierOrder(
      @Valid @RequestBody OrderUpdateRequest orderUpdateRequest,
      @RequestHeader(value = "x-auth-token", required = false) String token) {
    orderUpdateRequest.setLoggedInUser(JwtUtils.getUserId(token));
    final List<OlcmEvent> events = new ArrayList<>();
    final BaseResponse response =
        supplierOrderCmdHandler.updateSupplierOrder(null, orderUpdateRequest, events);
    sendEventToOlcm(response, events);
    return response;
  }

  private void sendEventToOlcm(final BaseResponse response, final List<OlcmEvent> events) {
    if (BaseResponse.Status.SUCCESS.equals(response.getStatus()) && !events.isEmpty()) {
      supplierOrderCmdHandler.sendEventToOlcm(events);
    }
  }
}
