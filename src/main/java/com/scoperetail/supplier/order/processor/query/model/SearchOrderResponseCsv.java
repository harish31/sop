package com.scoperetail.supplier.order.processor.query.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@EqualsAndHashCode(of = {"orderId"})
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
  "divisionId",
  "customerGroupId",
  "customerGroup",
  "customerId",
  "customer",
  "supplierId",
  "supplier",
  "orderType",
  "orderId",
  "status",
  "createdOn",
  "processDate",
  "deliveryDate",
  "totalCaseQuantity"
})
public class SearchOrderResponseCsv {
  private String divisionId;
  private Integer customerGroupId;
  private String customerGroup;
  private Long customerId;
  private String customer;
  private Long supplierId;
  private String supplier;
  private String orderType;
  private Integer orderId;
  private String status;
  private String createdOn;
  private String processDate;
  private String deliveryDate;
  private String totalCaseQuantity;
}
