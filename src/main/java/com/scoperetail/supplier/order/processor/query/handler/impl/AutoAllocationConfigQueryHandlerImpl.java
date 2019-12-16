package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.scoperetail.common.rest.client.model.supplier.Supplier;
import com.scoperetail.common.rest.client.service.api.SupplierService;
import com.scoperetail.order.persistence.entity.AutoAllocationConfig;
import com.scoperetail.order.persistence.repository.AutoAllocationConfigRepository;
import com.scoperetail.supplier.order.processor.query.handler.AutoAllocationConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AutoAllocationConfigQueryHandlerImpl implements AutoAllocationConfigQueryHandler {

  @Autowired AutoAllocationConfigRepository autoAllocationConfigRepository;

  @Autowired private SupplierService supplierService;

  @Value("${auto.allocation.config.default.release.offset}")
  private Short autoAllocDefaultReleaseConfig;

  @Value("${auto.allocation.config.default.total.duration}")
  private Short autoAllocDefaultTotalDurationConfig;

  @Override
  public List<AutoAllocationConfigResponse> getAutoAllocationConfig(String distCenterId) {
    Optional<List<Supplier>> optionalSupplierIds =
        supplierService.retrieveSuppliersByDistCenterId(distCenterId);
    List<AutoAllocationConfigResponse> response = new ArrayList<>();
    if (optionalSupplierIds.isPresent() && !optionalSupplierIds.get().isEmpty()) {
      final List<Long> supplierIds =
          optionalSupplierIds
              .get()
              .stream()
              .map(s -> s.getSupplierId())
              .collect(Collectors.toList());
      log.info(
          "Retrieving Auto Allocation Configuration for suppliers {} from distribution center id {}",
          supplierIds,
          distCenterId);

      Optional<List<AutoAllocationConfig>> optionalConfigs =
          autoAllocationConfigRepository.findBySupplierIdIn(supplierIds);
      final Map<Long, AutoAllocationConfig> configMap =
          optionalConfigs.isPresent()
              ? optionalConfigs
                  .get()
                  .stream()
                  .collect(Collectors.toMap(c -> c.getSupplierId(), c -> c))
              : new HashMap<>();

      supplierIds.forEach(
          s -> {
            AutoAllocationConfig config = configMap.get(s);
            if (config == null) {
              log.info("No Auto Allocation Configration found for Supplier {}", s);
              config =
                  AutoAllocationConfig.builder()
                      .supplierId(s)
                      .releaseOffset(autoAllocDefaultReleaseConfig)
                      .totalDuration(autoAllocDefaultTotalDurationConfig)
                      .build();
            }
            response.add(buildAutoAllocationConfigResponse(config));
          });
    }
    return response;
  }

  private AutoAllocationConfigResponse buildAutoAllocationConfigResponse(
      AutoAllocationConfig config) {
    return AutoAllocationConfigResponse.builder()
        .configId(config.getId())
        .supplierId(config.getSupplierId())
        .cutoffOffset(config.getCutoffOffset())
        .releaseOffset(config.getReleaseOffset())
        .totalDuration(config.getTotalDuration())
        .build();
  }
}
