package com.scoperetail.order.processor.amq;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Queue;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.broker.region.Topic;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.store.MessageStore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveMQBrokerExtension {
  private final Broker broker;

  public ActiveMQBrokerExtension(final Broker broker) {
    this.broker = broker;
  }

  public void clearAllMessages() throws Exception {
    final Map<ActiveMQDestination, Destination> destinationMap = broker.getDestinationMap();
    for (final Destination destination : destinationMap.values()) {
      final ActiveMQDestination activeMQDestination = destination.getActiveMQDestination();
      if (activeMQDestination.isTopic()) {
        clearAllMessages((Topic) destination);
      } else if (activeMQDestination.isQueue()) {
        clearAllMessages((Queue) destination);
      }
    }
  }

  private void clearAllMessages(final Topic topic) throws IOException {
    final List<Subscription> consumers = topic.getConsumers();
    for (final Subscription consumer : consumers) {
      final ConnectionContext consumerContext = consumer.getContext();
      final MessageStore messageStore = topic.getMessageStore();
      messageStore.removeAllMessages(consumerContext);
    }
  }

  private void clearAllMessages(final Queue queue) throws Exception {
    log.debug("Test: Purging messages from queue {}", queue.getName());
    queue.purge();
    log.debug("Test : Purging completed for {}", queue.getName());
  }
}
