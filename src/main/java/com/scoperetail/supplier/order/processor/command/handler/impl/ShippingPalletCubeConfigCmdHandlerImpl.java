package com.scoperetail.supplier.order.processor.command.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.order.persistence.entity.ShippingPalletCubeConfig;
import com.scoperetail.order.persistence.repository.ShippingPalletCubeConfigRepository;
import com.scoperetail.supplier.order.processor.command.handler.api.ShippingPalletCubeConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigUpdateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShippingPalletCubeConfigCmdHandlerImpl implements ShippingPalletCubeConfigCmdHandler {

  @Autowired ShippingPalletCubeConfigRepository supplierPalletConfigRepository;

  @Override
  public ShippingPalletCubeConfigCreateResponse createShippingPalletCubeConfig(
      ShippingPalletCubeConfigCreateRequest supplierPalletConfigCreateRequest) {
    log.info(
        "Creating Supplier Pallet Configuration for supplier {}",
        supplierPalletConfigCreateRequest.getSupplierId());
    ShippingPalletCubeConfig supplierPalletConfig =
        ShippingPalletCubeConfig.builder()
            .supplierId(supplierPalletConfigCreateRequest.getSupplierId())
            .shippingPalletCube(supplierPalletConfigCreateRequest.getShippingPalletCube())
            .build();
    supplierPalletConfigRepository.save(supplierPalletConfig);
    return new ShippingPalletCubeConfigCreateResponse(supplierPalletConfig.getId());
  }

  @Override
  public BaseResponse updateShippingPalletCubeConfig(
      ShippingPalletCubeConfigUpdateRequest supplierPalletConfigUpdateRequest) {

    ShippingPalletCubeConfig configEntity =
        supplierPalletConfigRepository
            .findById(supplierPalletConfigUpdateRequest.getId())
            .map(
                config -> {
                  config.setShippingPalletCube(
                      supplierPalletConfigUpdateRequest.getShippingPalletCube());
                  return config;
                })
            .orElseThrow(() -> new ApplicationException("No records to update"));

    supplierPalletConfigRepository.save(configEntity);
    return new BaseResponse();
  }
}
