package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.order.persistence.entity.OrderType;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.supplier.order.processor.query.handler.SupplierOrderCsvHandler;
import com.scoperetail.supplier.order.processor.query.handler.SupplierOrderQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.SearchOrderResponseCsv;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SupplierOrderCsvHandlerImpl implements SupplierOrderCsvHandler {

  private static final String HYPHEN = "-";

  @Autowired private SupplierOrderQueryHandler supplierOrderQueryHandler;

  @Override
  public Page<SearchOrderResponseCsv> getSearchOrderResponseCsv(
      final SupplierOrderSearchRequest supplierOrderSearchRequest, final Pageable pageable) {
    Page<SupplierOrder> supplierOrderPage =
        supplierOrderQueryHandler.searchSupplierOrders(supplierOrderSearchRequest, pageable);

    if (supplierOrderPage == null) {
      return null;
    }

    final Page<SearchOrderResponseCsv> searchOrderResponseCsv =
        supplierOrderPage.map(
            supplierOrder ->
                SearchOrderResponseCsv.builder()
                    .orderId(supplierOrder.getOrderId())
                    .supplier(
                        +supplierOrder.getSupplierId() + HYPHEN + supplierOrder.getSupplierName())
                    .supplierId(supplierOrder.getSupplierId())
                    .orderType(OrderType.getOrderType(supplierOrder.getOrderTypeId()).getLabel())
                    .status(OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId()).getLabel())
                    .customerId(supplierOrder.getOrderCustomer().getCustomerId())
                    .customer(
                        supplierOrder.getOrderCustomer().getCustomerId()
                            + HYPHEN
                            + supplierOrder.getOrderCustomer().getCustomerName())
                    .customerGroupId(supplierOrder.getOrderCustomer().getCogId())
                    .customerGroup(supplierOrder.getOrderCustomer().getCogLabel())
                    .divisionId(supplierOrder.getOrderCustomer().getDivisionId())
                    .deliveryDate(
                        Objects.toString(
                            supplierOrder.getSupplierOrderEnriched().getScheduledDeliveryDate(),
                            ""))
                    .processDate(
                        Objects.toString(
                            supplierOrder.getSupplierOrderEnriched().getScheduledProcessDate(), ""))
                    .createdOn(Objects.toString(supplierOrder.getCreateTs().toLocalDate(), ""))
                    .totalCaseQuantity(
                        Objects.toString(
                            supplierOrder.getSupplierOrderEnriched().getTotalCaseQuantity(), ""))
                    .build());
    log.info("Found : " + searchOrderResponseCsv.getContent().size() + " matching supplier orders");
    return searchOrderResponseCsv;
  }
}
