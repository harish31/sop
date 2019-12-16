package com.scoperetail.supplier.order.processor.query.resources;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.commons.manage.order.dto.response.SearchOrderResponse;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.handler.SupplierOrderQueryHandler;

@RestController
@CrossOrigin
public class SupplierOrderQryResourceController {

  @Autowired private SupplierOrderQueryHandler supplierOrderQueryHandler;

  @PostMapping(
      path = "/v1/manage/order/search",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse getSupplierOrders(
      @Valid @RequestBody SupplierOrderSearchRequest supplierOrderSearchRequest,
      Pageable pageable) {
    return supplierOrderQueryHandler.getSupplierOrders(supplierOrderSearchRequest, pageable);
  }

  @GetMapping(path = "/v1/manage/order/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public SearchOrderResponse getSupplierOrder(
      @PathVariable(name = "orderId", required = true) int orderId,
      @RequestParam(value = "source", required = false) final String source) {
    return supplierOrderQueryHandler.getSupplierOrder(orderId, source);
  }
}
