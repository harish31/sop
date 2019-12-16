package com.scoperetail.supplier.order.processor.contrive.handler;

import static com.scoperetail.commons.util.XMLUtil.isValidMessage;
import static com.scoperetail.commons.util.XMLUtil.unmarshell;
import static com.scoperetail.internal.schema.EventName.CAMS_DOWNLOAD_COMPLETE;
import static com.scoperetail.parley.impl.TaskResult.DISCARD;
import static com.scoperetail.parley.impl.TaskResult.FAILURE;
import static com.scoperetail.parley.impl.TaskResult.SUCCESS;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.CAMS_EVENT_ERROR_MESSAGE_TARGET;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.CAMS_EVENT_INBOUND_MESSAGE_SOURCE;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.xml.validation.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.internal.schema.CamsEvent;
import com.scoperetail.internal.schema.CamsEvent.OrderIds;
import com.scoperetail.parley.impl.TaskResult;
import com.scoperetail.parley.spi.ITaskHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.SupplierOrderCmdHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * This Task Handler handles the CAMS_DOWNLOAD_COMPLETE event receives from Cams.
 */
@Component
@Slf4j
public class CamsEventTaskHandler implements ITaskHandler<String> {

  @Autowired private Environment env;

  @Autowired private ContriveManager contriveManager;

  @Autowired private Schema camsDownloadCompleteEventInboundSchema;

  @Autowired private SupplierOrderCmdHandler supplierOrderCmdHandler;

  @Autowired private CamsEventFailureTaskHandler failureTaskHandler;

  @PostConstruct
  private void initialize() throws ContriveException {
    try {
      final String contriveListener = env.getProperty(CAMS_EVENT_INBOUND_MESSAGE_SOURCE);
      contriveManager.initMessageListener(contriveListener, this, failureTaskHandler);
      log.info("Contrive Message listner enabled. Listener name:" + contriveListener);
    } catch (final ContriveException e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public TaskResult doTask(final String message) {
    log.info("Received CAMS event message {}", message);
    TaskResult result = FAILURE;
    try {
      if (isValidMessage(ofNullable(message), ofNullable(camsDownloadCompleteEventInboundSchema))) {
        final CamsEvent camsEvent =
            unmarshell(ofNullable(message), ofNullable(camsDownloadCompleteEventInboundSchema), CamsEvent.class);
        log.debug("CAMS event {} received with event name = ", camsEvent.getEventName());
        if (CAMS_DOWNLOAD_COMPLETE.equals(camsEvent.getEventName())) {
          Optional.ofNullable(camsEvent.getOrderIds())
              .map(OrderIds::getOrderId)
              .ifPresent(orderIds -> supplierOrderCmdHandler.setIsLockedForEdit(orderIds, 'Y'));
          result = SUCCESS;
        }
      } else {
        result = DISCARD;
        log.error("Invalid message for CAMS file download status update processing.");
        contriveManager.send(env.getProperty(CAMS_EVENT_ERROR_MESSAGE_TARGET), message);
      }
    } catch (final Exception e) {
      log.error("Exception occured while processing CAMS file download status.", e);
      result = FAILURE;
    }
    log.info("CAMS File Download status updated = {}", result);
    return result;
  }
}
