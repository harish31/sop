package com.scoperetail.supplier.order.processor.query.resources;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.commons.orderLine.search.dto.request.OrderLineSearchRequest;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.handler.OrderLineQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.AdjustmentReasonsResponse;

@Validated
@RestController
@CrossOrigin
public class OrderLineQryResourceController {

  @Autowired OrderLineQueryHandler orderLineQueryHandler;

  @PostMapping(
      path = "/v1/orderlines/substitutions/search",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse searchSubstitutedLines(     
          @Valid
          @RequestBody
          OrderLineSearchRequest searchRequest,     
       @RequestParam (required = false) List<String> sortBy, Pageable pageable) {
    return orderLineQueryHandler.searchSubstitutedLines(searchRequest, pageable, sortBy);
  }

  @PostMapping(
      path = "/v1/orderlines/rejections/search",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse searchRejectedLines(
		  @SortDefault.SortDefaults({
	            @SortDefault(sort = "so.supplierId", direction = Sort.Direction.ASC),
	            @SortDefault(sort = "productId", direction = Sort.Direction.ASC),
	            @SortDefault(sort = "oc.customerId", direction = Sort.Direction.ASC)
	          }) Pageable pageable,
      @Valid @RequestBody OrderLineSearchRequest searchRequest) {
    return orderLineQueryHandler.searchRejectedLines(searchRequest, pageable);
  }

  @PostMapping(
      path = "/v1/orderlines/changeinqty/search",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse searchAdjustedLines(
		  @SortDefault.SortDefaults({
	            @SortDefault(sort = "supplierId", direction = Sort.Direction.ASC),
	            @SortDefault(sort = "ol.productId", direction = Sort.Direction.ASC),
	            @SortDefault(sort = "oc.customerId", direction = Sort.Direction.ASC)
	          })Pageable pageable,
      @Valid @RequestBody OrderLineSearchRequest searchRequest) {
    return orderLineQueryHandler.searchAdjustedLines(searchRequest, pageable);
  }

  @GetMapping(path = "/v1/adjustmentreasons", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AdjustmentReasonsResponse> retrieveAdjustmentReasons(
      @RequestParam(value = "source", required = false) final String source) {
    return orderLineQueryHandler.retrieveAdjustmentReasons(source);
  }
}
