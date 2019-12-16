package com.scoperetail.supplier.order.processor.contrive.handler;

import static com.scoperetail.parley.impl.TaskResult.SUCCESS;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.CAMS_EVENT_BO_MESSAGE_TARGET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.parley.impl.TaskResult;
import com.scoperetail.parley.spi.IFailureHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CamsEventFailureTaskHandler implements IFailureHandler<String> {

  private @Autowired Environment env;

  @Autowired private ContriveManager contriveManager;

  @Override
  public TaskResult doTask(final String message) {
    final String target = env.getProperty(CAMS_EVENT_BO_MESSAGE_TARGET);
    try {
      log.info("Sending Message to CAMS event handling Backout Queue - {}", target);
      contriveManager.send(target, message);
    } catch (final Exception e) {
      log.error("Exception while sending message to CAMS event handling Backout Queue:", e);
      log.error("The Message is now lost:{}", message);
    }
    return SUCCESS;
  }
}
