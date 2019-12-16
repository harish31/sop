package com.scoperetail.supplier.order.processor.query.handler;

import java.util.List;

import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigResponse;

public interface ShippingPalletCubeConfigQueryHandler {
  List<ShippingPalletCubeConfigResponse> getShippingPalletCubeConfig(String distCenterId);
}
