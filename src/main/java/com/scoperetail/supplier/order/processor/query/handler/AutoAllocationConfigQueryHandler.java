package com.scoperetail.supplier.order.processor.query.handler;

import java.util.List;

import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigResponse;

public interface AutoAllocationConfigQueryHandler {
  List<AutoAllocationConfigResponse> getAutoAllocationConfig(String distCenterId);
}
