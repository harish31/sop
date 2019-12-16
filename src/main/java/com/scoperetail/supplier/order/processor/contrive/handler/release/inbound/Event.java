package com.scoperetail.supplier.order.processor.contrive.handler.release.inbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
  private String payload;
  private String eventName;
  private Integer orderId;
}
