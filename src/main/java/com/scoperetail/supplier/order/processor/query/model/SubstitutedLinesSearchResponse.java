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
public class SubstitutedLinesSearchResponse {
  private Integer orderId;
  private Long customerId;
  private Long supplierId;
  private String subsType;
  private Long originalProductId;
  private String productDesc;
  private Long subsProductId;
  private String subsProductDesc;
  private Integer orderedItemQty;
  private Integer subsOrderedQty;
}
