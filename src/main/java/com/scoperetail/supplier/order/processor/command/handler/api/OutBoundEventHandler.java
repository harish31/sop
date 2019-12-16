package com.scoperetail.supplier.order.processor.command.handler.api;

import com.scoperetail.commons.ApplicationException;

public interface OutBoundEventHandler<E> {
  void send(E event) throws ApplicationException;
}
