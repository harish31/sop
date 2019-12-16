package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scoperetail.common.rest.client.model.sop.ScheduleInfo;
import com.scoperetail.order.persistence.entity.OrderCustomer;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.entity.SupplierOrderEnriched;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.query.handler.SchedulerInfoQueryHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SchedulerInfoQueryHandlerImpl implements SchedulerInfoQueryHandler {

  @Autowired private SupplierOrderRepository supplierOrderRepository;

  @Override
  public ScheduleInfo getSchedulerInfo(final Integer orderId) {
    log.info("Retrieving Supplier Order : " + orderId);
    final Optional<SupplierOrder> optionalSupplierOrder = supplierOrderRepository.findById(orderId);
    ScheduleInfo scheduleInfo = null;
    if (optionalSupplierOrder.isPresent()) {
      final SupplierOrder supplierOrder = optionalSupplierOrder.get();
      final SupplierOrderEnriched orderEnriched = supplierOrder.getSupplierOrderEnriched();
      final OrderCustomer orderCustomer = supplierOrder.getOrderCustomer();
      scheduleInfo =
          ScheduleInfo.builder()
              .customerId(orderCustomer.getCustomerId())
              .divisionId(orderCustomer.getDivisionId())
              .orderStatusId(supplierOrder.getOrderStatusId())
              .scheduledReleaseDate(
                  orderEnriched.getScheduledReleaseDate() == null
                      ? null
                      : Date.from(
                          orderEnriched
                              .getScheduledReleaseDate()
                              .atStartOfDay(ZoneId.systemDefault())
                              .toInstant()))
              .orderType(String.valueOf(supplierOrder.getOrderTypeId()))
              .supplierId(supplierOrder.getSupplierId())
              .scheduledReleaseStartTime(orderEnriched.getScheduledReleaseStartTime())
              .scheduledReleaseEndTime(orderEnriched.getScheduledReleaseEndTime())
              .build();
    }
    return scheduleInfo;
  }
}
