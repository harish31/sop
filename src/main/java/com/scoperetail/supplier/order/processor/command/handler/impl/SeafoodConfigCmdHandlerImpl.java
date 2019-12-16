package com.scoperetail.supplier.order.processor.command.handler.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.order.persistence.entity.SeaFoodConfig;
import com.scoperetail.order.persistence.repository.SeaFoodConfigRepository;
import com.scoperetail.supplier.order.processor.command.handler.api.SeafoodConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigUpdateRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SeafoodConfigCmdHandlerImpl implements SeafoodConfigCmdHandler {

  @Autowired SeaFoodConfigRepository seaFoodConfigRepository;

  @Override
  @Transactional
  public SeafoodConfigCreateResponse createSeafoodConfig(
      SeafoodConfigCreateRequest seafoodConfigCreateRequest) {
    log.info(
        "Creating Seafood Configuration for supplier {}",
        seafoodConfigCreateRequest.getSupplierId());
    SeaFoodConfig seafoodConfig =
        SeaFoodConfig.builder()
            .supplierId(seafoodConfigCreateRequest.getSupplierId())
            .isEnabled(seafoodConfigCreateRequest.getEnabled())
            .build();
    seaFoodConfigRepository.save(seafoodConfig);
    return new SeafoodConfigCreateResponse(seafoodConfig.getId());
  }

  @Override
  @Transactional
  public BaseResponse updateSeafoodConfig(SeafoodConfigUpdateRequest seafoodConfigUpdateRequest) {
    Optional<SeaFoodConfig> optionalConfig =
        seaFoodConfigRepository.findById(seafoodConfigUpdateRequest.getConfigId());
    BaseResponse baseResponse =
        new BaseResponse(BaseResponse.Status.FAILURE, "No records to update");
    if (optionalConfig.isPresent()) {
      SeaFoodConfig config = optionalConfig.get();
      log.info("Updating Seafood Configuration for supplier {}", config.getSupplierId());
      config.setIsEnabled(seafoodConfigUpdateRequest.getEnabled());
      baseResponse =
          new BaseResponse(BaseResponse.Status.SUCCESS, "Configuration updated successfully ");
    }
    return baseResponse;
  }
}
