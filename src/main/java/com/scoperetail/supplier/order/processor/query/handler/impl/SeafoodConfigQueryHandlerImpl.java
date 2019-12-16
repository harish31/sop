package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scoperetail.common.rest.client.model.supplier.Supplier;
import com.scoperetail.common.rest.client.service.api.SupplierService;
import com.scoperetail.order.persistence.entity.SeaFoodConfig;
import com.scoperetail.order.persistence.repository.SeaFoodConfigRepository;
import com.scoperetail.supplier.order.processor.query.handler.SeafoodConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SeafoodConfigQueryHandlerImpl implements SeafoodConfigQueryHandler {

  @Autowired private SeaFoodConfigRepository seaFoodConfigRepository;

  @Autowired private SupplierService supplierService;

  @Override
  public List<SeafoodConfigResponse> getSeafoodConfig(String distCenterId) {
    Optional<List<Supplier>> optionalSupplierIds =
        supplierService.retrieveSuppliersByDistCenterId(distCenterId);
    List<SeafoodConfigResponse> response = new ArrayList<>();
    if (optionalSupplierIds.isPresent() && !optionalSupplierIds.get().isEmpty()) {
      final List<Long> supplierIds =
          optionalSupplierIds
              .get()
              .stream()
              .map(s -> s.getSupplierId())
              .collect(Collectors.toList());
      log.info(
          "Retrieving Seafood Configuration for suppliers {} from distribution center id {}",
          supplierIds,
          distCenterId);

      List<SeaFoodConfig> configs = seaFoodConfigRepository.findBySupplierIdIn(supplierIds);
      final Map<Long, SeaFoodConfig> configMap =
          !configs.isEmpty()
              ? configs.stream().collect(Collectors.toMap(c -> c.getSupplierId(), c -> c))
              : new HashMap<>();

      supplierIds.forEach(
          s -> {
            SeaFoodConfig config = configMap.get(s);
            if (config == null) {
              log.info("No Seafood Configration found for Supplier {}", s);
              config = SeaFoodConfig.builder().supplierId(s).isEnabled('N').build();
            }
            response.add(buildSeafoodConfigResponse(config));
          });
    }
    return response;
  }

  private SeafoodConfigResponse buildSeafoodConfigResponse(SeaFoodConfig config) {
    return SeafoodConfigResponse.builder()
        .configId(config.getId())
        .supplierId(config.getSupplierId())
        .enabled(config.getIsEnabled())
        .build();
  }
}
