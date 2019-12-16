package com.scoperetail.supplier.order.processor.command.handler.impl;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.util.XMLUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.internal.schema.Event;
import com.scoperetail.internal.schema.EventName;
import com.scoperetail.internal.schema.ObjectFactory;
import com.scoperetail.oms.schema.OrderLog;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.TERMINATOR_TARGET;
import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class OrderVisibilityOutboundTerminatorHandler implements OutBoundEventHandler<OrderLog> {
  @Autowired private Environment env;
  @Autowired private ContriveManager contriveManager;
  @Autowired private Schema orderVisibilityOutboundSchema;

  @Override
  public void send(final OrderLog orderLog) throws ApplicationException {
    final String target = env.getProperty(TERMINATOR_TARGET);
    try {
      final Optional<String> outMessage =
              XMLUtil.marshall(
                      Event.class,
                      new ObjectFactory().createEvent(createEvent(orderLog)));
      outMessage.ifPresent(
              message -> {
                log.info("OrderVisibilityOutboundTerminatorHandler:: Message :: {}", message);
                try {
                  contriveManager.send(target, outMessage.get());
                } catch (final ContriveException ce) {
                  log.error("Event {} and target {}", orderLog, target);
                  log.error("Exception occured while sending message to order visibility Terminator  endpoint :{} with error {}",
                          target,
                          ce);
                  throw new ApplicationException("Problem in sending message to order visibility endpoint:{} " + target);
                }
              });
    } catch (final JAXBException e) {
      log.error("Exception occured in order visibility outbound handler: {}", e);
      throw new ApplicationException("Exception occured in order visibility outbound handler.");
    }
  }

  private Event createEvent(final OrderLog orderLog) throws JAXBException {
    final Event event = new Event();
    event.setEventName(EventName.ORDER_VISIBILITY);
    event.setOrderId(orderLog.getOrderHeader().getOrderId());
    final Optional<String> outMessage =
            XMLUtil.marshell(
                    OrderLog.class,
                    new com.scoperetail.oms.schema.ObjectFactory()
                            .createOrderLog(orderLog));
    final String payload = outMessage.orElseThrow(NoSuchElementException::new);
    event.setPayload("<![CDATA[" + payload + "]]>");
    return event;
  }
}
