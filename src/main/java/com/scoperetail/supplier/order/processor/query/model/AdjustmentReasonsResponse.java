package com.scoperetail.supplier.order.processor.query.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"adjReasonId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdjustmentReasonsResponse {

  private Integer adjReasonId;

  private String adjReasonCd;

  private String adjDesc;
}
