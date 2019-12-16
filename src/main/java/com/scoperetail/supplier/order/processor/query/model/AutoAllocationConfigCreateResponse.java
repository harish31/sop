package com.scoperetail.supplier.order.processor.query.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.scoperetail.commons.response.BaseResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoAllocationConfigCreateResponse extends BaseResponse {
  private static final long serialVersionUID = 1L;
  private Integer configId;
}
