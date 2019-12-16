package com.scoperetail.supplier.order.processor.query.handler;

import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.supplier.order.processor.query.model.SearchOrderResponseCsv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierOrderCsvHandler {
    Page<SearchOrderResponseCsv> getSearchOrderResponseCsv(
            SupplierOrderSearchRequest supplierOrderSearchRequest, Pageable pageable);
}
