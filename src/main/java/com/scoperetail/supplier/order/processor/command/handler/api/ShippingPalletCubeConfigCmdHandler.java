package com.scoperetail.supplier.order.processor.command.handler.api;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigUpdateRequest;

public interface ShippingPalletCubeConfigCmdHandler {
  ShippingPalletCubeConfigCreateResponse createShippingPalletCubeConfig(
      ShippingPalletCubeConfigCreateRequest supplierPalletConfigCreateRequest);

  BaseResponse updateShippingPalletCubeConfig(
      ShippingPalletCubeConfigUpdateRequest supplierPalletConfigUpdateRequest);
}
