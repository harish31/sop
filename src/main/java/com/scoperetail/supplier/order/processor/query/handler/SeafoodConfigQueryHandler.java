package com.scoperetail.supplier.order.processor.query.handler;

import java.util.List;

import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigResponse;

public interface SeafoodConfigQueryHandler {
  List<SeafoodConfigResponse> getSeafoodConfig(String distCenterId);
}
