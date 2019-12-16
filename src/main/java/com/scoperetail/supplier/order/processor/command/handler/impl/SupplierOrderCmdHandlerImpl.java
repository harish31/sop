package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.commons.constants.Constants.CHAR_Y;
import static com.scoperetail.commons.constants.Constants.COMMA;
import static com.scoperetail.commons.enums.Attribute.COG_ID;
import static com.scoperetail.commons.enums.Attribute.CUSTOMER_ID;
import static com.scoperetail.commons.enums.Attribute.CUSTOMER_ORDER_ID;
import static com.scoperetail.commons.enums.Attribute.CUSTOMER_STATE;
import static com.scoperetail.commons.enums.Attribute.CUSTOMER_TYPE;
import static com.scoperetail.commons.enums.Attribute.PRODUCT_ID;
import static com.scoperetail.commons.enums.Attribute.SUPPLIER_IDS;
import static com.scoperetail.commons.enums.ChangeReason.MERCHANDISER_INCREASED_QUANTITY;
import static com.scoperetail.commons.enums.EnrichmentStatus.IN_PROGRESS;
import static com.scoperetail.commons.enums.EnrichmentStatus.NOT_STARTED;
import static com.scoperetail.commons.enums.ErrorCode.CUSTOMER_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.CUSTOMER_TYPE_NOT_ALLOWED;
import static com.scoperetail.commons.enums.ErrorCode.GROUP_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.ORDER_LINE_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.ORDER_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.PRODUCT_SUPPLIER_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.SOS_NOT_FOUND;
import static com.scoperetail.commons.enums.ErrorCode.SUPPLIER_NOT_FOUND;
import static com.scoperetail.commons.enums.OlcmEventName.CANCEL;
import static com.scoperetail.commons.enums.OlcmEventName.CREATED;
import static com.scoperetail.commons.enums.OlcmEventName.ONHOLD;
import static com.scoperetail.commons.enums.OlcmEventName.READYTORELEASE_MNL;
import static com.scoperetail.commons.enums.OlcmEventName.REENRICH;
import static com.scoperetail.commons.enums.OrderLineStatus.ACTIVE;
import static com.scoperetail.commons.enums.OrderLineStatus.NEW;
import static com.scoperetail.commons.enums.OrderStatus.getOrderStatus;
import static com.scoperetail.internal.schema.Source.MANUAL_ALLOC;
import static com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder.addExceptionLog;
import static com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder.createExceptionLog;
import static com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder.createOrderException;
import static com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder.createOrderExceptionWithLines;
import static com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder.createOrderLineException;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ERROR_LOG_TARGET;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ORDERS_CAN_NOT_BE_EDITED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.common.rest.client.model.customer.group.CustomerAddressDto;
import com.scoperetail.common.rest.client.model.customer.group.CustomerDto;
import com.scoperetail.common.rest.client.model.customer.group.CustomerQueryResponse;
import com.scoperetail.common.rest.client.model.customer.group.GroupDto;
import com.scoperetail.common.rest.client.model.customer.order.CustomerOrderResponse;
import com.scoperetail.common.rest.client.model.customer.order.OrderLine;
import com.scoperetail.common.rest.client.model.product.supplier.ContentDto;
import com.scoperetail.common.rest.client.model.product.supplier.ProductSupplier;
import com.scoperetail.common.rest.client.model.product.supplier.ProductSupplierResponse;
import com.scoperetail.common.rest.client.model.product.supplier.SupplierResponse;
import com.scoperetail.common.rest.client.model.product.supplier.request.ProductsSuppliersRequest;
import com.scoperetail.common.rest.client.model.sos.SourceOfSupply;
import com.scoperetail.common.rest.client.model.sos.SourceOfSupplyResponse;
import com.scoperetail.common.rest.client.model.supplier.master.Supplier;
import com.scoperetail.common.rest.client.service.api.CustomerOrderService;
import com.scoperetail.common.rest.client.service.api.CustomerService;
import com.scoperetail.common.rest.client.service.api.ProductSupplierService;
import com.scoperetail.common.rest.client.service.api.SourceOfSupplyService;
import com.scoperetail.common.rest.client.service.api.SupplierService;
import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.OMSError;
import com.scoperetail.commons.dto.OlcmEvent;
import com.scoperetail.commons.enums.AdjustmentReason;
import com.scoperetail.commons.enums.Attribute;
import com.scoperetail.commons.enums.CustomerType;
import com.scoperetail.commons.enums.EnrichStatus;
import com.scoperetail.commons.enums.EnrichmentStatus;
import com.scoperetail.commons.enums.ErrorCode;
import com.scoperetail.commons.enums.GroupType;
import com.scoperetail.commons.enums.OlcmEventName;
import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.enums.OrderType;
import com.scoperetail.commons.enums.SupplierType;
import com.scoperetail.commons.manage.order.dto.request.OrderUpdateRequest;
import com.scoperetail.commons.manage.order.dto.request.ProductUpdateRequest;
import com.scoperetail.commons.model.Result;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.commons.util.DateUtil;
import com.scoperetail.commons.util.JsonUtil;
import com.scoperetail.internal.schema.AuditOrder;
import com.scoperetail.internal.schema.AuditOrderLine;
import com.scoperetail.internal.schema.AuditOrderLines;
import com.scoperetail.internal.schema.AuditOrders;
import com.scoperetail.internal.schema.ExceptionLog;
import com.scoperetail.internal.schema.OrderException;
import com.scoperetail.internal.schema.OrderExceptions;
import com.scoperetail.internal.schema.OrderLineException;
import com.scoperetail.internal.schema.OrderLineExceptions;
import com.scoperetail.internal.schema.OrderStatusCodes;
import com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder;
import com.scoperetail.logger.client.exception.spi.ExceptionMessageClient;
import com.scoperetail.order.enrichment.command.api.EnrichCommand;
import com.scoperetail.order.enrichment.model.OrderEnrichResult;
import com.scoperetail.order.persistence.entity.Capability;
import com.scoperetail.order.persistence.entity.EnrichTracker;
import com.scoperetail.order.persistence.entity.OrderCustomer;
import com.scoperetail.order.persistence.entity.OrderLineAdjustment;
import com.scoperetail.order.persistence.entity.OrderLineEnriched;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.entity.SupplierOrderEnriched;
import com.scoperetail.order.persistence.repository.CapabilityRepository;
import com.scoperetail.order.persistence.repository.EnrichTrackerRepository;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.audit.Auditable;
import com.scoperetail.supplier.order.processor.command.handler.api.OutBoundEventHandler;
import com.scoperetail.supplier.order.processor.command.handler.api.SupplierOrderCmdHandler;
import com.scoperetail.supplier.order.processor.command.model.CustomerOrder;
import com.scoperetail.validation.validators.CustomerTypeRestrictionCheck;
import com.scoperetail.validation.validators.RestrictedStatesCheck;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SupplierOrderCmdHandlerImpl implements SupplierOrderCmdHandler {

  private static final String ORDER_GROUP = "ORDER_GROUP";
  @Autowired private CustomerOrderService orderService;
  @Autowired private CustomerService customerService;
  @Autowired private SourceOfSupplyService sourceOfSupplyService;
  @Autowired private ProductSupplierService productSupplierService;
  @Autowired private SupplierService supplierService;
  @Autowired private SupplierOrderRepository supplierOrderRepository;
  @Autowired private ApplicationContext applicationContext;
  @Autowired private OutBoundEventHandler<OlcmEvent> orderOutboundHandler;
  @Autowired private RestrictedStatesCheck V18_RestrictedStatesCheck;
  @Autowired private CustomerTypeRestrictionCheck V36_CustomerTypeRestrictionCheck;
  @Autowired private ExceptionMessageClient exceptionMessageClient;
  @Autowired private CapabilityRepository capabilityRepository;
  @Autowired private EnrichTrackerRepository enrichTrackerRepository;

  @Value("#{'${manual.quantity.change.beans}'.split(',')}")
  private List<String> quantityChangeEnrichmentBeans;

  @Value("${E48}")
  private String seafoodEnrichmentBean;

  @Value("#{'${manual.orderstatus.change.beans}'.split(',')}")
  private List<String> orderStatusChangeEnrichmentBeans;

  @Value("#{'${manual.reenrich.beans}'.split(',')}")
  private List<String> reEnrichBeans;

  @Value("#{'${valid.order.status.for.order.edit}'.split(',')}")
  private List<Integer> validOrderStatuses;

  @Value("${honor.choice.of.customer.and.skip.sos.check:true}")
  private boolean honorChoiceOfCustomerAndSkipSosCheck;

  private Optional<SupplierResponse> unknownSupplierResponse = Optional.empty();

  private Supplier unknownSupplier = null;

  @Value("${unknown.supplier.id}")
  private Long unknownSupplierId;

  @Autowired private Environment env;

  @Data
  @Builder
  private static class PreUpdateOrderDetails {
    private OrderStatus orderstatus;
    private LocalDate processDate;
    @Builder.Default private Boolean isOrderLineUpdated = FALSE;
    @Builder.Default private Boolean isOrderStatusUpdated = FALSE;
    @Builder.Default private Boolean isOrderProcessDateUpdated = FALSE;
  }

  @Override
  @Auditable
  public void createSupplierOrder(
      final AuditOrders auditOrders,
      final CustomerOrder customerOrder,
      final List<OlcmEvent> events)
      throws Exception {
    List<SupplierOrder> allsupplierOrders = new ArrayList<>();
    final Integer customerOrderId = customerOrder.getOrderId();
    try {
      final CustomerOrderResponse order = getCustomerOrder(customerOrderId);
      orderLinesExists(order);

      log.debug(
          "Retrieving customer for customer={} for customer order={}",
          order.getCustomerId(),
          customerOrderId);
      final CustomerDto customer =
          getCustomerForOrder(order.getCustomerId(), order.getCustOrderId());
      final Optional<GroupDto> customerOrderGroupOpt = getCustomerOrderGroup(customer.getGroups());
      final OrderException orderException = createOrderException(customerOrderId, 0);
      if (customerOrderGroupOpt.isPresent()) {
        Map<Long, Supplier> suppliers = new HashMap<>();
        final Long supplierId = order.getSupplierId();
        if (honorChoiceOfCustomerAndSkipSosCheck && Optional.ofNullable(supplierId).isPresent()) {
          suppliers =
              Collections.singletonMap(
                  supplierId,
                  getSupplierOfCustomerChoice(supplierId, customerOrderId, orderException));
        } else {
          final List<SourceOfSupply> sourceOfSupplies =
              getAllSourceOfSupplies(
                  customer.getCustomerId(),
                  customerOrderGroupOpt.get(),
                  customerOrder.getOrderId(),
                  orderException);
          if (CollectionUtils.isNotEmpty(sourceOfSupplies))
            suppliers = getSuppliers(sourceOfSupplies, customerOrderId, orderException);
        }
        createSupplierOrders(suppliers, order, customer, allsupplierOrders, events, orderException);

        if (MANUAL_ALLOC.name().equals(order.getSrcOrderId())) {
          buildAuditOrderForNewOrderFromManualAlloc(
              auditOrders,
              allsupplierOrders.get(0),
              MERCHANDISER_INCREASED_QUANTITY.getCode(),
              order.getCreatedBy());
        }
      } else {
        log.debug(
            "COG not found for customer id {} : errorcode {}",
            customer.getCustomerId(),
            GROUP_NOT_FOUND);
        addExceptionLogAndThrow(
            customerOrderId, GROUP_NOT_FOUND, CUSTOMER_ID, customer.getCustomerId());
      }
      log.debug("Create supplier order process completed.");
    } catch (final ApplicationException appException) {
      sendErrorLog((OrderException) appException.getErrorData().getError());
      log.debug("Error log data {} sent.", JsonUtil.toJson(appException.getErrorData().getError()));
    }
  }

  private Optional<GroupDto> getCustomerOrderGroup(final List<GroupDto> groups) {
    Optional<GroupDto> groupDtoOpt = Optional.empty();
    if (CollectionUtils.isNotEmpty(groups)) {
      groupDtoOpt =
          groups.stream().filter(group -> ORDER_GROUP.equals(group.getGroupType())).findFirst();
    }
    return groupDtoOpt;
  }

  private ApplicationException addExceptionLogAndThrow(
      final Integer customerOrderId,
      final ErrorCode errorCode,
      final Attribute attribute,
      final Object attrValue)
      throws ApplicationException {
    final OrderException orderException = createOrderException(customerOrderId, 0);
    addExceptionLog(orderException, createExceptionLog(attribute, attrValue, errorCode));
    throw new ApplicationException(new OMSError<OrderException>(orderException));
  }

  private Map<Long, Supplier> getSuppliers(
      final List<SourceOfSupply> sourceOfSupplies,
      final Integer customerOrderId,
      final OrderException orderException) {
    final List<Long> sosIds =
        sourceOfSupplies.stream().map(SourceOfSupply::getSupplierId).collect(Collectors.toList());
    return getAllSuppliers(sosIds, customerOrderId, orderException)
        .stream()
        .collect(Collectors.toMap(Supplier::getSupplierId, supplier -> supplier));
  }

  private void orderLinesExists(final CustomerOrderResponse order) {
    if (CollectionUtils.isEmpty(order.getOrderLines())) {
      log.error(ORDER_LINE_NOT_FOUND.getLabel(), order.getOrderLines());
      addExceptionLogAndThrow(
          order.getCustOrderId(), ORDER_LINE_NOT_FOUND, CUSTOMER_ORDER_ID, order.getCustOrderId());
    }
  }

  private CustomerOrderResponse getCustomerOrder(final Integer orderId) {
    log.debug("Retrieving customer order for order id {}", orderId);
    final CustomerOrderResponse customerOrderRes =
        orderService
            .getCustomerOrder(orderId)
            .orElseThrow(
                () ->
                    addExceptionLogAndThrow(orderId, ORDER_NOT_FOUND, CUSTOMER_ORDER_ID, orderId));
    if (null == customerOrderRes.getCustOrderId()) {
      addExceptionLogAndThrow(orderId, ORDER_NOT_FOUND, CUSTOMER_ORDER_ID, orderId);
    }
    return customerOrderRes;
  }

  private CustomerDto getCustomerForOrder(final Long customerId, final Integer custOrderId) {
    final Optional<CustomerQueryResponse> optRes =
        customerService.getCustomers(Arrays.asList(customerId));
    if (optRes
        .map(CustomerQueryResponse::getCustomers)
        .map(CollectionUtils::isEmpty)
        .orElse(FALSE)) {
      log.debug("Customer not found.");
      addExceptionLogAndThrow(custOrderId, CUSTOMER_NOT_FOUND, CUSTOMER_ID, customerId);
    }
    return optRes.get().getCustomers().get(0);
  }

  private List<SourceOfSupply> getAllSourceOfSupplies(
      final Long customerId,
      final GroupDto groupDto,
      final Integer orderId,
      final OrderException orderException) {
    log.debug("Retrieving SOS for customer {} and COGs {}", customerId, groupDto.getGroupId());
    final Integer cogId = groupDto.getGroupId();
    final Optional<SourceOfSupplyResponse> sourceOfSupplyResOPt =
        sourceOfSupplyService.getSourceSoupplies(customerId, cogId);

    final Optional<List<SourceOfSupply>> sourceOfSupplies =
        sourceOfSupplyResOPt
            .map(SourceOfSupplyResponse::getPage)
            .map(ContentDto<SourceOfSupply>::getContent);

    if (sourceOfSupplies.map(CollectionUtils::isEmpty).orElse(FALSE)) {
      log.debug("Source of supplies not found for COG {} : errorcode {}", cogId, SOS_NOT_FOUND);
      addExceptionLog(orderException, createExceptionLog(COG_ID, cogId, SOS_NOT_FOUND));
    }
    return sourceOfSupplies.get();
  }

  private void buildAuditOrderForNewOrderFromManualAlloc(
      final AuditOrders auditOrders,
      final SupplierOrder order,
      final Integer changeReasonId,
      final String userId) {
    log.debug("Build audit log for new order created from Manual Allocation.");
    final AuditOrder auditOrder = new AuditOrder();
    auditOrder.setOrderId(order.getOrderId());
    auditOrder.setChangeReasonId(changeReasonId);
    auditOrder.setUserId(userId);
    auditOrder.setSourcets(DateUtil.toString(LocalDateTime.now()));
    auditOrder.setStatusCode(
        OrderStatusCodes.fromValue(getOrderStatus(order.getOrderStatusId()).name()));

    auditOrder.setAuditOrderLines(new AuditOrderLines());
    order
        .getOrderLines()
        .forEach(
            line -> {
              final AuditOrderLine auditOrderLine = buildAuditOrderLine(line, changeReasonId);
              auditOrderLine.setLineStatusCode(NEW.name());
              auditOrder.getAuditOrderLines().getAuditOrderLine().add(auditOrderLine);
            });
    auditOrders.getAuditOrder().add(auditOrder);
  }

  public void createSupplierOrders(
      final Map<Long, Supplier> suppliers,
      final CustomerOrderResponse order,
      final CustomerDto customer,
      final List<SupplierOrder> allsupplierOrders,
      final List<OlcmEvent> events,
      final OrderException orderLevelException)
      throws Exception {
    log.debug(
        "Creating supplier orders for order {} customer {} suppliers", order, customer, suppliers);
    final Map<Long, OrderLineException> productIdsWithLineExceptions = new HashMap<>();
    Map<Long, SupplierOrder> supplierOrders = new HashMap<>();
    if (Optional.ofNullable(orderLevelException.getOrderExceptionLogs()).isPresent()) {
      // Build Supplier Order(Only Unknown) if there are Order level exceptions(SOS
      // not found OR Supplier not found)
      supplierOrders = getSupplierOrders(order, customer, orderLevelException);
    } else {
      // Build Supplier Order(Normal OR Unknown) if there are no Order level
      // exceptions
      supplierOrders = getSupplierOrders(order, customer, suppliers, productIdsWithLineExceptions);
    }
    allsupplierOrders.addAll(supplierOrders.values());
    supplierOrderRepository.saveAll(allsupplierOrders);
    allsupplierOrders.forEach(suppOrder -> addOlcmEvent(events, suppOrder, CREATED));
    if (Optional.ofNullable(orderLevelException.getOrderExceptionLogs()).isPresent()
        || MapUtils.isNotEmpty(productIdsWithLineExceptions)) {
      final List<OrderException> orderExceptions =
          buildSupplierOrderExceptions(
              order.getCustOrderId(),
              allsupplierOrders,
              productIdsWithLineExceptions,
              orderLevelException);
      for (OrderException orderException : orderExceptions) {
        sendErrorLog(orderException);
      }
    }
    log.info(
        "Supplier order created for the customer {} for customer order {}",
        order.getCustomerId(),
        order.getCustOrderId());
  }

  private List<OrderException> buildSupplierOrderExceptions(
      final Integer custOrderId,
      final List<SupplierOrder> allsupplierOrders,
      final Map<Long, OrderLineException> productIdsWithLineExceptions,
      final OrderException orderLevelException) {
    final List<OrderException> orderExceptions = new ArrayList<>();
    allsupplierOrders
        .stream()
        .forEach(
            suppOrder -> {
              final OrderException orderException =
                  Optional.ofNullable(orderLevelException.getOrderExceptionLogs()).isPresent()
                      ? orderLevelException
                      : createOrderExceptionWithLines(custOrderId, suppOrder.getOrderId());
              suppOrder
                  .getOrderLines()
                  .stream()
                  .forEach(
                      ol -> {
                        final OrderLineException orderLineException =
                            productIdsWithLineExceptions.get(ol.getOrigProductId());
                        if (nonNull(orderLineException)) {
                          orderLineException.setLineNumber(ol.getLineNbr());
                          orderException
                              .getOrderLineExceptions()
                              .getOrderLineException()
                              .add(orderLineException);
                        }
                      });
              if (Optional.ofNullable(orderLevelException.getOrderExceptionLogs()).isPresent()
                  || isNotEmpty(orderException.getOrderLineExceptions().getOrderLineException())) {
                log.error(
                    "There are errors in order lines for supplier order id {}",
                    suppOrder.getOrderId());
                orderExceptions.add(orderException);
              }
            });
    return orderExceptions;
  }

  private OlcmEvent buildOlcmEvent(final SupplierOrder order, final OlcmEventName eventName) {
    final String supplierType = SupplierType.getSupplierType(order.getSupplierTypeId()).name();
    final String customerType =
        CustomerType.getCustomerType(order.getOrderCustomer().getCustTypeId()).name();
    final String orderType = OrderType.getOrderType(order.getOrderTypeId()).name();
    return OlcmEvent.builder()
        .orderId(order.getOrderId())
        .supplierType(supplierType)
        .customerType(customerType)
        .orderType(orderType)
        .eventName(eventName)
        .payload("")
        /*
         * .payload(supplierType + "-" + customerType + "-" + orderType + ":" +
         * order.getOrderId())
         */
        .build();
  }

  private Map<Long, List<ProductSupplier>> getProductsSuppliers(
      final CustomerOrderResponse order, final Set<Long> supplierIds) {
    final List<Long> productIds =
        order.getOrderLines().stream().map(OrderLine::getProductId).collect(Collectors.toList());
    ProductsSuppliersRequest productsSuppliersRequest =
        ProductsSuppliersRequest.builder()
            .productIds(productIds)
            .supplierIds(new ArrayList<>(supplierIds))
            .build();
    final Optional<ProductSupplierResponse> optProductSupplierResponse =
        productSupplierService.getProductsSuppliers(productsSuppliersRequest);
    final List<ProductSupplier> productSuppliers =
        optProductSupplierResponse
            .map(ProductSupplierResponse::getPage)
            .map(ContentDto<ProductSupplier>::getContent)
            .orElse(new ArrayList<ProductSupplier>());

    final Map<Long, List<ProductSupplier>> productsSuppliersMap =
        productSuppliers.stream().collect(Collectors.groupingBy(ProductSupplier::getProductId));
    return productsSuppliersMap;
  }

  private Map<Long, SupplierOrder> getSupplierOrders(
      final CustomerOrderResponse order,
      final CustomerDto customer,
      final Map<Long, Supplier> suppliers,
      final Map<Long, OrderLineException> productIdsWithLineExceptions) {
    final Map<Long, SupplierOrder> supplierOrders = new HashMap<>();
    final Set<Long> keys = suppliers.keySet();
    final Map<Long, List<ProductSupplier>> productsSuppliersMap = getProductsSuppliers(order, keys);

    for (final OrderLine orderLine : order.getOrderLines()) {
      if (OrderLineStatus.REJECTED.getCode() != orderLine.getOrderLineStatusId()) {
        log.debug(
            "Retreiving suppliers for product {} and supplier", orderLine.getProductId(), keys);
        final List<ExceptionLog> exceptionLogs = new ArrayList<>();

        final List<ProductSupplier> productSuppliers =
            ofNullable(productsSuppliersMap.get(orderLine.getProductId()))
                .orElse(new ArrayList<ProductSupplier>());
        if (CollectionUtils.isEmpty(productSuppliers)) {
          orderLine.setRejectReasonCode(PRODUCT_SUPPLIER_NOT_FOUND.getCode());
          exceptionLogs.add(
              createExceptionLog(PRODUCT_ID, orderLine.getLineNbr(), PRODUCT_SUPPLIER_NOT_FOUND));
        }

        final AtomicInteger atomicInt = new AtomicInteger(0);
        Boolean isValidOrderLine = false;
        for (final ProductSupplier productSupplier : productSuppliers) {
          atomicInt.incrementAndGet();
          final Optional<ErrorCode> restrictedStateErrorCode =
              V18_RestrictedStatesCheck.validate(
                  Optional.ofNullable(customer.getAddress())
                      .map(CustomerAddressDto::getState)
                      .orElse(null),
                  Optional.ofNullable(productSupplier.getRestrictedState()));
          final Boolean isRestrictedStatesCheckFailed = restrictedStateErrorCode.isPresent();

          final Optional<ErrorCode> customerTypeRestrictionErrorCode =
              isCustomerTypeRestricted(
                  productSupplier.getAllowedCustomerTypes(), customer.getCustType());
          final Boolean isCustTypeCheckFailed = customerTypeRestrictionErrorCode.isPresent();

          if (!isRestrictedStatesCheckFailed && !isCustTypeCheckFailed) {
            isValidOrderLine = true;
            buildSupplierOrder(
                order,
                orderLine,
                customer,
                suppliers.get(productSupplier.getSupplierId()),
                supplierOrders,
                productSupplier.getSupplierProductId());
            break;
          }

          if (atomicInt.get() == productSuppliers.size()) {
            if (isRestrictedStatesCheckFailed) {
              orderLine.setRejectReasonCode(restrictedStateErrorCode.get().getCode());
              exceptionLogs.add(
                  createExceptionLog(
                      CUSTOMER_STATE,
                      customer.getAddress().getState(),
                      restrictedStateErrorCode.get()));
            }
            if (isCustTypeCheckFailed) {
              if (!Optional.ofNullable(orderLine.getRejectReasonCode()).isPresent())
                orderLine.setRejectReasonCode(customerTypeRestrictionErrorCode.get().getCode());
              exceptionLogs.add(
                  createExceptionLog(
                      CUSTOMER_TYPE,
                      CustomerType.getCustomerType(customer.getCustType()).toString(),
                      customerTypeRestrictionErrorCode.get()));
            }
          }
        }
        if (!isValidOrderLine) {
          buildSupplierOrder(
              order, orderLine, customer, getUnknownSupplier(), supplierOrders, null);
        }
        if (isNotEmpty(exceptionLogs)) {
          final OrderLineException orderLineException =
              createOrderLineException(
                  orderLine.getLineNbr(), orderLine.getProductId(), exceptionLogs);
          productIdsWithLineExceptions.put(orderLine.getProductId(), orderLineException);
        }
      } else {
        buildSupplierOrder(order, orderLine, customer, getUnknownSupplier(), supplierOrders, null);
      }
    }
    return supplierOrders;
  }

  private Map<Long, SupplierOrder> getSupplierOrders(
      final CustomerOrderResponse order,
      final CustomerDto customer,
      final OrderException orderLevelException) {
    final Map<Long, SupplierOrder> supplierOrders = new HashMap<>();
    // Exception log would always be of size '1' bcz if SOS check fails, validate
    // supplier check doesn't execute
    ExceptionLog exceptionLog =
        orderLevelException.getOrderExceptionLogs().getExceptionLog().get(0);

    for (final OrderLine orderLine : order.getOrderLines()) {
      if (!Optional.ofNullable(orderLine.getRejectReasonCode()).isPresent())
        orderLine.setRejectReasonCode(Integer.valueOf(exceptionLog.getExceptionCode()));
      buildSupplierOrder(order, orderLine, customer, getUnknownSupplier(), supplierOrders, null);
    }
    return supplierOrders;
  }

  private Supplier getUnknownSupplier() {
    if (!unknownSupplierResponse.isPresent() || unknownSupplier == null) {
      unknownSupplierResponse = supplierService.getSuppliers(Arrays.asList(unknownSupplierId));
      unknownSupplier = unknownSupplierResponse.get().getPage().getContent().get(0);
    }
    return unknownSupplier;
  }

  private Optional<ErrorCode> isCustomerTypeRestricted(
      final String allowedTypes, final String customerType) {
    String allowedCustomerTypeIds = null;
    Optional<ErrorCode> optionalErrorCode = Optional.ofNullable((CUSTOMER_TYPE_NOT_ALLOWED));
    if (Optional.ofNullable(allowedTypes).isPresent()) {
      allowedCustomerTypeIds =
          Arrays.asList(allowedTypes.split(COMMA))
              .stream()
              .map(v -> String.valueOf(CustomerType.valueOf(v).getCode()))
              .collect(Collectors.joining(COMMA));
      optionalErrorCode =
          V36_CustomerTypeRestrictionCheck.validate(
              CustomerType.getCustomerType(customerType).getCode(),
              Optional.ofNullable(allowedCustomerTypeIds));
    }
    return optionalErrorCode;
  }

  private void buildSupplierOrder(
      final CustomerOrderResponse order,
      final OrderLine orderLine,
      final CustomerDto customer,
      final Supplier supplier,
      final Map<Long, SupplierOrder> supplierOrders,
      final String supplierProductId) {
    final com.scoperetail.order.persistence.entity.OrderLine supplierOrderLine =
        createOrderLine(orderLine);
    if (supplier.getSupplierId().equals(unknownSupplierId)) {
      supplierOrderLine.setOrderLineStatusId(OrderLineStatus.REJECTED.getCode());
    }
    final OrderLineEnriched ole =
        OrderLineEnriched.builder()
            .orderLine(supplierOrderLine)
            .currentItemQty(orderLine.getOrderedItemQty())
            .supplierProductId(supplierProductId) // supplierProductId required for
            // forecaster.
            .build();
    supplierOrderLine.setOrderLineEnriched(ole);

    SupplierOrder supplierOrder = supplierOrders.get(supplier.getSupplierId());
    if (null == supplierOrder) {
      supplierOrder = createSupplierOrder(order, supplier);
      final OrderCustomer orderCustomer = createOrderCustomer(customer, supplierOrder);
      supplierOrder.setOrderCustomer(orderCustomer);
      final SupplierOrderEnriched supplierOrderEnriched =
          createSupplierOrderEnriched(order, supplierOrder);
      supplierOrder.setSupplierOrderEnriched(supplierOrderEnriched);
      supplierOrder.setCreatedBy(order.getCreatedBy());
      supplierOrders.put(supplier.getSupplierId(), supplierOrder);
    }
    ole.setSupplierOrder(supplierOrder);
    supplierOrderLine.setSupplierOrder(supplierOrder);
    supplierOrder.getOrderLines().add(supplierOrderLine);
    supplierOrder.setCreatedBy(order.getCreatedBy());
    supplierOrder
        .getSupplierOrderEnriched()
        .setOrigSuppLineCount(supplierOrder.getOrderLines().size());
  }

  private List<Supplier> getAllSuppliers(
      final List<Long> supplierIds,
      final Integer customerOrderId,
      final OrderException orderException) {
    log.debug("Retrieving supplier details for the product {}", supplierIds);
    final Optional<SupplierResponse> supplierResponseOpt =
        supplierService.getSuppliers(supplierIds);

    final Optional<List<Supplier>> suppliers =
        supplierResponseOpt.map(SupplierResponse::getPage).map(ContentDto<Supplier>::getContent);

    if (suppliers.map(CollectionUtils::isEmpty).orElse(FALSE)) {
      log.debug(
          "Suppliers not found for SOS Ids {} : errorcode {}", supplierIds, SUPPLIER_NOT_FOUND);
      addExceptionLog(
          orderException, createExceptionLog(SUPPLIER_IDS, supplierIds, SUPPLIER_NOT_FOUND));
    }
    return suppliers.get();
  }

  private Supplier getSupplierOfCustomerChoice(
      final Long supplierId, final Integer customerOrderId, final OrderException orderException) {
    List<Long> supplierIds = Collections.singletonList(supplierId);
    log.debug("Retrieving suppliers {} of customer choice", supplierIds);
    final Optional<SupplierResponse> supplierResponseOpt =
        supplierService.getSuppliers(supplierIds);

    final Optional<List<Supplier>> suppliers =
        supplierResponseOpt.map(SupplierResponse::getPage).map(ContentDto::getContent);
    if (suppliers.map(CollectionUtils::isEmpty).orElse(FALSE)) {
      log.debug(
          "Suppliers of customer choice not found for supplied Ids {} : errorcode {}",
          supplierIds,
          SUPPLIER_NOT_FOUND);
      addExceptionLog(
          orderException, createExceptionLog(SUPPLIER_IDS, supplierIds, SUPPLIER_NOT_FOUND));
    }
    return suppliers.get().stream().findFirst().get();
  }

  private SupplierOrderEnriched createSupplierOrderEnriched(
      final CustomerOrderResponse order, final SupplierOrder supplierOrder) {
    final SupplierOrderEnriched supplierOrderEnriched =
        SupplierOrderEnriched.builder()
            .supplierOrder(supplierOrder)
            .routeCode(order.getRouteCode())
            .routeId(order.getRouteId())
            .srcOrderId(Integer.toString(order.getCustOrderId()))
            .srcOrderId2(order.getSrcOrderId2())
            .scheduledProcessDate(order.getScheduledProcessDate())
            .scheduledDeliveryDate(order.getScheduledDeliveryDate())
            .scheduledCutoffDate(LocalDate.now())
            .scheduledReleaseDate(LocalDate.now())
            .stopId(order.getStopId())
            .poRefNbr(order.getPoRefNbr())
            .transferTypeCd(order.getTransferTypeCd())
            .comment(order.getComment())
            .srcOrderCreateTs(order.getSrcOrderCreateTs())
            .origCustLineCount(order.getOrderLines().size())
            .srcSuppId(order.getSrcSuppId())
            .notifyInd(order.getNotifyInd().charAt(0))
            .build();

    return supplierOrderEnriched;
  }

  private com.scoperetail.order.persistence.entity.OrderLine createOrderLine(
      final OrderLine orderLine) {
    return com.scoperetail.order.persistence.entity.OrderLine.builder()
        .orderedItemQty(orderLine.getOrderedItemQty())
        .origProductId(orderLine.getProductId())
        .expiredProductId(orderLine.getExpiredProductId())
        .orderLineStatusId(
            orderLine.getOrderLineStatusId().equals(NEW.getCode())
                ? ACTIVE.getCode()
                : orderLine.getOrderLineStatusId())
        .productId(orderLine.getProductId())
        .rejectReasonCode(orderLine.getRejectReasonCode())
        .build();
  }

  private OrderCustomer createOrderCustomer(
      final CustomerDto customer, final SupplierOrder supplierOrder) {
    final Map<GroupType, List<GroupDto>> groupMap =
        customer
            .getGroups()
            .stream()
            .collect(
                Collectors.groupingBy(
                    p -> GroupType.getGroupTypeOptional(p.getGroupType()).orElse(null)));

    // Get the first Order group and first release group as we can store only one
    // order group and
    // release group for order.
    final GroupDto orderGroup = groupMap.get(GroupType.ORDER_GROUP).get(0);
    final List<GroupDto> releaseGroup = groupMap.get(GroupType.RELEASE_GROUP);
    final OrderCustomer orderCustomer =
        OrderCustomer.builder()
            .customerId(customer.getCustomerId())
            .divisionId(customer.getDivisionId())
            .customerName(customer.getFullName())
            .custTypeId(CustomerType.getCustomerType(customer.getCustType()).getCode())
            .orderId(supplierOrder.getOrderId())
            .cogId(orderGroup.getGroupId())
            .cogLabel(orderGroup.getLabel())
            .crgId(isNotEmpty(releaseGroup) ? releaseGroup.get(0).getGroupId() : null)
            .crgLabel(isNotEmpty(releaseGroup) ? releaseGroup.get(0).getLabel() : null)
            .crgIds(
                isNotEmpty(releaseGroup)
                    ? releaseGroup
                        .stream()
                        .map(grp -> String.valueOf(grp.getGroupId()))
                        .collect(joining(COMMA))
                    : null)
            .supplierOrder(supplierOrder)
            .customerAccId(customer.getCustomerAccId())
            .state(
                Optional.ofNullable(customer.getAddress())
                    .map(CustomerAddressDto::getState)
                    .orElse(null))
            .build();
    return orderCustomer;
  }

  private SupplierOrder createSupplierOrder(
      final CustomerOrderResponse order, final Supplier supplier) {
    final SupplierOrder supplierOrder =
        SupplierOrder.builder()
            .custOrderId(order.getCustOrderId())
            .orderTypeId(order.getOrderTypeId())
            .supplierId(supplier.getSupplierId())
            .supplierName(supplier.getFullName())
            .supplierTypeId(supplier.getSupplierTypeId())
            .orderStatusId(
                supplier.getSupplierId().equals(unknownSupplierId)
                    ? OrderStatus.REJECTED.getCode()
                    : order.getOrderStatusId())
            .enrichmentStatus(NOT_STARTED.getLabel())
            .distCenterId(supplier.getDistributionCenterId())
            .physWhseId(supplier.getPhysicalWarehouseId())
            .orderLines(new HashSet<>())
            .distCenterId(supplier.getDistributionCenterId())
            .physWhseId(supplier.getPhysicalWarehouseId())
            .suppDivisionId(supplier.getDivisionId())
            .veto(order.getVeto())
            .build();

    Optional.ofNullable(order.getFutureOrder())
        .filter(s -> !s.isEmpty())
        .map(String::toUpperCase)
        .ifPresent(
            futureOrder -> {
              supplierOrder.setFutureOrder(futureOrder.charAt(0) == 'Y' ? 'Y' : 'N');
            });
    return supplierOrder;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Auditable
  public BaseResponse updateSupplierOrders(
      final AuditOrders auditOrders,
      final List<OrderUpdateRequest> orderUpdateRequests,
      final List<OlcmEvent> events,
      final String userId) {
    final Map<Integer, OrderUpdateRequest> orderRequestMap =
        orderUpdateRequests.stream().collect(Collectors.toMap(r -> r.getOrderId(), r -> r));
    final List<Integer> orderIds = new ArrayList<>(orderRequestMap.keySet());
    log.info("Updating Supplier Orders : " + orderIds);
    final Optional<List<SupplierOrder>> optionalSupplierOrders =
        supplierOrderRepository.findByOrderIdInAndOrderStatusIdNotIn(
            orderIds, Arrays.asList(OrderStatus.CANCELLED.getCode()));
    if (optionalSupplierOrders.isPresent()) {
      final List<SupplierOrder> supplierOrders = optionalSupplierOrders.get();
      if (isAnyOrderLockedForEdit(supplierOrders)) {
        log.error(ORDERS_CAN_NOT_BE_EDITED);
        throw new ApplicationException(ORDERS_CAN_NOT_BE_EDITED);
      }
      supplierOrders.forEach(
          order -> {
            final PreUpdateOrderDetails preUpdateOrderDetails =
                PreUpdateOrderDetails.builder()
                    .orderstatus(OrderStatus.getOrderStatus(order.getOrderStatusId()))
                    .processDate(order.getSupplierOrderEnriched().getScheduledProcessDate())
                    .build();
            final OrderUpdateRequest orderUpdateRequest = orderRequestMap.get(order.getOrderId());
            orderUpdateRequest.setLoggedInUser(userId);
            auditOrders.getAuditOrder().add(buildAuditSupplierOrder(orderUpdateRequest, order));
            updateSupplierOrder(orderUpdateRequest, order);

            log.info("Triggering enrichments as order status updated.");
            triggerEnrichments(order, preUpdateOrderDetails, events);
          });
      supplierOrderRepository.saveAll(supplierOrders);
      return new BaseResponse(BaseResponse.Status.SUCCESS, "Orders updated successfully!");
    }
    return new BaseResponse(BaseResponse.Status.FAILURE, "No orders to update!!");
  }

  private AuditOrder buildAuditSupplierOrder(
      final OrderUpdateRequest request, final SupplierOrder order) {
    final AuditOrder auditOrder = new AuditOrder();
    auditOrder.setOrderId(order.getOrderId());
    auditOrder.setChangeReasonId(
        Objects.nonNull(request.getChangeReasonId()) ? request.getChangeReasonId() : 0);
    auditOrder.setUserId(request.getLoggedInUser());
    auditOrder.setSourcets(DateUtil.toString(LocalDateTime.now()));

    if (!request
        .getProcessingDate()
        .equals(order.getSupplierOrderEnriched().getScheduledProcessDate())) {
      auditOrder.setSchedProcessDate(DateUtil.toString(request.getProcessingDate()));
    }

    if (!request
        .getDeliveryDate()
        .equals(order.getSupplierOrderEnriched().getScheduledDeliveryDate())) {
      auditOrder.setSchedDeliveryDate(DateUtil.toString(request.getDeliveryDate()));
    }

    if (request.getOrderStatusId() != null
        && !order.getOrderStatusId().equals(request.getOrderStatusId())) {
      auditOrder.setStatusCode(
          OrderStatusCodes.fromValue(
              OrderStatus.getOrderStatus(request.getOrderStatusId()).name()));
    }
    return auditOrder;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Auditable
  public BaseResponse updateSupplierOrder(
      final AuditOrders auditOrders,
      final OrderUpdateRequest orderUpdateRequest,
      final List<OlcmEvent> events) {
    BaseResponse response =
        new BaseResponse(BaseResponse.Status.FAILURE, "Order not found to update");
    final Map<Integer, ProductUpdateRequest> lineNbrMap =
        orderUpdateRequest
            .getItemUpdateRequest()
            .stream()
            .collect(Collectors.toMap(r -> r.getLineNbr(), r -> r));
    Optional<SupplierOrder> optionalSupplierOrder = Optional.empty();
    final Optional<List<ProductUpdateRequest>> optionalItemUpdateRequest =
        Optional.ofNullable(orderUpdateRequest)
            .map(OrderUpdateRequest::getItemUpdateRequest)
            .filter(i -> !i.isEmpty());
    if (!optionalItemUpdateRequest.isPresent()) {
      log.info("Updating Supplier Order with orderId : " + orderUpdateRequest.getOrderId());
      optionalSupplierOrder =
          supplierOrderRepository.findByOrderIdAndOrderStatusIdNotIn(
              orderUpdateRequest.getOrderId(), Arrays.asList(OrderStatus.CANCELLED.getCode()));
    } else {
      final List<Integer> lineNbrs = new ArrayList<>(lineNbrMap.keySet());
      log.info(
          "Updating Supplier Order with orderId : "
              + orderUpdateRequest.getOrderId()
              + " and productIds : "
              + lineNbrs);
      optionalSupplierOrder =
          supplierOrderRepository.findByOrderIdAndOrderLines_LineNbrInAndOrderStatusIdNotIn(
              orderUpdateRequest.getOrderId(),
              lineNbrs,
              Arrays.asList(OrderStatus.CANCELLED.getCode()));
    }
    if (optionalSupplierOrder.isPresent()) {
      final SupplierOrder supplierOrder = optionalSupplierOrder.get();
      if (!isAnyOrderLockedForEdit(Collections.singletonList(supplierOrder))) {
        final PreUpdateOrderDetails preUpdateOrderDetails =
            PreUpdateOrderDetails.builder()
                .orderstatus(OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId()))
                .processDate(supplierOrder.getSupplierOrderEnriched().getScheduledProcessDate())
                .build();
        final AuditOrder auditOrder = buildAuditSupplierOrder(orderUpdateRequest, supplierOrder);
        auditOrders.getAuditOrder().add(auditOrder);

        updateSupplierOrder(orderUpdateRequest, supplierOrder);
        Boolean isOrderLineUpdated = FALSE;
        if (!lineNbrMap.isEmpty()) {
          isOrderLineUpdated =
              updateSupplierOrderLines(
                  lineNbrMap, supplierOrder, auditOrder, orderUpdateRequest.getLoggedInUser());
          preUpdateOrderDetails.setIsOrderLineUpdated(isOrderLineUpdated);
        }
        supplierOrderRepository.save(supplierOrder);

        triggerEnrichments(supplierOrder, preUpdateOrderDetails, events);
        
        response = new BaseResponse(BaseResponse.Status.SUCCESS, "Order updated successfully");
      } else {
        response =
            new BaseResponse(
                BaseResponse.Status.FAILURE, "The order can not be modified at this point of time");
      }
    }
    return response;
  }

  public Set<String> identifyEnrichments(
      final Boolean pdchanged,
      final Boolean statuschanged,
      final OrderStatus currentStatus,
      final OrderStatus newStatus,
      final Boolean linesUpdated,
      final StringBuilder enrichmnetExecutionType) {
    StringBuilder propNameBuilder =
        getOrderEditPropertyName(pdchanged, statuschanged, currentStatus, newStatus);

    Optional<List<List<String>>> enrichmentsWithExecutionType =
        Optional.ofNullable(env.getProperty(propNameBuilder.toString()))
            .map(s -> s.split(":"))
            .map(arr -> Arrays.asList(arr))
            .map(
                lst ->
                    lst.stream()
                        .map(s -> s.split(","))
                        .map(arr -> Arrays.asList(arr))
                        .collect(Collectors.toList()));

    final Set<String> enrichmentsToRun = new LinkedHashSet<>();

    enrichmentsWithExecutionType
        .filter(lst -> (lst.size() == 2 && lst.get(0).size() > 0 && lst.get(1).size() > 0))
        .ifPresent(
            lst -> {
              enrichmentsToRun.addAll(lst.get(0));
              enrichmnetExecutionType.append(lst.get(1).get(0));
            });
    if (linesUpdated) {
      enrichmentsToRun.addAll(quantityChangeEnrichmentBeans);
    }
    return enrichmentsToRun;
  }

  private StringBuilder getOrderEditPropertyName(
      final Boolean pdchanged,
      final Boolean statuschanged,
      final OrderStatus currentStatus,
      final OrderStatus newStatus) {
    StringBuilder propNameBuilder = new StringBuilder("manual");

    if (pdchanged) propNameBuilder.append(".pdchanged");
    if (statuschanged) {
      propNameBuilder.append(".statuschanged");
      propNameBuilder.append("." + currentStatus.name());
      propNameBuilder.append("." + newStatus.name());
    }
    return propNameBuilder;
  }

  private void triggerEnrichments(
      final SupplierOrder supplierOrder,
      final PreUpdateOrderDetails preUpdateOrderDetails,
      final List<OlcmEvent> events) {

    final Boolean processDateChanged =
        !supplierOrder
            .getSupplierOrderEnriched()
            .getScheduledProcessDate()
            .equals(preUpdateOrderDetails.getProcessDate());

    final OrderStatus newOrderStatus = OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId());
    final OrderStatus currentOrderStatus = preUpdateOrderDetails.getOrderstatus();

    final Boolean statusChanged = !currentOrderStatus.equals(newOrderStatus);
    final Boolean linesUpdated = preUpdateOrderDetails.getIsOrderLineUpdated();

    StringBuilder enrichmnetExecutionType = new StringBuilder();
    final Set<String> enrichmentsToBeRun =
        identifyEnrichments(
            processDateChanged,
            statusChanged,
            currentOrderStatus,
            newOrderStatus,
            linesUpdated,
            enrichmnetExecutionType);

    preUpdateOrderDetails.setIsOrderStatusUpdated(statusChanged);
    preUpdateOrderDetails.setIsOrderProcessDateUpdated(processDateChanged);

    if (enrichmnetExecutionType.toString().equals("ASYNC")) {
      log.debug("Async execution + notify OLCM for OrderId:{}", supplierOrder.getOrderId());
      markEnrichTrackersForAsyncExecution(enrichmentsToBeRun, supplierOrder.getOrderId());

      // Dont allow user to edit this order.
      supplierOrder.setEnrichmentStatus(IN_PROGRESS.getLabel());
      
      addOlcmEvent(events, supplierOrder, REENRICH);
    } else if (enrichmnetExecutionType.toString().equals("SYNC")) {
      log.debug("Sync execution + notify OLCM for OrderId:{}", supplierOrder.getOrderId());
      executeEnrichment(enrichmentsToBeRun, supplierOrder);
      addOlcmEventFromOrderStatus(events, supplierOrder);
    } else {
      log.debug(
          "Sync execution without notifying OLCM (only Line edit) for OrderId:{}",
          supplierOrder.getOrderId());
      executeEnrichment(enrichmentsToBeRun, supplierOrder);
    }
  }

  private void markEnrichTrackersForAsyncExecution(
      final Set<String> enrichmentsToBeRun, final Integer orderId) {
    final List<EnrichTracker> enrichTrackers =
        enrichTrackerRepository.findByOrderIdOrderByEnrichSequence(orderId);
    log.info("Executing Asynchronous enrichments via x-sequencer {}", enrichmentsToBeRun);
    final Map<Integer, String> requiredEnrichTrackers =
        enrichTrackers
            .stream()
            .filter(et -> enrichmentsToBeRun.contains(et.getCapability().getCapabilityInterface()))
            .collect(
                Collectors.toMap(
                    EnrichTracker::getEnrichTrackerId,
                    et -> et.getCapability().getCapabilityInterface()));

    final List<Integer> enrichTrackerIds = new ArrayList<>(requiredEnrichTrackers.keySet());
    enrichTrackerRepository.updateStatus(enrichTrackerIds, EnrichStatus.PENDING.getCode());
    log.info(
        "Marked enrichments to execute through x-sequencer = {}", requiredEnrichTrackers.values());
  }

  private void addOlcmEventFromOrderStatus(
      final List<OlcmEvent> events, final SupplierOrder supplierOrder) {
    final OlcmEventName eventName =
        getOlcmEventName(getOrderStatus(supplierOrder.getOrderStatusId()));
    events.add(buildOlcmEvent(supplierOrder, eventName));
  }

  private void addOlcmEvent(
      final List<OlcmEvent> events,
      final SupplierOrder supplierOrder,
      final OlcmEventName eventName) {
    events.add(buildOlcmEvent(supplierOrder, eventName));
  }

  private void updateSupplierOrder(
      final OrderUpdateRequest orderUpdateRequest, final SupplierOrder supplierOrder) {
    final SupplierOrderEnriched supplierOrderEnriched = supplierOrder.getSupplierOrderEnriched();
    final Optional<OrderUpdateRequest> optionalOrderUpdateRequest =
        Optional.ofNullable(orderUpdateRequest);
    final Optional<LocalDate> optDeliveryDate =
        optionalOrderUpdateRequest.map(OrderUpdateRequest::getDeliveryDate);
    if (optDeliveryDate.isPresent()) {
      supplierOrderEnriched.setScheduledDeliveryDate(optDeliveryDate.get());
    }
    final Optional<LocalDate> optProcessingDate =
        optionalOrderUpdateRequest.map(OrderUpdateRequest::getProcessingDate);
    if (optProcessingDate.isPresent()) {
      supplierOrderEnriched.setScheduledProcessDate(optProcessingDate.get());
    }
    final Optional<Integer> optOrderStatusId =
        optionalOrderUpdateRequest.map(OrderUpdateRequest::getOrderStatusId);
    if (optOrderStatusId.isPresent()) {
      supplierOrder.setOrderStatusId(optOrderStatusId.get());
    }
    supplierOrder.setLastModifiedBy(orderUpdateRequest.getLoggedInUser());
  }

  @Override
  public void sendEventToOlcm(final List<OlcmEvent> events) {
    log.debug("Sending events to OLCM {}", events);
    if (isNotEmpty(events)) {
      events.forEach(event -> orderOutboundHandler.send(event));
      log.info("Message event sent to OLCM");
    }
  }

  private OlcmEventName getOlcmEventName(final OrderStatus orderStatus) {
    OlcmEventName eventName = null;
    switch (orderStatus) {
        // HOLD/ACTIVE -> "CANCELLED"
      case CANCELLED:
        eventName = CANCEL;
        break;

        // ACTIVE -> "HOLD"
      case ON_HOLD:
        eventName = ONHOLD;
        break;

        // HOLD/ACTIVE -> "READY TO RELEASE"
      case READY_TO_RELEASE:
        eventName = READYTORELEASE_MNL;
        break;

      default:
        log.debug("Not applicable Olcm event for order status {}.", orderStatus);
        break;
    }
    log.debug(
        "getOlcmEventName::[OrderStatus: {}], Resulted in OLCM eventName: [{}]",
        orderStatus,
        eventName);
    return eventName;
  }

  private Boolean updateSupplierOrderLines(
      final Map<Integer, ProductUpdateRequest> itemRequestMap,
      final SupplierOrder supplierOrder,
      final AuditOrder auditOrder,
      final String userId) {
    final AtomicBoolean isAnyOrderLineUpdated = new AtomicBoolean(FALSE);
    final AuditOrderLines auditOrderLines = new AuditOrderLines();
    log.info("Updating Order Lines");
    supplierOrder
        .getOrderLines()
        .forEach(
            line -> {
              final ProductUpdateRequest itemRequest = itemRequestMap.get(line.getLineNbr());
              if (itemRequest != null) {
                final Integer currentItemQty = line.getOrderLineEnriched().getCurrentItemQty();
                final int updatedQty =
                    Integer.signum(itemRequest.getQuantity()) == -1 ? 0 : itemRequest.getQuantity();
                line.getOrderLineEnriched().setCurrentItemQty(updatedQty);
                line.getOrderLineEnriched().setUserAdjustedItemQty(updatedQty);
                AuditOrderLine auditOrderLine = null;
                if (currentItemQty != updatedQty) {
                  auditOrderLine = buildAuditOrderLine(line, itemRequest.getItemChangeReasonId());
                  auditOrderLine.setProductQty(updatedQty);
                }
                createOrderLineAdjustments(
                    line, updatedQty, AdjustmentReason.MNG_ORDER.getCode(), userId);

                if (nonNull(auditOrderLine)) {
                  auditOrderLines.getAuditOrderLine().add(auditOrderLine);
                }
                isAnyOrderLineUpdated.set(TRUE);
              }
            });
    if (CollectionUtils.isNotEmpty(auditOrderLines.getAuditOrderLine())) {
      auditOrder.setAuditOrderLines(auditOrderLines);
    }
    return isAnyOrderLineUpdated.get();
  }

  private AuditOrderLine buildAuditOrderLine(
      final com.scoperetail.order.persistence.entity.OrderLine line,
      final Integer itemChangeReasonId) {
    final AuditOrderLine auditOrderLine = new AuditOrderLine();
    auditOrderLine.setLineNumber(line.getLineNbr());
    auditOrderLine.setOriginalProductId(BigInteger.valueOf(line.getOrigProductId()));
    auditOrderLine.setOrderedProductQty(line.getOrderedItemQty());
    auditOrderLine.setChangeReasonId(itemChangeReasonId);
    return auditOrderLine;
  }

  private void createOrderLineAdjustments(
      final com.scoperetail.order.persistence.entity.OrderLine ol,
      final int updatedQty,
      final int adjustmentReasonId,
      final String userId) {
    log.info("Creating Order Lines Adjustments for Order " + ol.getSupplierOrder().getOrderId());
    final Set<OrderLineAdjustment> orderLineAdjustments = ol.getOrderLineAdjustments();
    final OrderLineAdjustment orderLineAdjustment =
        OrderLineAdjustment.builder()
            .adjustmentItemQty(updatedQty)
            .adjustmentReasonId(adjustmentReasonId)
            .orderLine(ol)
            .supplierOrder(ol.getSupplierOrder())
            .build();
    orderLineAdjustment.setCreatedBy(userId);
    orderLineAdjustments.add(orderLineAdjustment);
  }

  private void executeEnrichment(
      final Set<String> enrichmentsToBeRun, final SupplierOrder supplierOrder) {
    final OrderException orderException =
        createOrderException(supplierOrder.getCustOrderId(), supplierOrder.getOrderId());

    final List<String> capabilityList =
        capabilityRepository
            .findCapabiltyByEnrichSequence(
                supplierOrder.getOrderTypeId(),
                supplierOrder.getSupplierTypeId(),
                supplierOrder.getOrderCustomer().getCustTypeId())
            .stream()
            .map(Capability::getCapabilityInterface)
            .collect(Collectors.toList());

    enrichmentsToBeRun.forEach(
        enrichBean -> {
          if (capabilityList.contains(enrichBean)) {
            log.info("Executing synchronous enrichment {}", enrichBean);
            final EnrichCommand capabilitySequenceBean =
                (EnrichCommand) applicationContext.getBean(enrichBean);
            final Result<OrderEnrichResult> res = capabilitySequenceBean.execute(supplierOrder);
            createOrderLineExceptions(orderException, res);
          }
        });
    try {
      sendErrorLog(orderException);
      log.debug("Error log data {} sent.", JsonUtil.toJson(orderException));
    } catch (final Exception e) {
      log.error("Problem in error logging for {} with exception: {}", ERROR_LOG_TARGET, e);
    }
  }

  private void createOrderLineExceptions(
      final OrderException orderException, final Result<OrderEnrichResult> oer) {
    oer.getData()
        .ifPresent(
            orderEnrichResult -> {
              if (CollectionUtils.isNotEmpty(orderEnrichResult.getOrderLines())) {
                orderEnrichResult
                    .getOrderLines()
                    .stream()
                    .forEach(
                        orderLine -> {
                          if (CollectionUtils.isNotEmpty(orderLine.getErrorCodes())) {
                            final OrderLineException orderLineException =
                                createOrderLineException(
                                    orderLine.getOrderLineId(),
                                    orderLine.getProductId(),
                                    createExceptionLog(orderLine.getErrorCodes()));
                            if (orderException.getOrderLineExceptions() == null) {
                              orderException.setOrderLineExceptions(new OrderLineExceptions());
                            }
                            orderException
                                .getOrderLineExceptions()
                                .getOrderLineException()
                                .add(orderLineException);
                          }
                        });
              }
            });
  }

  private void sendErrorLog(final OrderException orderException) throws Exception {
    log.debug("Sending error target {}", ERROR_LOG_TARGET);
    if (OrderExceptionBuilder.preapareOrderException(orderException, OrderStatus.NEW.getCode())) {
      final OrderExceptions exceptions = new OrderExceptions();
      exceptions.getOrderException().add(orderException);
      exceptionMessageClient.log(exceptions, ERROR_LOG_TARGET);
      log.debug("Error logging complete");
    }
  }

  @Transactional
  @Override
  public void setIsLockedForEdit(final List<Integer> orderIds, final Character isLocked) {
    log.info("Set lock on Supplier Orders for edit : " + orderIds);
    supplierOrderRepository.setIsLockedForEdit(orderIds, isLocked);
  }

  private boolean isAnyOrderLockedForEdit(final List<SupplierOrder> supplierOrders) {
    return supplierOrders
        .stream()
        .filter(
            order ->
                CHAR_Y.equals(order.getIsLockedForEdit())
                    || !(validOrderStatuses.contains(order.getOrderStatusId()))
                    || (EnrichmentStatus.IN_PROGRESS
                        .getLabel()
                        .equals(order.getEnrichmentStatus())))
        .findFirst()
        .isPresent();
  }
}
