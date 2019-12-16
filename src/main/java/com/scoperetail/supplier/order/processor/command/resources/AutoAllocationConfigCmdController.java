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
import com.scoperetail.supplier.order.processor.command.handler.api.AutoAllocationConfigCmdHandler;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateResponse;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigUpdateRequest;

@RestController
@CrossOrigin
@RequestMapping("/v1/auto-allocation/config")
public class AutoAllocationConfigCmdController {

  @Autowired private AutoAllocationConfigCmdHandler autoAllocationConfigCmdHandler;

  @PostMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public AutoAllocationConfigCreateResponse createAutoAllocationConfig(
      @Valid @RequestBody AutoAllocationConfigCreateRequest autoAllocationConfigCreateRequest) {
    return autoAllocationConfigCmdHandler.createAutoAllocationConfig(
        autoAllocationConfigCreateRequest);
  }

  @PatchMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse updateAutoAllocationConfig(
      @Valid @RequestBody AutoAllocationConfigUpdateRequest autoAllocationConfigUpdateRequest) {
    return autoAllocationConfigCmdHandler.updateAutoAllocationConfig(
        autoAllocationConfigUpdateRequest);
  }
}
