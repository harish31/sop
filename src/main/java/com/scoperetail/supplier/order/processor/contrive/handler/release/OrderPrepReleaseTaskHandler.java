package com.scoperetail.supplier.order.processor.contrive.handler.release;

import static com.scoperetail.parley.impl.TaskResult.DISCARD;
import static com.scoperetail.parley.impl.TaskResult.FAILURE;
import static com.scoperetail.parley.impl.TaskResult.SUCCESS;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ORDER_PREPRELEASE_ERROR_MESSAGE_TARGET;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.RELEASE_INBOUND_MESSAGE_SOURCE;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.util.JsonUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.oms.schema.OutboundCustomerOrder;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.parley.impl.TaskResult;
import com.scoperetail.parley.spi.ITaskHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.PrepareReleaseCmdHandler;
import com.scoperetail.supplier.order.processor.contrive.handler.release.inbound.Event;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderPrepReleaseTaskHandler implements ITaskHandler<String> {

  @Autowired private Environment env;
  @Autowired private ContriveManager contriveManager;
  @Autowired private PrepareReleaseCmdHandler releaseCmdHandler;
  @Autowired private OrderPrepReleaseFailureTaskHandler orderPrepReleaseFailureTaskHandler;

  @Autowired
  @Qualifier("releasedOutboundHandler")
  private OutBoundEventHandler outboundHandler;

  @PostConstruct
  private void initialize() throws ContriveException {
    try {
      final String contriveListener = env.getProperty(RELEASE_INBOUND_MESSAGE_SOURCE);
      contriveManager.initMessageListener(
          contriveListener, this, orderPrepReleaseFailureTaskHandler);
      log.info("Contrive Message listener enabled. listener name:" + contriveListener);
    } catch (final ContriveException e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public TaskResult doTask(final String message) {
    log.info("The Message:" + message);
    TaskResult result = FAILURE;
    Event event = null;
    try {
      event = JsonUtil.convertFromJson(message, Event.class);
    } catch (final IOException e) {
      log.error("Exception while converting the json inbound Message ::{}:: ", message, e);
      result = DISCARD;
      try {
        contriveManager.send(env.getProperty(ORDER_PREPRELEASE_ERROR_MESSAGE_TARGET), message);
      } catch (final ContriveException e1) {
        log.error(
            "Exception occurred while sending discarded message {} to error inbound channel: {}",
            message,
            ORDER_PREPRELEASE_ERROR_MESSAGE_TARGET,
            e);
      }
    }
    if (DISCARD != result) {
      result = createOutBoundOrder(message, event);
    }
    return result;
  }

  public TaskResult createOutBoundOrder(final String message, final Event event) {
    TaskResult result = FAILURE;
    try {
      final Optional<SupplierOrder> supplierOrder =
          releaseCmdHandler.readSupplierOrder(event.getOrderId());
      final SupplierOrder order = supplierOrder.orElseThrow(NoResultException::new);
      final OutboundCustomerOrder orderPrepRelease = releaseCmdHandler.createOutboundOrder(order);
      releaseCmdHandler.updateSupplierOrder(event.getOrderId());
      outboundHandler.send(orderPrepRelease);
      result = SUCCESS;
    } catch (final Exception e) {
      log.error("Exception while prepare relase order message {}", message, e);
    }
    return result;
  }
}
