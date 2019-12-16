package com.scoperetail.order.processor.amq;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

//@Configuration
//@EnableJms
public class ReceiverConfig {

  @Value("${spring.activemq.broker-url}")
  private String brokerUrl;

  @Bean
  public ActiveMQConnectionFactory activeMQConnectionFactory() {
    final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    activeMQConnectionFactory.setBrokerURL(brokerUrl);

    return activeMQConnectionFactory;
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
    final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(activeMQConnectionFactory());
    factory.setConcurrency("3-10");

    return factory;
  }

  @Bean
  public Receiver receiver() {
    return new Receiver();
  }
}
