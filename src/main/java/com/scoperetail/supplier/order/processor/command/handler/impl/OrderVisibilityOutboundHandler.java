package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ORDER_VISIBILITY_OUTBOUND;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.util.XMLUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.oms.schema.OrderLog;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderVisibilityOutboundHandler implements OutBoundEventHandler<OrderLog> {
  @Autowired private Environment env;
  @Autowired private ContriveManager contriveManager;
  @Autowired private Schema orderVisibilityOutboundSchema;

  @Override
  public void send(final OrderLog orderLog) throws ApplicationException {
    final String target = env.getProperty(ORDER_VISIBILITY_OUTBOUND);
    try {
      final Optional<String> outMessage =
          XMLUtil.marshell(
              OrderLog.class,
              new com.scoperetail.oms.schema.ObjectFactory().createOrderLog(orderLog));
      XMLUtil.isValidMessage(outMessage, ofNullable(orderVisibilityOutboundSchema));
      contriveManager.send(target, outMessage.get());
    } catch (final JAXBException je) {
      log.error("Exception occured while marshelling the order log object: {}", je);
      throw new ApplicationException("Failed to marshel the order log object.", je);
    } catch (final ContriveException ce) {
      log.error(
          "Exception occured while sending message to order visibility endpoint :{} with error {}",
          target,
          ce);
      throw new ApplicationException(
          "Problem in sending message to order visibility endpoint:{} " + target, ce);
    } catch (final Exception e) {
      log.error("Exception occured in order visibility outbound handler: {}", e);
      throw new ApplicationException("Exception occured in order visibility outbound handler", e);
    }
  }
}
