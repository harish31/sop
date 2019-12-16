package com.scoperetail.supplier.order.processor.query.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scoperetail.supplier.order.processor.query.handler.SeafoodConfigQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.SeafoodConfigResponse;

@RestController
@CrossOrigin
@RequestMapping("/v1/seafood/config")
public class SeafoodConfigQryController {

  @Autowired private SeafoodConfigQueryHandler seafoodConfigQueryHandler;

  @GetMapping(value = "/{distCenterId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<SeafoodConfigResponse> getSeafoodConfig(
      @PathVariable("distCenterId") String distCenterId) {
    return seafoodConfigQueryHandler.getSeafoodConfig(distCenterId);
  }
}
