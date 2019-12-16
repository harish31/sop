package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.SUPPLIER_ORDER_TARGET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.commons.util.JsonUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;

import lombok.extern.slf4j.Slf4j;

@Primary
@Component
@Slf4j
public class SupplierOrderOutboundHandler implements OutBoundEventHandler<OlcmEvent> {
  private @Autowired Environment env;

  @Autowired private ContriveManager contriveManager;

  @Override
  public void send(final OlcmEvent olcmEvent) throws ApplicationException {
    final String target = env.getProperty(SUPPLIER_ORDER_TARGET);
    final String eventMessage = JsonUtil.toJson(olcmEvent);
    log.debug("Sending message from SOP ===========> OLCM message data {}", eventMessage);
    try {
      contriveManager.send(target, eventMessage);
    } catch (final ContriveException e) {
      log.error("Problem in sending message to OLCM endpoint :{} with error {}", target, e);
      throw new ApplicationException("Problem in sending message to OLCM endpoint: " + target, e);
    }
  }
}
