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
import com.scoperetail.supplier.order.processor.command.handler.api.SeafoodConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigUpdateRequest;

@RestController
@CrossOrigin
@RequestMapping("/v1/seafood/config")
public class SeafoodConfigCmdController {

  @Autowired private SeafoodConfigCmdHandler seafoodConfigCmdHandler;

  @PostMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public SeafoodConfigCreateResponse createSeafoodConfig(
      @Valid @RequestBody SeafoodConfigCreateRequest seafoodConfigCreateRequest) {
    return seafoodConfigCmdHandler.createSeafoodConfig(seafoodConfigCreateRequest);
  }

  @PatchMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse updateSeafoodConfig(
      @Valid @RequestBody SeafoodConfigUpdateRequest seafoodConfigUpdateRequest) {
    return seafoodConfigCmdHandler.updateSeafoodConfig(seafoodConfigUpdateRequest);
  }
}
