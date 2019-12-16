package com.scoperetail.order.processor.amq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  @Autowired
  private JmsTemplate jmsTemplate;

  public void send(final String destination, final String message) {
    jmsTemplate.convertAndSend(destination, message);
  }
}
