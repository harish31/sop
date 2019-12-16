package com.scoperetail.supplier.order.processor.command.handler.impl;

import static com.scoperetail.commons.enums.Attribute.ORDER_STATUS;
import static com.scoperetail.commons.enums.ErrorCode.ORDER_RELEASE_FAILED;
import static com.scoperetail.commons.enums.OrderLineStatus.getLineStatus;
import static com.scoperetail.commons.enums.OrderStatus.RELEASED_FOR_FULFILLEMENT;
import static com.scoperetail.commons.util.NumberUtil.convertToBigDecimal;
import static com.scoperetail.order.persistence.entity.OrderType.TRANSFER;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.DEFAULT_SUPPLIER_PRODUCT_ID;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.ERROR_LOG_TARGET;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.SUPPLIER_SERVICE_ADDRESS_ATTRIBUTE;
import static java.util.Optional.ofNullable;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scoperetail.common.rest.client.model.customer.group.CustomerAddressDto;
import com.scoperetail.common.rest.client.model.customer.group.CustomerDto;
import com.scoperetail.common.rest.client.model.customer.group.CustomerResponse;
import com.scoperetail.common.rest.client.model.product.supplier.SupplierResponse;
import com.scoperetail.common.rest.client.model.supplier.master.Supplier;
import com.scoperetail.common.rest.client.model.supplier.master.SupplierAddress;
import com.scoperetail.common.rest.client.service.api.CustomerService;
import com.scoperetail.common.rest.client.service.api.SupplierService;
import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.enums.CustomerType;
import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.enums.SubstitutionType;
import com.scoperetail.commons.util.XMLUtil;
import com.scoperetail.internal.schema.ExceptionLog;
import com.scoperetail.internal.schema.OrderException;
import com.scoperetail.internal.schema.OrderExceptionLogs;
import com.scoperetail.internal.schema.OrderExceptions;
import com.scoperetail.logger.client.exception.impl.OrderExceptionBuilder;
import com.scoperetail.logger.client.exception.spi.ExceptionMessageClient;
import com.scoperetail.oms.schema.Adjustment;
import com.scoperetail.oms.schema.Adjustments;
import com.scoperetail.oms.schema.ObjectFactory;
import com.scoperetail.oms.schema.OrderSchedStatusCodes;
import com.scoperetail.oms.schema.OrderStatusCodes;
import com.scoperetail.oms.schema.OrderTotalType;
import com.scoperetail.oms.schema.OrderTypeCodes;
import com.scoperetail.oms.schema.OutboundCustomer;
import com.scoperetail.oms.schema.OutboundCustomerOrder;
import com.scoperetail.oms.schema.OutboundOrderHeader;
import com.scoperetail.oms.schema.OutboundOrderLine;
import com.scoperetail.oms.schema.OutboundOrderLines;
import com.scoperetail.oms.schema.OutboundSupplier;
import com.scoperetail.oms.schema.PostalAddress;
import com.scoperetail.oms.schema.Substitution;
import com.scoperetail.oms.schema.Substitutions;
import com.scoperetail.oms.schema.SupplierType;
import com.scoperetail.oms.schema.TransferOrderType;
import com.scoperetail.order.persistence.entity.OrderCustomer;
import com.scoperetail.order.persistence.entity.OrderLine;
import com.scoperetail.order.persistence.entity.OrderLineAdjustment;
import com.scoperetail.order.persistence.entity.OrderLineEnriched;
import com.scoperetail.order.persistence.entity.OrderLineSubstitution;
import com.scoperetail.order.persistence.entity.OrderType;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.entity.SupplierOrderEnriched;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.command.handler.api.PrepareReleaseCmdHandler;
import com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants;

import lombok.extern.slf4j.Slf4j;

/** TODO - remove all boilerplate code and use MapStruct framework for object to object mapping */
@Slf4j
@Service
public class PrepareReleaseCmdHandlerImpl implements PrepareReleaseCmdHandler {

  @Value("${order.outbound.xsd.validation.enable:true}")
  private boolean isXsdValidationEnabled;

  @Value("${order.outbound.order.line.statuses}")
  private List<OrderLineStatus> orderOutboundLineStatuses;

  @Value("#{'${valid.order.release.status}'.split(',')}")
  private List<Integer> validOrderReleaseStatuses;

  public static final String CORP_ID = "001";
  @Autowired private SupplierOrderRepository supplierOrderRepository;
  @Autowired private SupplierService supplierService;
  @Autowired private CustomerService customerService;
  @Autowired private Schema customerOrderOutboundSchema;
  @Autowired private ExceptionMessageClient exceptionMessageClient;

  @Override
  public OutboundCustomerOrder createOutboundOrder(final SupplierOrder order) {
    final Integer orderId = order.getOrderId();
    if (!isValidOrderStatus(order.getOrderStatusId())) {
      final OrderStatus orderStatus = OrderStatus.getOrderStatus(order.getOrderStatusId());
      log.error(
          "Order can not be sent to outbound because of invalid status: order id-{},order status id-{}",
          orderId,
          orderStatus.getLabel());
      final OrderExceptions orderExceptions =
          buildExceptionLog(order.getCustOrderId(), orderId, orderStatus.getLabel());
      try {
        exceptionMessageClient.log(orderExceptions, ERROR_LOG_TARGET);
      } catch (final Exception e) {
        log.error("Problem in error logging of order: {}, exception: {}", orderId, e);
      }
      throw new ApplicationException("Order can not be sent to outbound because of invalid status");
    }
    order.setOrderStatusId(RELEASED_FOR_FULFILLEMENT.getCode());
    final OutboundCustomerOrder customerOrder = getOutboundCustomerOrder(order);
    // Optional validation against XSD.
    validateAgainstXsd(customerOrder);
    return customerOrder;
  }

  @Override
  @Transactional
  public Optional<SupplierOrder> readSupplierOrder(final Integer orderId) {
    return supplierOrderRepository.findSequencerSupplierOrder(orderId);
  }

  @Override
  @Transactional
  public void updateSupplierOrder(final Integer orderId) {
    final SupplierOrder order = supplierOrderRepository.findById(orderId).get();
    log.debug("Order is ready for relase, we are updating to DB {}", orderId);
    order.setOrderStatusId(RELEASED_FOR_FULFILLEMENT.getCode());
    order.setSysOrdReleaseTs(LocalDateTime.now());
  }

  private void validateAgainstXsd(final OutboundCustomerOrder customerOrder) {
    if (isXsdValidationEnabled) {
      try {
        final Optional<String> outMessage =
            XMLUtil.marshell(
                OutboundCustomerOrder.class,
                new com.scoperetail.oms.schema.ObjectFactory()
                    .createCustomerOrderOutbound(customerOrder));
        log.debug("Generated Outbound Message: {}", outMessage);
        XMLUtil.isValidMessage(
            ofNullable(outMessage.get()), ofNullable(customerOrderOutboundSchema));
      } catch (final JAXBException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private OutboundCustomerOrder getOutboundCustomerOrder(final SupplierOrder order) {
    final OutboundCustomerOrder customerOrder = new OutboundCustomerOrder();
    customerOrder.setOrderHeader(getOrderHeader(order));
    customerOrder.setSupplier(getOutboundSupplier(order));
    customerOrder.setLines(getOrderLines(order));
    customerOrder.setCustomer(getCustomer(order));
    return customerOrder;
  }

  private OutboundOrderHeader getOrderHeader(final SupplierOrder order) {
    final SupplierOrderEnriched orderEnriched = order.getSupplierOrderEnriched();
    final OutboundOrderHeader outboundOrderHeader = new OutboundOrderHeader();
    outboundOrderHeader.setOrderId(String.valueOf(order.getOrderId()));
    outboundOrderHeader.setOrderTotal(getOrderTotalType(orderEnriched));
    outboundOrderHeader.setOrderTypeCode(getTypeCodes(order.getOrderTypeId()));

    if (order.getOrderTypeId().equals(TRANSFER.getCode())) {
      outboundOrderHeader.setTransferOrderType(
          TransferOrderType.fromValue(orderEnriched.getTransferTypeCd()));
    }
    outboundOrderHeader.setPoRefNbr(orderEnriched.getPoRefNbr());
    outboundOrderHeader.setRouteId(nullSafeBigInteger(orderEnriched.getRouteId()));
    //    outboundOrderHeader.setSchedCutoffDate(orderEnriched.getScheduledCutoffDate());
    outboundOrderHeader.setSchedCutoffTime(orderEnriched.getScheduledCutoffTime());
    outboundOrderHeader.setSchedDeliveryDate(orderEnriched.getScheduledDeliveryDate());

    Optional.ofNullable(orderEnriched.getScheduledProcessDate())
        .map(localDate -> localDate.atTime(LocalTime.now()))
        .ifPresent(schedProcessDate -> outboundOrderHeader.setSchedProcessDate(schedProcessDate));
    outboundOrderHeader.setSchedReleaseDate(orderEnriched.getScheduledReleaseDate());
    outboundOrderHeader.setSchedReleaseTime(orderEnriched.getScheduledReleaseTime());

    final LocalDate cutOffDate =
        Optional.ofNullable(orderEnriched.getScheduledCutoffDate())
            .orElse(outboundOrderHeader.getSchedProcessDate().toLocalDate());

    outboundOrderHeader.setSchedCutoffDate(cutOffDate);

    // TODO -find mapping of codes
    outboundOrderHeader.setSchedStatusCode(OrderSchedStatusCodes.SCHEDULED);
    outboundOrderHeader.setSrcOrderCreateTimestamp(order.getCreateTs());
    outboundOrderHeader.setSrcOrderId(orderEnriched.getSrcOrderId());
    outboundOrderHeader.setSrcOrderId2(orderEnriched.getSrcOrderId2());
    outboundOrderHeader.setStatusCode(getStatusCodes(order.getOrderStatusId()));
    outboundOrderHeader.setStopId(nullSafeBigInteger(orderEnriched.getStopId()));
    Optional.ofNullable(order.getFutureOrder())
        .map(String::valueOf)
        .ifPresent(outboundOrderHeader::setOrderHoldForFuture);
    return outboundOrderHeader;
  }

  private OrderTypeCodes getTypeCodes(final Integer orderTypeId) {
    return OrderTypeCodes.fromValue(OrderType.getOrderType(orderTypeId).name());
  }

  private OrderStatusCodes getStatusCodes(final Integer orderStatusId) {
    return OrderStatusCodes.fromValue(OrderStatus.getOrderStatus(orderStatusId).name());
  }

  private OrderTotalType getOrderTotalType(final SupplierOrderEnriched orderEnriched) {
    final OrderTotalType orderTotalType = new OrderTotalType();
    orderTotalType.setTotalCubeQuantity(orderEnriched.getTotalCubeVolume());
    orderTotalType.setTotalItemQuantity(nullSafeBigInteger(orderEnriched.getTotalItemQty()));
    orderTotalType.setTotalItemWeight(orderEnriched.getTotalItemWgt());
    orderTotalType.setTotalPalletQuantity(orderEnriched.getTotalPalletQty());
    orderTotalType.setTotalCaseQuantity(orderEnriched.getTotalCaseQuantity());
    return orderTotalType;
  }

  private OutboundOrderLines getOrderLines(final SupplierOrder order) {
    final Collection<OrderLine> orderLines = order.getOrderLines();
    final OutboundOrderLines outboundOrderLines = new OutboundOrderLines();
    int lineCounter = 1;
    for (final OrderLine orderLine : orderLines) {
      if (orderOutboundLineStatuses.contains(getLineStatus(orderLine.getOrderLineStatusId()))) {
        final OrderLineEnriched orderLineEnriched = orderLine.getOrderLineEnriched();
        final OutboundOrderLine outboundOrderLine = new OutboundOrderLine();
        outboundOrderLine.setLineNumber(lineCounter++);
        // outboundOrderLine.setChangeReasonCode(String.valueOf(orderLine.getChangeReasonId()));
        outboundOrderLine.setLineStatusCode(
            OrderLineStatus.getLineStatus(orderLine.getOrderLineStatusId()).name());
        outboundOrderLine.setAdjustments(getAdjustments(orderLine));
        outboundOrderLine.setSupplierProductId(
            ofNullable(StringUtils.stripToNull(orderLineEnriched.getSupplierProductId()))
                .orElse(DEFAULT_SUPPLIER_PRODUCT_ID));
        // TODO -find mapping of BuyerName
        outboundOrderLine.setBuyerName(null);
        outboundOrderLine.setCaseUpc(orderLineEnriched.getCaseUpc());
        outboundOrderLine.setItemCubeVolume(orderLineEnriched.getItemCubeVolume());
        outboundOrderLine.setOrderedItemQuantity(nullSafeBigInteger(orderLine.getOrderedItemQty()));
        outboundOrderLine.setAdjItemQuantity(
            convertToBigDecimal(orderLineEnriched.getCurrentItemQty()));
        if (orderLineEnriched.getSubstitutionType() != null) {
          final SubstitutionType substitutionType =
              SubstitutionType.getSubstitutionTypeByCode(orderLineEnriched.getSubstitutionType());
          outboundOrderLine.setSubType(
              com.scoperetail.oms.schema.SubstitutionType.fromValue(substitutionType.getLabel()));
        }
        // TODO -find mapping of UomCodeQty
        //          outboundOrderLine.setOrderedItemQuantityUom(
        //              UomCodeQty.fromValue(orderLine.getOrderedUom()));
        outboundOrderLine.setOrderedItemWeight(orderLine.getOrderedItemWgt());
        // TODO - find mapping of UomCodeWgt
        //          outboundOrderLine.setOrderedItemWeightUom(
        //              UomCodeWgt.fromValue(orderLine.getOrderedUom()));

        outboundOrderLine.setOrigProductId(nullSafeBigInteger(orderLine.getOrigProductId()));
        outboundOrderLine.setPalletQuantity(orderLineEnriched.getPalletQty());
        outboundOrderLine.setProductId(nullSafeBigInteger(orderLine.getProductId()));
        outboundOrderLine.setExpiredProductId(nullSafeBigInteger(orderLine.getExpiredProductId()));
        outboundOrderLine.setRetailSectionCd(orderLineEnriched.getWhseSectionCode());
        outboundOrderLine.setRetailUpc(orderLineEnriched.getRetailUpc());
        outboundOrderLine.setShipUnitPackTypeCd(orderLineEnriched.getShipUnitPackCd());
        outboundOrderLine.setSubstitutions(getSubstitutions(orderLine));
        outboundOrderLine.setSupplierUpc(orderLineEnriched.getSupplierUpc());
        outboundOrderLine.setSupplierUpcDesc(orderLineEnriched.getSupplierUpcDesc());      
        outboundOrderLine.setTotalProductWeight(orderLineEnriched.getItemUnitWt());
        outboundOrderLines.getLine().add(outboundOrderLine);
      }
    }
    return outboundOrderLines;
  }

  private Substitutions getSubstitutions(final OrderLine orderLine) {
    final List<OrderLineSubstitution> byOrderIdAndLineNbr =
        orderLine.getOrderLineSubstitutions().stream().collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(byOrderIdAndLineNbr)) {
      final Substitutions substitutions = new Substitutions();
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

  private Adjustments getAdjustments(final OrderLine orderLine) {

    final Collection<OrderLineAdjustment> orderLineAdjustments =
        orderLine.getOrderLineAdjustments();
    if (CollectionUtils.isNotEmpty(orderLineAdjustments)) {
      final Adjustments adjustments = new Adjustments();
      int seqCounter = 1;
      Integer previousQty = orderLine.getOrderedItemQty();
      for (final OrderLineAdjustment orderLineAdjustment : orderLineAdjustments) {
        final Adjustment adjustment = new Adjustment();
        adjustment.setAdjItemQuantity(
            nullSafeBigInteger(orderLineAdjustment.getAdjustmentItemQty()));
        adjustment.setReasonCode(String.valueOf(orderLineAdjustment.getAdjustmentReasonId()));
        adjustment.setExcepMiscText(orderLineAdjustment.getExceptionMiscText());
        adjustment.setCreatedBy(orderLineAdjustment.getCreatedBy());
        adjustment.setCreateTimestamp(orderLineAdjustment.getCreateTs());
        adjustment.setSeqNbr(seqCounter++);
        adjustment.setAdjItemQtyChange(
            nullSafeBigInteger(orderLineAdjustment.getAdjustmentItemQty() - previousQty));
        previousQty = orderLineAdjustment.getAdjustmentItemQty();
        adjustments.getAdjustment().add(adjustment);
      }
      return adjustments;
    }

    return null;
  }

  private OutboundSupplier getOutboundSupplier(final SupplierOrder order) {
    final OutboundSupplier outboundSupplier = new OutboundSupplier();
    outboundSupplier.setSupplierId(nullSafeBigInteger(order.getSupplierId()));
    final com.scoperetail.commons.enums.SupplierType supplierType =
        com.scoperetail.commons.enums.SupplierType.getSupplierType(order.getSupplierTypeId());
    outboundSupplier.setSupplierType(SupplierType.fromValue(supplierType.name()));
    outboundSupplier.setFullName(order.getSupplierName());
    outboundSupplier.setDistCenterId(order.getDistCenterId());
    outboundSupplier.setPhysWhseId(order.getPhysWhseId());
    outboundSupplier.setCorpId(CORP_ID);
    final Optional<SupplierResponse> suppliers =
        supplierService.getSuppliersWithAddress(
            Collections.singletonList(order.getSupplierId()),
            Collections.singletonList(SUPPLIER_SERVICE_ADDRESS_ATTRIBUTE));
    suppliers.ifPresent(
        supplierResponse ->
            supplierResponse
                .getPage()
                .getContent()
                .stream()
                .findFirst()
                .ifPresent(
                    supplier -> {
                      mapSupplier(outboundSupplier, supplier);
                    }));
    return outboundSupplier;
  }

  private void mapSupplier(final OutboundSupplier outboundSupplier, final Supplier supplier) {
    final ObjectFactory factory = new ObjectFactory();
    final PostalAddress address = factory.createPostalAddress();
    final SupplierAddress supplierAddress = supplier.getAddress();
    if (supplierAddress != null) {
      address.setAddressLine1Txt(supplierAddress.getAddress1());
      address.setAddressLine2Txt(supplierAddress.getAddress2());
      address.setStateCd(factory.createPostalAddressStateCd(supplierAddress.getState()));
      address.setCountryCd(factory.createPostalAddressCountryCd(supplierAddress.getCountryCd()));
      address.setCityName(factory.createPostalAddressCityName(supplierAddress.getCity()));
    }
    outboundSupplier.setDivisionId(supplier.getDivisionId());
    outboundSupplier.setAddress(address);
    outboundSupplier.setPaymentMethod(supplier.getPaymentMethod());
    outboundSupplier.setSupplierDunsNbr(supplier.getSuppDunsNbr());
    outboundSupplier.setTransportMode(supplier.getTransportMode());
    outboundSupplier.setB2BTransferFormat(supplier.getB2bTransferFormat());
    outboundSupplier.setEdiVendorAccntNbr(supplier.getEdiVendorAccNmbr());
    outboundSupplier.setBackdoorVendorSubAccountId(supplier.getBackDoorVendorSubAccId());
    if (supplier.getIsB2B() != null) {
      outboundSupplier.setIsB2B(String.valueOf(supplier.getIsB2B()));
    }
  }

  private OutboundCustomer getCustomer(final SupplierOrder order) {
    final OrderCustomer customer = order.getOrderCustomer();
    final OutboundCustomer outboundCustomer = new OutboundCustomer();
    outboundCustomer.setCorpId(CORP_ID);
    outboundCustomer.setDivisionId(customer.getDivisionId());
    outboundCustomer.setCustomerId(nullSafeBigInteger(customer.getCustomerId()));
    outboundCustomer.setFullName(customer.getCustomerName());
    final CustomerType customerType = CustomerType.getCustomerType(customer.getCustTypeId());
    outboundCustomer.setCustomerType(
        com.scoperetail.oms.schema.CustomerType.fromValue(customerType.name()));
    final Optional<CustomerResponse> customersWithAddress =
        customerService.getCustomersWithAddress(
            Collections.singletonList(customer.getCustomerId()));
    customersWithAddress.ifPresent(
        customerResponse -> {
          if (CollectionUtils.isNotEmpty(customerResponse.getPage().getContent())) {
            customerResponse
                .getPage()
                .getContent()
                .stream()
                .findFirst()
                .ifPresent(customerDto -> mapCustomer(customer, outboundCustomer, customerDto));
          }
        });

    return outboundCustomer;
  }

  private void mapCustomer(
      final OrderCustomer customer,
      final OutboundCustomer outboundCustomer,
      final CustomerDto customerDto) {
    final ObjectFactory factory = new ObjectFactory();
    final PostalAddress address = factory.createPostalAddress();

    final CustomerAddressDto customerAddressDto = customerDto.getAddress();
    address.setAddressLine1Txt(customerAddressDto.getAddress1());
    address.setAddressLine2Txt(customerAddressDto.getAddress2());
    address.setStateCd(factory.createPostalAddressStateCd(customerAddressDto.getState()));
    address.setCountryCd(factory.createPostalAddressCountryCd(customerAddressDto.getCountryCd()));
    address.setCityName(factory.createPostalAddressCityName(customerAddressDto.getCity()));
    outboundCustomer.setAddress(address);
    outboundCustomer.setCustomerAccountId(customerDto.getCustomerAccId());
    outboundCustomer.setCustomerType(
        com.scoperetail.oms.schema.CustomerType.fromValue(
            CustomerType.getCustomerType(customer.getCustTypeId()).name()));
    outboundCustomer.setDivisionId(customer.getDivisionId());
  }

  private void loadOrderDetails(final SupplierOrder order) {
    order.getSupplierOrderEnriched();
    order.getOrderLines();
    order.getOrderCustomer();
  }

  private BigInteger nullSafeBigInteger(final Integer integer) {
    return integer == null ? null : BigInteger.valueOf(integer);
  }

  private BigInteger nullSafeBigInteger(final Long longValue) {
    return longValue == null ? null : BigInteger.valueOf(longValue);
  }

  private boolean isValidOrderStatus(final Integer orderStatusId) {
    return validOrderReleaseStatuses.contains(orderStatusId);
  }

  private OrderExceptions buildExceptionLog(
      final Integer customerId, final Integer orderId, final String orderStatus) {
    final OrderExceptions orderExceptions = new OrderExceptions();
    final OrderExceptionLogs orderExceptionLogs = new OrderExceptionLogs();
    final ExceptionLog exceptionLog =
        OrderExceptionBuilder.createExceptionLog(ORDER_STATUS, orderStatus, ORDER_RELEASE_FAILED);
    orderExceptionLogs.getExceptionLog().add(exceptionLog);
    final OrderException orderException =
        OrderExceptionBuilder.createOrderException(customerId, orderId);
    orderException.setOrderExceptionLogs(orderExceptionLogs);
    orderExceptions.getOrderException().add(orderException);
    return orderExceptions;
  }
}
