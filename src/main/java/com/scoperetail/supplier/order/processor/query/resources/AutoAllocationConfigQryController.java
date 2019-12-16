package com.scoperetail.supplier.order.processor.query.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.supplier.order.processor.query.handler.AutoAllocationConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigResponse;

@RestController
@CrossOrigin
@RequestMapping("/v1/auto-allocation/config")
public class AutoAllocationConfigQryController {

  @Autowired private AutoAllocationConfigQueryHandler autoAllocationConfigQueryHandler;

  @GetMapping(value = "/{distCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AutoAllocationConfigResponse> getAutoAllocationConfig(
      @PathVariable("distCenterId") String distCenterId) {
    return autoAllocationConfigQueryHandler.getAutoAllocationConfig(distCenterId);
  }
}
