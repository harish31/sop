package com.scoperetail.order.processor.amq;

import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveMqHelper {

  public void removeAllMessages() {
    log.debug("Test: removing messages from the configured queue");
    final Map<String, BrokerService> brokers = BrokerRegistry.getInstance().getBrokers();
    try {
      for (final BrokerService brokerService : brokers.values()) {
        final Broker broker = brokerService.getBroker();
        new ActiveMQBrokerExtension(broker).clearAllMessages();
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    log.debug("Test: removing messages from the configured queue completed");
  }
}
