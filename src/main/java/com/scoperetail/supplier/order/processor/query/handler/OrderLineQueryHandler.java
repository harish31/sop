package com.scoperetail.supplier.order.processor.query.handler;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;

import com.scoperetail.commons.orderLine.search.dto.request.OrderLineSearchRequest;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.model.AdjustmentReasonsResponse;

public interface OrderLineQueryHandler {

  BaseResponse searchSubstitutedLines(
      final OrderLineSearchRequest searchRequest,
      final Pageable pageable,
      final List<String> sortBy);

  BaseResponse searchRejectedLines(@Valid OrderLineSearchRequest searchRequest, Pageable pageable);

  BaseResponse searchAdjustedLines(
      final OrderLineSearchRequest searchRequest, final Pageable pageable);

  List<AdjustmentReasonsResponse> retrieveAdjustmentReasons(final String source);
}
