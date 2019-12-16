package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.commons.constants.Constants.CORP_ID;
import static com.scoperetail.commons.enums.OrderLineStatus.getLineStatus;
import static com.scoperetail.commons.enums.OrderStatus.getOrderStatus;
import static com.scoperetail.commons.util.NumberUtil.convertToBigInteger;
import static com.scoperetail.commons.util.NumberUtil.convertToBigIntegerFromString;
import static com.scoperetail.commons.util.XMLUtil.isValidMessage;
import static com.scoperetail.commons.util.XMLUtil.marshell;
import static com.scoperetail.oms.schema.OrderStatusCodes.fromValue;
import static com.scoperetail.order.persistence.entity.OrderSchedStatus.getOrderSchedStatus;
import static com.scoperetail.order.persistence.entity.UnitOfMeasure.getUnitOfMeasure;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.DEFAULT_SUPPLIER_PRODUCT_ID;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.common.rest.client.exception.manager.response.OrderErrorLog;
import com.scoperetail.common.rest.client.exception.manager.response.OrderLogResponse;
import com.scoperetail.common.rest.client.model.ContentDto;
import com.scoperetail.common.rest.client.model.audit.AuditResponse;
import com.scoperetail.common.rest.client.model.audit.AuditorResponseDto;
import com.scoperetail.common.rest.client.model.audit.OrderChangeLogDto;
import com.scoperetail.common.rest.client.model.audit.OrderLineChangeLogDto;
import com.scoperetail.common.rest.client.service.api.AuditorService;
import com.scoperetail.common.rest.client.service.api.OrderErrorLogService;
import com.scoperetail.commons.enums.Attribute;
import com.scoperetail.commons.enums.ChangeCategory;
import com.scoperetail.commons.enums.CustomerType;
import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.enums.SubstitutionType;
import com.scoperetail.commons.util.DateUtil;
import com.scoperetail.commons.util.NumberUtil;
import com.scoperetail.oms.schema.ErrorLog;
import com.scoperetail.oms.schema.ErrorLogs;
import com.scoperetail.oms.schema.LineChangeLog;
import com.scoperetail.oms.schema.LineChangeLogs;
import com.scoperetail.oms.schema.OrderChangeLog;
import com.scoperetail.oms.schema.OrderChangeLogs;
import com.scoperetail.oms.schema.OrderLog;
import com.scoperetail.oms.schema.OrderLogCustomer;
import com.scoperetail.oms.schema.OrderLogHeader;
import com.scoperetail.oms.schema.OrderLogLine;
import com.scoperetail.oms.schema.OrderLogLines;
import com.scoperetail.oms.schema.OrderLogSubstitutions;
import com.scoperetail.oms.schema.OrderLogSupplier;
import com.scoperetail.oms.schema.OrderLogTotalType;
import com.scoperetail.oms.schema.OrderSchedStatusCodes;
import com.scoperetail.oms.schema.OrderStatusCodes;
import com.scoperetail.oms.schema.OrderTypeCodes;
import com.scoperetail.oms.schema.Substitution;
import com.scoperetail.oms.schema.SupplierType;
import com.scoperetail.oms.schema.UomCodeQty;
import com.scoperetail.order.persistence.entity.OrderCustomer;
import com.scoperetail.order.persistence.entity.OrderLine;
import com.scoperetail.order.persistence.entity.OrderLineEnriched;
import com.scoperetail.order.persistence.entity.OrderLineSubstitution;
import com.scoperetail.order.persistence.entity.OrderType;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.entity.SupplierOrderEnriched;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.command.handler.api.OrderVisibilityLogCmdHandler;
import com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants;

import lombok.extern.slf4j.Slf4j;

/** TODO - remove all boilerplate code and use MapStruct framework for object to object mapping */
@Slf4j
@Service
public class OrderVisibilityLogCmdHandlerImpl implements OrderVisibilityLogCmdHandler {

  @Autowired private SupplierOrderRepository supplierOrderRepository;
  @Autowired private AuditorService auditorService;
  @Autowired private OrderErrorLogService orderErrorLogService;
  @Autowired private Schema orderVisibilityOutboundSchema;

  @Value("${order.visibility.xsd.validation.enable:true}")
  private boolean isXsdValidationEnabled;

  @Value("#{'${error.severities}'.split(',')}")
  private List<String> severities;

  @Value("${change.category:ORD_VIS}")
  private ChangeCategory changeCategory;

  @Value("${order.visibility.order.statuses}")
  private List<OrderStatus> orderVisibilityStatuses;

  @Value("${order.visibility.order.line.statuses}")
  private List<OrderLineStatus> orderVisibilityLineStatuses;

  @Value("${order.visibility.max.description.length:100}")
  private Integer maxDescriptionLength;

  private static final String ZERO = "0";

  @Override
  public OrderLog createOutboundOrderVisibilityLog(final Integer orderId) {
    log.info("Create outbound message for order visibility order log.");
    final Optional<SupplierOrder> supplierOrder =
        supplierOrderRepository.findOrderVisibilitySupplierOrder(orderId);
    final SupplierOrder order = supplierOrder.orElseThrow(NoResultException::new);
    OrderLog orderLog = null;
    if (com.scoperetail.commons.enums.OrderType.TRANSFER.getCode() != order.getOrderTypeId()
        && orderVisibilityStatuses.contains(getOrderStatus(order.getOrderStatusId()))) {
      log.info("Order is eligible for order log");
      // eager loading supplier order details
      loadOrderDetails(order);
      orderLog = buildOutboundOrderVisibilityLog(order);
      // Optional validation against XSD.
      validateAgainstXsd(orderLog);
    } else {
      log.info("Order id {} is not eligible for order log outbound.", orderId);
    }
    log.trace("Order visibility outbound message created = {}", orderLog);
    return orderLog;
  }

  private void validateAgainstXsd(final OrderLog orderLog) {
    if (isXsdValidationEnabled) {
      try {
        final Optional<String> outMessage =
            marshell(
                OrderLog.class,
                new com.scoperetail.oms.schema.ObjectFactory().createOrderLog(orderLog));
        log.debug("Generated Order log Outbound Message: {}", outMessage);
        isValidMessage(ofNullable(outMessage.get()), ofNullable(orderVisibilityOutboundSchema));
      } catch (final JAXBException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private OrderLog buildOutboundOrderVisibilityLog(final SupplierOrder order) {
    final AuditorResponseDto auditorResponse = getAuditorChangeLogs(order.getOrderId());
    final OrderLog orderLog = new OrderLog();
    orderLog.setOrderHeader(getOrderHeader(order, auditorResponse));
    orderLog.setSupplier(getOrderLogSupplier(order));
    orderLog.setLines(getOrderLogLines(order, auditorResponse));
    orderLog.setCustomer(getCustomer(order));
    orderLog.setErrorLog(getErrorLogs(order));
    return orderLog;
  }

  private AuditorResponseDto getAuditorChangeLogs(final Integer orderId) {
    AuditorResponseDto auditorResponse = null;
    final Optional<AuditResponse> auditResponseOpt =
        auditorService.getAuditor(orderId, changeCategory);
    if (auditResponseOpt.isPresent()) {
      auditorResponse =
          auditResponseOpt.map(AuditResponse::getPage).map(ContentDto::getContent).get().get(0);
    }
    return auditorResponse;
  }

  private ErrorLogs getErrorLogs(final SupplierOrder order) {
    List<OrderErrorLog> orderErrorLogs = null;
    final Optional<OrderLogResponse> errorLogResOpt =
        orderErrorLogService.getOrderErrorLog(
            order.getOrderId(), order.getCustOrderId(), severities);

    if (errorLogResOpt.isPresent()) {
      final Optional<List<OrderErrorLog>> optionalErrorLogMap =
          errorLogResOpt.map(OrderLogResponse::getPage).map(ContentDto::getContent);
      if (optionalErrorLogMap.isPresent()) {

        Map<Long, Integer> origProdIdLineMap = getOriginalProdIdLineMap(order);

        final List<String> productIds =
            order
                .getOrderLines()
                .stream()
                .map(ol -> ol.getProductId().toString())
                .collect(Collectors.toList());
        orderErrorLogs = optionalErrorLogMap.get();

        orderErrorLogs =
            orderErrorLogs
                .stream()
                .filter(
                    oel ->
                        oel.getProductId() == null
                            || ZERO.equals(oel.getProductId())
                            || productIds.contains(oel.getProductId()))
                .collect(Collectors.toList());

        // set SOP Line number to errors generated at COP level
        orderErrorLogs
            .stream()
            .forEach(
                orderLog -> {
                  if ((orderLog.getLineNbr() == null
                          || Integer.valueOf(orderLog.getLineNbr()).equals(0))
                      && Optional.ofNullable(orderLog.getProductId()).isPresent()
                      && Optional.ofNullable(
                              origProdIdLineMap.get(Long.valueOf(orderLog.getProductId())))
                          .isPresent()) {
                    orderLog.setLineNbr(
                        String.valueOf(
                            origProdIdLineMap.get(Long.valueOf(orderLog.getProductId()))));
                  }
                });
      }
    }

    final ErrorLogs errorLogs = new ErrorLogs();
    if (isNotEmpty(orderErrorLogs)) {
      orderErrorLogs.forEach(
          errLog -> {
            final ErrorLog errorLog = new ErrorLog();
            final Attribute att = Attribute.getAttribute(parseToInteger(errLog.getAttributeId()));
            errorLog.setAttribName(getSubString(att.getLabel()));
            errorLog.setSourceTime(DateUtil.toLocalDateTime(errLog.getSourceTs()));
            errorLog.setCreateTime(DateUtil.toLocalDateTime(errLog.getCreateTs()));
            errorLog.setErrorCode(convertToBigIntegerFromString(errLog.getErrorCode()));
            errorLog.setErrorDesc(getShortDesc(errLog.getShortDesc(), errLog.getErrorDesc()));
            errorLog.setLineNumber(
                Integer.valueOf(errLog.getLineNbr()) > 0
                    ? Long.valueOf(errLog.getLineNbr())
                    : null);
            errorLog.setProductDesc(errLog.getProductDesc());
            ofNullable(errLog.getProductId())
                .map(NumberUtil::convertToBigIntegerFromString)
                .ifPresent(errorLog::setProductId);
            errorLog.setUserId(errLog.getUserId());
            errorLogs.getErrorLog().add(errorLog);
          });
    }
    return errorLogs;
  }

  private Map<Long, Integer> getOriginalProdIdLineMap(SupplierOrder order) {
    return order
        .getOrderLines()
        .stream()
        .collect(
            Collectors.toMap(
                OrderLine::getOrigProductId, OrderLine::getLineNbr, (Entry1, Entry2) -> Entry1));
  }

  private OrderLogHeader getOrderHeader(
      final SupplierOrder order, final AuditorResponseDto auditorResponse) {
    final SupplierOrderEnriched orderEnriched = order.getSupplierOrderEnriched();
    final OrderLogHeader orderLogHeader = new OrderLogHeader();
    orderLogHeader.setOrderId(String.valueOf(order.getOrderId()));
    orderLogHeader.setOrderTotal(getOrderLogTotalType(orderEnriched));
    orderLogHeader.setOrderTypeCode(getTypeCodes(order.getOrderTypeId()));
    orderLogHeader.setPoRefNbr(orderEnriched.getPoRefNbr());
    orderLogHeader.setReleasedTime(order.getSysOrdReleaseTs());
    orderLogHeader.setSchedDeliveryDate(orderEnriched.getScheduledDeliveryDate());
    ofNullable(orderEnriched.getScheduledProcessDate())
        .map(localDate -> localDate.atTime(LocalTime.now()))
        .ifPresent(schedProcessDate -> orderLogHeader.setSchedProcessDate(schedProcessDate));
    orderLogHeader.setSchedReleaseStartTime(orderEnriched.getScheduledReleaseStartTime());
    orderLogHeader.setSchedReleaseEndTime(orderEnriched.getScheduledReleaseEndTime());

    ofNullable(order.getOrderSchedStatusId())
        .ifPresent(
            schedStatusId -> {
              orderLogHeader.setSchedStatusCode(
                  OrderSchedStatusCodes.fromValue(getOrderSchedStatus(schedStatusId).name()));
            });
    orderLogHeader.setDataBatch(orderEnriched.getSrcSuppId());
    orderLogHeader.setSrcOrderCreateTimestamp(
        ofNullable(orderEnriched.getSrcOrderCreateTs()).orElse(order.getCreateTs()));
    orderLogHeader.setSrcOrderId(orderEnriched.getSrcOrderId());
    orderLogHeader.setSrcOrderId2(orderEnriched.getSrcOrderId2());
    orderLogHeader.setStatusCode(getStatusCodes(order.getOrderStatusId()));

    if (nonNull(auditorResponse) && isNotEmpty(auditorResponse.getOrderChangeLogs())) {
      orderLogHeader.setOrderChangeLogs(getOrderChangeLogs(auditorResponse.getOrderChangeLogs()));
    }
    return orderLogHeader;
  }

  private OrderChangeLogs getOrderChangeLogs(final List<OrderChangeLogDto> orderChanges) {
    final OrderChangeLogs orderChangeLogs = new OrderChangeLogs();
    orderChanges.forEach(
        orderChange -> {
          final OrderChangeLog orderChangeLog = new OrderChangeLog();
          orderChangeLog.setChangeReasonDesc(
              getShortDesc(orderChange.getShortDesc(), orderChange.getChangeReason()));
          orderChangeLog.setChangeReasonId(parseToInteger(orderChange.getChangeReasonId()));
          orderChangeLog.setRouteId(parseToInteger(orderChange.getRouteId()));
          orderChangeLog.setSchedDeliveryDate(orderChange.getScheduledDeliveryDate());
          orderChangeLog.setSchedProcessDate(orderChange.getScheduledProcessDate());
          orderChangeLog.setSourceTime(DateUtil.toLocalDateTime(orderChange.getSourceTs()));
          orderChangeLog.setCreateTime(DateUtil.toLocalDateTime(orderChange.getCreateTs()));
          orderChangeLog.setStatusCode(
              nonNull(orderChange.getOrderStatusCode())
                  ? fromValue(getOrderStatus(orderChange.getOrderStatusCode()).name())
                  : null);
          orderChangeLog.setUserId(orderChange.getUserId());
          orderChangeLogs.getOrderChangeLog().add(orderChangeLog);
        });
    return orderChangeLogs;
  }

  private OrderTypeCodes getTypeCodes(final Integer orderTypeId) {
    return OrderTypeCodes.fromValue(OrderType.getOrderType(orderTypeId).name());
  }

  private OrderStatusCodes getStatusCodes(final Integer orderStatusId) {
    return fromValue(getOrderStatus(orderStatusId).name());
  }

  private OrderLogTotalType getOrderLogTotalType(final SupplierOrderEnriched orderEnriched) {
    final OrderLogTotalType orderTotalType = new OrderLogTotalType();
    final Integer lineCount = orderEnriched.getSupplierOrder().getOrderLines().size();
    orderTotalType.setLineCount(lineCount);
    orderTotalType.setOriginalLineCount(ofNullable(orderEnriched.getOrigSuppLineCount()).orElse(0));
    orderTotalType.setTotalItemQuantity(convertToBigInteger(orderEnriched.getTotalItemQty()));
    orderTotalType.setOriginalTotalItemQuantity(
        convertToBigInteger(orderEnriched.getTotalOrderedItemQty()));
    return orderTotalType;
  }

  private OrderLogLines getOrderLogLines(
      final SupplierOrder order, final AuditorResponseDto auditorResponse) {
    final Collection<OrderLine> orderLines = order.getOrderLines();
    final OrderLogLines outboundOrderLines = new OrderLogLines();
    final Set<Integer> orderLineLogLineNbrs = new HashSet<>();
    for (final OrderLine orderLine : orderLines) {
      if (orderVisibilityLineStatuses.contains(getLineStatus(orderLine.getOrderLineStatusId()))) {
        final OrderLineEnriched orderLineEnriched = orderLine.getOrderLineEnriched();
        final OrderLogLine orderLogLine = new OrderLogLine();
        orderLogLine.setLineNumber(orderLine.getLineNbr());
        orderLogLine.setLineStatusCode(getLineStatus(orderLine.getOrderLineStatusId()).name());
        orderLogLine.setAdjItemQuantity(BigDecimal.valueOf(orderLineEnriched.getCurrentItemQty()));
        orderLogLine.setSupplierProductId(
            ofNullable(StringUtils.stripToNull(orderLineEnriched.getSupplierProductId()))
                .orElse(DEFAULT_SUPPLIER_PRODUCT_ID));
        orderLogLine.setOrderedItemQuantity(convertToBigInteger(orderLine.getOrderedItemQty()));

        if (nonNull(orderLineEnriched.getSupplierUom())) {
          orderLogLine.setOrderedItemQuantityUom(
              UomCodeQty.fromValue(getUnitOfMeasure(orderLineEnriched.getSupplierUom()).name()));
        }
        orderLogLine.setOrderedItemWeight(orderLine.getOrderedItemWgt());
        // TODO find mapping value.
        // orderLogLine.setOrderedItemWeightUom(UomCodeWgt.fromValue(orderLine.getOrderedUom()));
        if (orderLineEnriched.getSubstitutionType() != null) {
          SubstitutionType substitutionType =
              SubstitutionType.getSubstitutionTypeByCode(orderLineEnriched.getSubstitutionType());
          orderLogLine.setSubType(
              com.scoperetail.oms.schema.SubstitutionType.fromValue(substitutionType.getLabel()));
        }
        orderLogLine.setSubstitutions(getSubstitutions(orderLine));
        orderLogLine.setOrigProductId(convertToBigInteger(orderLine.getOrigProductId()));
        orderLogLine.setPalletQuantity(orderLineEnriched.getPalletQty());
        orderLogLine.setProductId(convertToBigInteger(orderLine.getProductId()));
        orderLogLine.setExpiredProductId(nullSafeBigInteger(orderLine.getExpiredProductId()));
        orderLogLine.setRetailSectionCd(orderLineEnriched.getRetailSectionCode());
        orderLogLine.setRetailUpc(orderLineEnriched.getRetailUpc());
        orderLogLine.setSupplierUpc(
            ofNullable(StringUtils.stripToNull(orderLineEnriched.getSupplierUpc())).orElse("NULL"));
        orderLineLogLineNbrs.add(orderLine.getLineNbr());
        outboundOrderLines.getLine().add(orderLogLine);
      }
    }

    if (nonNull(auditorResponse) && isNotEmpty(auditorResponse.getOrderLineChangeLogs())) {
      outboundOrderLines.setLineChangeLogs(
          getOrderLineChangeLogs(auditorResponse.getOrderLineChangeLogs(), orderLineLogLineNbrs));
    }
    return outboundOrderLines;
  }

  private OrderLogSubstitutions getSubstitutions(final OrderLine orderLine) {
    final List<OrderLineSubstitution> byOrderIdAndLineNbr =
        orderLine.getOrderLineSubstitutions().stream().collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(byOrderIdAndLineNbr)) {
      final OrderLogSubstitutions substitutions = new OrderLogSubstitutions();
      byOrderIdAndLineNbr.forEach(
          lineSubstitution -> {
            final Substitution substitution = new Substitution();
            substitution.setSupplierProductId(
                ofNullable(StringUtils.stripToNull(lineSubstitution.getSupplierProductId()))
                    .orElse(DEFAULT_SUPPLIER_PRODUCT_ID));
            substitution.setProductId(nullSafeBigInteger(lineSubstitution.getProductId()));
            substitution.setFactor(lineSubstitution.getSubsFactor());
            substitution.setSubsOrderedQuantity(
                nullSafeBigInteger(lineSubstitution.getSubsOrderedQty()));
            substitution.setType(String.valueOf(lineSubstitution.getSubsTypeId()));
            substitution.setRank(lineSubstitution.getRank());
            substitution.setRetailUpc(lineSubstitution.getRetailUpc());
            substitutions.getSubstitution().add(substitution);
          });
      return substitutions;
    }
    return null;
  }

  private LineChangeLogs getOrderLineChangeLogs(
      final List<OrderLineChangeLogDto> orderLineChanges, final Set<Integer> orderLineLogLineNbrs) {
    final LineChangeLogs lineChangeLogs = new LineChangeLogs();
    orderLineChanges.forEach(
        lineChange -> {
          final Integer lineNbr = parseToInteger(lineChange.getLineNbr());
          if (orderLineLogLineNbrs.contains(lineNbr)) {
            final LineChangeLog lineChangeLog = new LineChangeLog();
            lineChangeLog.setChangeReasonId(parseToInteger(lineChange.getChangeReasonId()));
            lineChangeLog.setChangeReasonDesc(
                getShortDesc(lineChange.getShortDesc(), lineChange.getChangeReason()));
            lineChangeLog.setLineNumber(lineNbr);
            lineChangeLog.setLineStatusCode(
                nonNull(lineChange.getLineStatusCode())
                    ? getLineStatus(lineChange.getLineStatusCode()).name()
                    : "NULL");
            lineChangeLog.setPreviousProductQty(parseToInteger(lineChange.getOrderedItemQty()));
            lineChangeLog.setPreviousProductId(
                convertToBigIntegerFromString(lineChange.getOriginalProductId()));
            lineChangeLog.setProductId(convertToBigIntegerFromString(lineChange.getProductId()));
            lineChangeLog.setProductQty(parseToInteger(lineChange.getAdjItemQty()));
            lineChangeLog.setSourceTime(DateUtil.toLocalDateTime(lineChange.getSourceTs()));
            lineChangeLog.setCreateTime(DateUtil.toLocalDateTime(lineChange.getCreateTs()));
            lineChangeLogs.getLineChangeLog().add(lineChangeLog);
          }
        });
    return lineChangeLogs;
  }

  private OrderLogSupplier getOrderLogSupplier(final SupplierOrder order) {
    final OrderLogSupplier orderLogSupplier = new OrderLogSupplier();
    orderLogSupplier.setSupplierId(convertToBigInteger(order.getSupplierId()));
    final com.scoperetail.commons.enums.SupplierType supplierType =
        com.scoperetail.commons.enums.SupplierType.getSupplierType(order.getSupplierTypeId());
    orderLogSupplier.setSupplierType(SupplierType.fromValue(supplierType.name()));
    orderLogSupplier.setDistCenterId(
        ofNullable(StringUtils.stripToNull(order.getDistCenterId())).orElse("NULL"));
    orderLogSupplier.setPhysWhseId(order.getPhysWhseId());
    orderLogSupplier.setDivisionId(order.getSuppDivisionId());
    orderLogSupplier.setCorpId(CORP_ID);
    return orderLogSupplier;
  }

  private OrderLogCustomer getCustomer(final SupplierOrder order) {
    final OrderCustomer customer = order.getOrderCustomer();
    final OrderLogCustomer orderLogCustomer = new OrderLogCustomer();
    orderLogCustomer.setCorpId(CORP_ID);
    orderLogCustomer.setDivisionId(customer.getDivisionId());
    orderLogCustomer.setCustomerId(convertToBigInteger(customer.getCustomerId()));
    orderLogCustomer.setCustomerAccountId(customer.getCustomerAccId());
    final CustomerType customerType = CustomerType.getCustomerType(customer.getCustTypeId());
    orderLogCustomer.setCustomerType(
        com.scoperetail.oms.schema.CustomerType.fromValue(customerType.name()));
    return orderLogCustomer;
  }

  private void loadOrderDetails(final SupplierOrder order) {
    order.getSupplierOrderEnriched();
    order.getOrderLines();
    order.getOrderCustomer();
  }

  private Integer parseToInteger(final String value) {
    return value == null ? null : Integer.valueOf(value);
  }

  private String getShortDesc(final String shortDesc, final String longDesc) {
    return StringUtils.isNotEmpty(shortDesc) ? shortDesc : getSubString(longDesc);
  }

  private String getSubString(final String longDesc) {
    return StringUtils.substring(longDesc, 0, maxDescriptionLength);
  }

  private BigInteger nullSafeBigInteger(final Integer integer) {
    return integer == null ? null : BigInteger.valueOf(integer);
  }

  private BigInteger nullSafeBigInteger(final Long longValue) {
    return longValue == null ? null : BigInteger.valueOf(longValue);
  }
}
