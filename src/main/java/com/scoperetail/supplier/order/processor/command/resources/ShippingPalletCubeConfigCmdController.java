package com.scoperetail.supplier.order.processor.command.resources;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.supplier.order.processor.command.handler.api.ShippingPalletCubeConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigUpdateRequest;

@RestController
@CrossOrigin
@RequestMapping("/v1/shippingPalletCube/config")
public class ShippingPalletCubeConfigCmdController {

  @Autowired private ShippingPalletCubeConfigCmdHandler supplierPalletConfigCmdHandler;

  @PostMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ShippingPalletCubeConfigCreateResponse createShippingPalletCubeConfig(
      @Valid @RequestBody ShippingPalletCubeConfigCreateRequest supplierPalletConfigCreateRequest) {
    return supplierPalletConfigCmdHandler.createShippingPalletCubeConfig(
        supplierPalletConfigCreateRequest);
  }

  @PatchMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse updateShippingPalletCubeConfig(
      @Valid @RequestBody ShippingPalletCubeConfigUpdateRequest supplierPalletConfigUpdateRequest) {
    return supplierPalletConfigCmdHandler.updateShippingPalletCubeConfig(
        supplierPalletConfigUpdateRequest);
  }
}
