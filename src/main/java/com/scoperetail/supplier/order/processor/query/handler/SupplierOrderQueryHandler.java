package com.scoperetail.supplier.order.processor.query.handler;

import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.commons.manage.order.dto.response.SearchOrderResponse;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierOrderQueryHandler {
  BaseResponse getSupplierOrders(
      SupplierOrderSearchRequest supplierOrderSearchRequest, Pageable pageable);

  SearchOrderResponse getSupplierOrder(Integer orderId, String source);

  Page<SupplierOrder> searchSupplierOrders(
      final SupplierOrderSearchRequest supplierOrderSearchRequest, final Pageable pageable);
}
