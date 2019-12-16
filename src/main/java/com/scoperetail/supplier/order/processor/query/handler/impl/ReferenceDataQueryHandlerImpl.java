package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.manage.order.dto.response.ChangeReasonResponse;
import com.scoperetail.commons.manage.order.dto.response.LineStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderStatusResponse;
import com.scoperetail.commons.manage.order.dto.response.OrderTypeResponse;
import com.scoperetail.order.persistence.entity.ChangeReason;
import com.scoperetail.order.persistence.entity.OrderType;
import com.scoperetail.order.persistence.repository.ChangeReasonRepository;
import com.scoperetail.supplier.order.processor.query.handler.ReferenceDataQueryHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReferenceDataQueryHandlerImpl implements ReferenceDataQueryHandler {

  @Autowired private ChangeReasonRepository changeReasonRepository;

  @Override
  public List<OrderTypeResponse> getAllOrderType() {
    log.info("Fetching order type reference data");
    final List<OrderTypeResponse> orderTypeResponses = new ArrayList<OrderTypeResponse>();
    Stream.of(OrderType.values())
        .forEach(
            orderType ->
                orderTypeResponses.add(
                    OrderTypeResponse.builder()
                        .orderTypeId(orderType.getCode())
                        .description(orderType.getLabel())
                        .build()));
    return orderTypeResponses;
  }

  @Override
  public List<LineStatusResponse> getAllLineStatus() {
    log.info("Fetching line status reference data");
    final List<LineStatusResponse> lineStatusResponses = new ArrayList<LineStatusResponse>();
    Stream.of(OrderLineStatus.values())
        .forEach(
            lineStatus ->
                lineStatusResponses.add(
                    LineStatusResponse.builder()
                        .lineStatusId(lineStatus.getCode())
                        .description(lineStatus.getLabel())
                        .build()));
    return lineStatusResponses;
  }

  @Override
  public List<OrderStatusResponse> getAllOrderStatus() {
    log.info("Fetching order status reference data");
    final List<OrderStatusResponse> orderStatusResponses = new ArrayList<OrderStatusResponse>();
    Stream.of(OrderStatus.values())
        .forEach(
            orderStatus ->
                orderStatusResponses.add(
                    OrderStatusResponse.builder()
                        .orderStatusId(orderStatus.getCode())
                        .description(orderStatus.getLabel())
                        .build()));
    return orderStatusResponses;
  }

  @Override
  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  public List<ChangeReasonResponse> getAllChangeReason() {
    log.info("Fetching change reason reference data");
    final List<ChangeReason> ChangeReasons = changeReasonRepository.findAll();
    final List<ChangeReasonResponse> changeReasonResponses = new ArrayList<>();
    ChangeReasons.forEach(
        changeReason ->
            changeReasonResponses.add(
                ChangeReasonResponse.builder()
                    .changeReasonId(changeReason.getChangeReasonId())
                    .description(changeReason.getDescription())
                    .build()));
    return changeReasonResponses;
  }
}
