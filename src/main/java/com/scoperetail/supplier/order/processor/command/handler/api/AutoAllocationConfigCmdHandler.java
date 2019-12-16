package com.scoperetail.supplier.order.processor.command.handler.api;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigUpdateRequest;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateResponse;

public interface AutoAllocationConfigCmdHandler {
  BaseResponse updateAutoAllocationConfig(
      AutoAllocationConfigUpdateRequest autoAllocationConfigUpdateRequest);

  AutoAllocationConfigCreateResponse createAutoAllocationConfig(
      AutoAllocationConfigCreateRequest autoAllocationConfigCreateRequest);
}
