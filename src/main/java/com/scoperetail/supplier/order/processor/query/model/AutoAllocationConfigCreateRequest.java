package com.scoperetail.supplier.order.processor.query.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.scoperetail.supplier.order.processor.command.validator.AutoAllocationConfigValid;

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
@AutoAllocationConfigValid
public class AutoAllocationConfigCreateRequest {
  @NotNull(message = "Supplier Id is mandatory!")
  private Long supplierId;

  private Short cutoffOffset;

  private Short releaseOffset;

  private Short totalDuration;
}
