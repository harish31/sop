package com.scoperetail.supplier.order.processor.query.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeafoodConfigUpdateRequest {
  @NotNull(message = "Config Id is mandatory!")
  private Integer configId;

  @NotNull(message = "Enabled flag is mandatory!")
  private Character enabled;
}