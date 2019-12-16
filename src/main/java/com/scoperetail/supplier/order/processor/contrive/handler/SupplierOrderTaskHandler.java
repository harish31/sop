package com.scoperetail.supplier.order.processor.contrive.handler;

import static com.scoperetail.commons.util.XMLUtil.isValidMessage;
import static com.scoperetail.commons.util.XMLUtil.unmarshell;
import static com.scoperetail.parley.impl.TaskResult.DISCARD;
import static com.scoperetail.parley.impl.TaskResult.FAILURE;
import static com.scoperetail.parley.impl.TaskResult.SUCCESS;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.INBOUND_MESSAGE_SOURCE;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.SUPPLIER_ORDER_ERROR_MESSAGE_TARGET;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.validation.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.parley.impl.TaskResult;
import com.scoperetail.parley.spi.ITaskHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.SupplierOrderCmdHandler;
import com.scoperetail.supplier.order.processor.command.model.CustomerOrder;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SupplierOrderTaskHandler implements ITaskHandler<String> {

  private @Autowired Environment env;

  @Autowired private ContriveManager contriveManager;

  @Autowired private Schema supplierOrderInboundSchema;

  @Autowired private SupplierOrderCmdHandler supplierOrderCmdHandler;

  @Autowired private SupplierOrderFailureTaskHandler failureTaskHandler;

  @PostConstruct
  private void initialize() throws ContriveException {
    try {
      final String constriveListener = env.getProperty(INBOUND_MESSAGE_SOURCE);
      contriveManager.initMessageListener(constriveListener, this, failureTaskHandler);
      log.info("Contrive Message listner enabled. Lintener name:" + constriveListener);
    } catch (final ContriveException e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public TaskResult doTask(final String message) {
    log.info("Create supplier order for message {}", message);
    TaskResult result = FAILURE;
    try {
      if (isValidMessage(ofNullable(message), ofNullable(supplierOrderInboundSchema))) {
        final CustomerOrder customerOrder =
            unmarshell(
                ofNullable(message), ofNullable(supplierOrderInboundSchema), CustomerOrder.class);
        log.debug("Customer order {} received for enrichment ", customerOrder.getOrderId());
        final List<OlcmEvent> events = new ArrayList<>();
        supplierOrderCmdHandler.createSupplierOrder(null, customerOrder, events);
        supplierOrderCmdHandler.sendEventToOlcm(events);
        result = SUCCESS;
      } else {
        result = DISCARD;
        log.error("Invalid message for supplier order processing.");
        contriveManager.send(env.getProperty(SUPPLIER_ORDER_ERROR_MESSAGE_TARGET), message);
      }
    } catch (final Exception e) {
      log.error("Error occurred while creating supplier orders.", e);
    }
    log.info("Create supplier order process completed {}", result);
    return result;
  }
}
