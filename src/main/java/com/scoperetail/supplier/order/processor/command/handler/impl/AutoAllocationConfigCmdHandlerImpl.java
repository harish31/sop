package com.scoperetail.supplier.order.processor.command.handler.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.order.persistence.entity.AutoAllocationConfig;
import com.scoperetail.order.persistence.repository.AutoAllocationConfigRepository;
import com.scoperetail.supplier.order.processor.command.handler.api.AutoAllocationConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigUpdateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AutoAllocationConfigCmdHandlerImpl implements AutoAllocationConfigCmdHandler {
  @Autowired private AutoAllocationConfigRepository autoAllocationConfigRepository;

  @Override
  @Transactional
  public AutoAllocationConfigCreateResponse createAutoAllocationConfig(
      AutoAllocationConfigCreateRequest autoAllocationConfigCreateRequest) {
    log.info(
        "Creating Auto Allocation Configuration for supplier {}",
        autoAllocationConfigCreateRequest.getSupplierId());
    AutoAllocationConfig autoAllocationConfig =
        AutoAllocationConfig.builder()
            .supplierId(autoAllocationConfigCreateRequest.getSupplierId())
            .releaseOffset(autoAllocationConfigCreateRequest.getReleaseOffset())
            .cutoffOffset(autoAllocationConfigCreateRequest.getCutoffOffset())
            .totalDuration(autoAllocationConfigCreateRequest.getTotalDuration())
            .build();
    AutoAllocationConfig config = autoAllocationConfigRepository.save(autoAllocationConfig);
    return new AutoAllocationConfigCreateResponse(config.getId());
  }

  @Override
  @Transactional
  public BaseResponse updateAutoAllocationConfig(
      AutoAllocationConfigUpdateRequest autoAllocationConfigUpdateRequest) {
    Optional<AutoAllocationConfig> optionalConfig =
        autoAllocationConfigRepository.findById(autoAllocationConfigUpdateRequest.getConfigId());
    BaseResponse baseResponse =
        new BaseResponse(BaseResponse.Status.FAILURE, "No records to update");
    if (optionalConfig.isPresent()) {
      AutoAllocationConfig config = optionalConfig.get();
      log.info("Updating Auto Allocation Configuration for supplier {}", config.getSupplierId());
      config.setReleaseOffset(autoAllocationConfigUpdateRequest.getReleaseOffset());
      config.setCutoffOffset(autoAllocationConfigUpdateRequest.getCutoffOffset());
      baseResponse =
          new BaseResponse(BaseResponse.Status.SUCCESS, "Configuration updated successfully ");
    }
    return baseResponse;
  }
}
