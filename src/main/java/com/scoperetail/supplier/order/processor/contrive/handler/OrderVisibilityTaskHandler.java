package com.scoperetail.supplier.order.processor.contrive.handler;

import static com.scoperetail.parley.impl.TaskResult.DISCARD;
import static com.scoperetail.parley.impl.TaskResult.FAILURE;
import static com.scoperetail.parley.impl.TaskResult.SUCCESS;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ORDER_VISIBILITY_ERROR;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ORDER_VISIBILITY_SOURCE;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.scoperetail.supplier.order.processor.command.handler.impl.OrderVisibilityOutboundTerminatorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.commons.util.JsonUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.oms.schema.OrderLog;
import com.scoperetail.parley.impl.TaskResult;
import com.scoperetail.parley.spi.ITaskHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.OrderVisibilityLogCmdHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderVisibilityTaskHandler implements ITaskHandler<String> {

  @Autowired private Environment env;
  @Autowired private ContriveManager contriveManager;
  @Autowired private OrderVisibilityFailureTaskHandler failureTaskHandler;
  @Autowired private OrderVisibilityLogCmdHandler orderVisibilityLogCmdHandler;

  @Autowired private OutBoundEventHandler<OrderLog> orderVisibilityOutboundHandler;
  @Autowired private OrderVisibilityOutboundTerminatorHandler orderVisibilityOutboundTerminatorHandler;

  @Value("${send.ov.via.terminator}")
  private boolean isSendViaTerminatorEnabled;
  @PostConstruct
  private void initialize() throws ContriveException {
    try {
      final String constriveListener = env.getProperty(ORDER_VISIBILITY_SOURCE);
      contriveManager.initMessageListener(constriveListener, this, failureTaskHandler);
      log.info("Contrive Message listner enabled. Lintener name:" + constriveListener);
    } catch (final ContriveException e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }
  @Override
  public TaskResult doTask(final String message) {
    log.info("Order visibility inbound message received {}", message);
    TaskResult result = FAILURE;
    try {
      final OlcmEvent enrichementEvent = JsonUtil.convertFromJson(message, OlcmEvent.class);
      OrderLog orderLog =
          orderVisibilityLogCmdHandler.createOutboundOrderVisibilityLog(
              enrichementEvent.getOrderId());
      if (Optional.ofNullable(orderLog).isPresent()) {
        if(isSendViaTerminatorEnabled){
          log.info("Order visibility log built, sending to terminator.");
          orderVisibilityOutboundTerminatorHandler.send(orderLog);
        }
        else {
          log.info("Order visibility log built, sending to outbound.");
          orderVisibilityOutboundHandler.send(orderLog);
        }
      }
      result = SUCCESS;
    } catch (final Exception e) {
      log.error("Order visibility json validation failed, message discarded exception {}", e);
      result = DISCARD;
      try {
        contriveManager.send(env.getProperty(ORDER_VISIBILITY_ERROR), message);
      } catch (final ContriveException e1) {
        log.error("Exception occurred while discarding inbound Message ::{}", e1);
      }
    }
    log.info("Order visibility dotask completed with result {}", result);
    return result;
  }
}
