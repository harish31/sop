package com.scoperetail.order.processor.amq;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

public class Receiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

  private CountDownLatch latch = new CountDownLatch(1);
  private String message;

  public CountDownLatch getLatch() {
    return latch;
  }

  @JmsListener(destination = "${parley.target[0].name}")
  public void receiveInvalidMessage(final String message) {
    LOGGER.info("received message='{}'", message);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
