package com.scoperetail.supplier.order.processor.query.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.supplier.order.processor.query.handler.ShippingPalletCubeConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigResponse;

@RestController
@CrossOrigin
@RequestMapping("/v1/shippingPalletCube/config")
public class ShippingPalletCubeConfigController {

  @Autowired private ShippingPalletCubeConfigQueryHandler supplierPalletCubeConfigQueryHandler;

  @GetMapping(value = "/{distCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ShippingPalletCubeConfigResponse> getShippingPalletCubeConfig(
      @PathVariable("distCenterId") String distCenterId) {
    return supplierPalletCubeConfigQueryHandler.getShippingPalletCubeConfig(distCenterId);
  }
}

