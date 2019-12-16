package com.scoperetail.supplier.order.processor.query.model;

import java.math.BigDecimal;

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
public class ShippingPalletCubeConfigUpdateRequest {
  @NotNull(message = "Id is mandatory!")
  private Long id;

  @NotNull(message = "Shipping Pallet Cube value is mandatory!")
  private BigDecimal shippingPalletCube;
}
