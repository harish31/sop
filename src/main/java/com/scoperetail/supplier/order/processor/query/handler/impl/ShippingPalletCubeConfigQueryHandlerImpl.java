package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scoperetail.common.rest.client.model.supplier.Supplier;
import com.scoperetail.common.rest.client.service.api.SupplierService;
import com.scoperetail.order.persistence.entity.ShippingPalletCubeConfig;
import com.scoperetail.order.persistence.repository.ShippingPalletCubeConfigRepository;
import com.scoperetail.supplier.order.processor.query.handler.ShippingPalletCubeConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.ShippingPalletCubeConfigResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShippingPalletCubeConfigQueryHandlerImpl implements ShippingPalletCubeConfigQueryHandler {

  @Autowired private ShippingPalletCubeConfigRepository supplierPalletConfigRepository;

  @Autowired private SupplierService supplierService;

  @Override
  public List<ShippingPalletCubeConfigResponse> getShippingPalletCubeConfig(String distCenterId) {

    Optional<List<Supplier>> optionalSupplierIds =
        supplierService.retrieveSuppliersByDistCenterId(distCenterId);

    final List<Long> supplierIds =
        optionalSupplierIds
            .orElse(new ArrayList<Supplier>())
            .stream()
            .map(supplier -> supplier.getSupplierId())
            .collect(Collectors.toList());
    log.info(
        "Retrieving Supplier Pallet Configuration for suppliers {} from distribution center id {}",
        supplierIds,
        distCenterId);

    Map<Long, ShippingPalletCubeConfig> configMap =
        supplierPalletConfigRepository
            .findBySupplierIdIn(supplierIds)
            .stream()
            .collect(Collectors.toMap(config -> config.getSupplierId(), config -> config));

    return supplierIds
        .stream()
        .map(
            supplierID -> {
              ShippingPalletCubeConfig cubeConfig = configMap.get(supplierID);
              return ShippingPalletCubeConfigResponse.builder()
                  .id(cubeConfig != null ? cubeConfig.getId() : null)
                  .supplierId(supplierID)
                  .shippingPalletCube(cubeConfig != null ? cubeConfig.getShippingPalletCube() : BigDecimal.ONE)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
