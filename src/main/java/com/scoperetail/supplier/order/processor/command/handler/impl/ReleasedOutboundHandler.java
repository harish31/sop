package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.TERMINATOR_TARGET;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.util.XMLUtil;
import com.scoperetail.contrive.common.ContriveException;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.internal.schema.Event;
import com.scoperetail.internal.schema.EventName;
import com.scoperetail.internal.schema.ObjectFactory;
import com.scoperetail.oms.schema.OutboundCustomerOrder;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;

import lombok.extern.slf4j.Slf4j;

@Component("releasedOutboundHandler")
@Slf4j
public class ReleasedOutboundHandler implements OutBoundEventHandler<OutboundCustomerOrder> {
  private @Autowired Environment env;

  @Autowired private ContriveManager contriveManager;

  @Override
  public void send(final OutboundCustomerOrder customerOrder) throws ApplicationException {
    final String target = env.getProperty(TERMINATOR_TARGET);
    try {
      final Optional<String> outMessage =
          XMLUtil.marshall(
              Event.class, new ObjectFactory().createEvent(createEvent(customerOrder)));
      outMessage.ifPresent(
          message -> {
            log.info("ReleasedOutboundHandler:: Message :: {}", message);
            try {
              contriveManager.send(target, message);
            } catch (final ContriveException e) {
              log.error("Event {} and target {}", customerOrder, target);
              log.error("Problem in sending message to out queue: {}", e);
              throw new ApplicationException("Problem in sending message to out queue");
            }
          });
    } catch (final JAXBException e) {
      log.error("Exception: {}", e);
      throw new ApplicationException("Failed to parse outbound event message.");
    }
  }

  private Event createEvent(final OutboundCustomerOrder customerOrder) throws JAXBException {
    final Event event = new Event();
    event.setEventName(EventName.RELEASED);
    event.setOrderId(customerOrder.getOrderHeader().getOrderId());
    final Optional<String> outMessage =
        XMLUtil.marshell(
            OutboundCustomerOrder.class,
            new com.scoperetail.oms.schema.ObjectFactory()
                .createCustomerOrderOutbound(customerOrder));
    final String payload = outMessage.orElseThrow(NoSuchElementException::new);
    event.setPayload("<![CDATA[" + payload + "]]>");
    return event;
  }
}
