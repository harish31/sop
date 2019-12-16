package com.scoperetail.supplier.order.processor.audit;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.scoperetail.internal.schema.AuditOrder;
import com.scoperetail.internal.schema.AuditOrderLine;
import com.scoperetail.internal.schema.AuditOrderLines;
import com.scoperetail.internal.schema.AuditOrders;
import com.scoperetail.logger.client.auditor.spi.AuditorMessageClient;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component("sopAuditableAspect")
@Slf4j
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class AuditableAspect {

  @Autowired private AuditorMessageClient auditorMessageClient;

  @Value("${audit.queue.property.name}")
  private String auditOrderTarget;

  @Around("@annotation(com.scoperetail.supplier.order.processor.audit.Auditable)")
  public Object auditOrder(final ProceedingJoinPoint joinPoint) throws Throwable {
    log.debug(
        "Around advice: before join point execution: Method Name: {} | Args: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.asList(joinPoint.getArgs()));

    final AuditOrders auditOrders = new AuditOrders();
    final List<Object> args = new ArrayList<>();
    Collections.addAll(args, joinPoint.getArgs());
    args.remove(0); // remove first element which is null
    args.add(0, auditOrders); // add audit order

    final Object response = joinPoint.proceed(args.toArray());

    log.debug(
        "Around advice: after join point execution: Method Name: {} | Args: {}",
        joinPoint.getSignature().toShortString(),
        Arrays.asList(joinPoint.getArgs()));

    if (isEligibletoSend(auditOrders)) {
      auditorMessageClient.log(auditOrders, auditOrderTarget);
    }
    return response;
  }

  private boolean isEligibletoSend(final AuditOrders auditOrders) {
    boolean isEligible = false;
    final List<AuditOrder> auditOrderList = auditOrders.getAuditOrder();
    if (isNotEmpty(auditOrderList)) {
      final AuditOrder auditOrder = auditOrderList.get(0);
      isEligible = isValidAuditOrderLinesPresent(auditOrder) || isAuditOrderUpdated(auditOrder);
    }
    return isEligible;
  }

  private Boolean isValidAuditOrderLinesPresent(final AuditOrder auditOrder) {
    final AtomicBoolean isValidAuditOrderLinesPresent = new AtomicBoolean(false);
    Optional.ofNullable(auditOrder)
        .map(AuditOrder::getAuditOrderLines)
        .map(AuditOrderLines::getAuditOrderLine)
        .ifPresent(
            l -> {
              isValidAuditOrderLinesPresent.set(
                  isNotEmpty(l) && isRequiredDataExistForAuditOrderLine(l));
            });
    return isValidAuditOrderLinesPresent.get();
  }

  private boolean isAuditOrderUpdated(final AuditOrder auditOrder) {
    return (!(auditOrder.getStatusCode() == null
            && isEmpty(auditOrder.getSchedProcessDate())
            && isEmpty(auditOrder.getSchedDeliveryDate())
            && auditOrder.getRouteId() == null
            && auditOrder.getTotalItemQuantity() == null
            && auditOrder.getTotalItemWeight() == null
            && auditOrder.getTotalPalletQuantity() == null
            && auditOrder.getTotalCubeQuantity() == null)
        && auditOrder.getChangeReasonId() > 0);
  }

  private boolean isRequiredDataExistForAuditOrderLine(final List<AuditOrderLine> auditOrderLines) {
    return !auditOrderLines
        .stream()
        .anyMatch(
            auditOrderLine ->
                !((nonNull(auditOrderLine.getProductId())
                        || nonNull(auditOrderLine.getProductQty())
                        || nonNull(auditOrderLine.getLineStatusCode()))
                    && (auditOrderLine.getLineNumber() > 0
                        && auditOrderLine.getOriginalProductId().longValue() > 0L
                        && auditOrderLine.getChangeReasonId() > 0)));
  }
}
