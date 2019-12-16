package com.scoperetail.supplier.order.processor.command.handler.api;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigUpdateRequest;

public interface SeafoodConfigCmdHandler {
  SeafoodConfigCreateResponse createSeafoodConfig(
      SeafoodConfigCreateRequest seafoodConfigCreateRequest);

  BaseResponse updateSeafoodConfig(SeafoodConfigUpdateRequest seafoodConfigUpdateRequest);
}
