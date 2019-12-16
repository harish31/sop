package com.scoperetail.supplier.order.processor.query.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RejectedLinesSearchResponse {
  private Integer orderId;
  private Long customerId;
  private Long productId;
  private String prodDesc;
  private String customerName;
  private Long supplierId;
  private Integer errorCode;
  private String errorDesc;
}
