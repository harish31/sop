package com.scoperetail.supplier.order.processor.query.handler.impl;

import static com.scoperetail.commons.constants.Constants.CHAR_N;
import static com.scoperetail.commons.constants.Constants.CROSS;
import static com.scoperetail.commons.constants.Constants.HYPHEN;
import static com.scoperetail.commons.constants.Constants.SLASH;
import static com.scoperetail.commons.enums.EnrichmentStatus.getEnrichmentStatus;
import static com.scoperetail.commons.enums.OrderStatus.ENRICHMENT_IN_PROGRESS;
import static com.scoperetail.commons.enums.OrderStatus.NEW;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.TOTAL_QUANTITY;
import static com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants.UI_SOURCE;
import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;

import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.enums.EnrichmentStatus;
import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.commons.enums.OrderType;
import com.scoperetail.commons.manage.order.dto.enums.OperatorType;
import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.commons.manage.order.dto.response.ChangeReasonResponse;
import com.scoperetail.commons.manage.order.dto.response.SearchItemResponse;
import com.scoperetail.commons.manage.order.dto.response.SearchOrderResponse;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.commons.response.BaseResponse.Status;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.query.mapper.SupplierOrderSearchResultMapper;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.query.handler.ReferenceDataQueryHandler;
import com.scoperetail.supplier.order.processor.query.handler.SupplierOrderQueryHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SupplierOrderQueryHandlerImpl implements SupplierOrderQueryHandler {

  @Autowired private SupplierOrderRepository supplierOrderRepository;
  @Autowired private ReferenceDataQueryHandler referenceDataQueryHandler;

  @Value("${search.by.quantity.rsc.enabled:true}")
  private boolean searchByQuantityAndRscEnabled;

  @Value("#{'${invalid.order.line.statuses}'.split(',')}")
  private List<Integer> invalidOrderLineStatuses;

  @Override
  public BaseResponse getSupplierOrders(
      final SupplierOrderSearchRequest supplierOrderSearchRequest, final Pageable pageable) {

    boolean isSearchByProductOrBicOrRsc =
        supplierOrderSearchRequest.getProductId() != null
            || supplierOrderSearchRequest.getSupplierProductId() != null
            || supplierOrderSearchRequest.getRetailSectionCode() != null;

    Page<SupplierOrder> supplierOrderPage =
        searchSupplierOrders(supplierOrderSearchRequest, pageable);

    if (supplierOrderPage == null) {
      return new BaseResponse(Status.FAILURE, "No records found!");
    }

    final Page<SearchOrderResponse> responsePage =
        supplierOrderPage.map(
            supplierOrder ->
                SearchOrderResponse.builder()
                    .orderId(supplierOrder.getOrderId())
                    .supplier(
                        +supplierOrder.getSupplierId() + HYPHEN + supplierOrder.getSupplierName())
                    .supplierId(supplierOrder.getSupplierId())
                    .orderType(OrderType.getOrderType(supplierOrder.getOrderTypeId()).getLabel())
                    .status(getOrderStatus(supplierOrder))
                    .customerId(supplierOrder.getOrderCustomer().getCustomerId())
                    .customer(
                        supplierOrder.getOrderCustomer().getCustomerId()
                            + HYPHEN
                            + supplierOrder.getOrderCustomer().getCustomerName())
                    .customerGroupId(supplierOrder.getOrderCustomer().getCogId())
                    .customerGroup(supplierOrder.getOrderCustomer().getCogLabel())
                    .divisionId(supplierOrder.getOrderCustomer().getDivisionId())
                    .deliveryDate(
                        supplierOrder.getSupplierOrderEnriched().getScheduledDeliveryDate())
                    .processingDate(
                        supplierOrder.getSupplierOrderEnriched().getScheduledProcessDate())
                    .createdOn(supplierOrder.getCreateTs().toLocalDate())
                    .totalQty(getTotalQuantity(supplierOrder, isSearchByProductOrBicOrRsc))
                    .isLockedForEdit(
                        isNull(supplierOrder.getIsLockedForEdit())
                            ? CHAR_N
                            : supplierOrder.getIsLockedForEdit())
                    .build());
    log.info("Found : " + responsePage.getContent().size() + " matching supplier orders");
    return new BaseResponse(responsePage);
  }

  private String getOrderStatus(final SupplierOrder supplierOrder) {
    String orderStatus = null;
    // Enrichment status would be NULL for old orders only.
    if (isNull(supplierOrder.getEnrichmentStatus())) {
      orderStatus = OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId()).getLabel();
    } else {
      final EnrichmentStatus enrichmentStatus =
          getEnrichmentStatus(supplierOrder.getEnrichmentStatus());
      switch (enrichmentStatus) {
        case COMPLETED:
          orderStatus = OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId()).getLabel();
          break;
        case IN_PROGRESS:
          orderStatus = ENRICHMENT_IN_PROGRESS.getLabel();
          break;
        case NOT_STARTED:
        default:
          orderStatus = NEW.getLabel();
          break;
      }
    }
    return orderStatus;
  }

  @Override
  public Page<SupplierOrder> searchSupplierOrders(
      final SupplierOrderSearchRequest supplierOrderSearchRequest, final Pageable pageable) {
    return searchByQuantityAndRscEnabled
        ? searchSupplierOrdersV2(supplierOrderSearchRequest, pageable)
        : searchSupplierOrdersV1(supplierOrderSearchRequest, pageable);
  }

  private Page<SupplierOrder> searchSupplierOrdersV1(
      final SupplierOrderSearchRequest supplierOrderSearchRequest, final Pageable pageable) {
    log.info("Retrieving Supplier Orders ");
    Page<SupplierOrder> supplierOrderPage = null;

    if (supplierOrderSearchRequest.getProductId() != null
        || supplierOrderSearchRequest.getSupplierProductId() != null) {
      final Optional<List<Integer>> optionalSupplierOrderIds =
          supplierOrderRepository.getSupplierOrderIds(
              supplierOrderSearchRequest.getProductId(), supplierOrderSearchRequest.getSupplierProductId());
      if (optionalSupplierOrderIds.isPresent()) {
        final List<Integer> orderIds = optionalSupplierOrderIds.get();
        if (supplierOrderSearchRequest.getOrderId() != null) {
          if (orderIds.contains(supplierOrderSearchRequest.getOrderId())) {
            supplierOrderPage =
                supplierOrderRepository.getSupplierOrders(
                    Arrays.asList(supplierOrderSearchRequest.getOrderId()),
                    supplierOrderSearchRequest,
                    pageable);
          }
        } else {
          supplierOrderPage =
              supplierOrderRepository.getSupplierOrders(
                  orderIds, supplierOrderSearchRequest, pageable);
        }
      } else {
        log.info("No Matching Supplier Orders found!");
      }
    } else {
      supplierOrderPage =
          supplierOrderRepository.getSupplierOrders(supplierOrderSearchRequest, pageable);
    }
    return supplierOrderPage;
  }

  private Page<SupplierOrder> searchSupplierOrdersV2(
      final SupplierOrderSearchRequest searchRequest, Pageable pageable) {
    log.info("Retrieving Supplier Orders ");
    processOperator(searchRequest);
    Page<SupplierOrder> supplierOrderPage = null;
    // If productId or SupplierProductId or retail section code is present then use the new query
    if (searchRequest.getProductId() != null
        || searchRequest.getSupplierProductId() != null
        || searchRequest.getRetailSectionCode() != null) {

      // Create new Pageable with JpaSort unsafe if sort on totalQty is enabled
      pageable = getUpdatedPageable(pageable);

      Page<SupplierOrderSearchResultMapper> supplierOrderTotalQuantity =
          supplierOrderRepository.getSupplierOrderCalQuantity(
              searchRequest, invalidOrderLineStatuses, pageable);
      if (supplierOrderTotalQuantity != null && supplierOrderTotalQuantity.getTotalElements() > 0) {
        Map<Integer, Long> orderQuantityMap =
            supplierOrderTotalQuantity
                .getContent()
                .stream()
                .collect(
                    Collectors.toMap(
                        SupplierOrderSearchResultMapper::getOrderId,
                        SupplierOrderSearchResultMapper::getTotalQuantity,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        Map<Integer, SupplierOrder> supplierOrdersByIdMap =
            supplierOrderRepository
                .getByOrderIds(orderQuantityMap.keySet())
                .stream()
                .collect(Collectors.toMap(SupplierOrder::getOrderId, c -> c));

        List<SupplierOrder> supplierOrders = new ArrayList<>();
        orderQuantityMap
            .entrySet()
            .stream()
            .forEach(
                entry -> {
                  SupplierOrder supplierOrder = supplierOrdersByIdMap.get(entry.getKey());
                  supplierOrder.setTotalQuantity(entry.getValue());
                  supplierOrders.add(supplierOrder);
                });
        supplierOrderPage =
            new PageImpl<>(supplierOrders, pageable, supplierOrderTotalQuantity.getTotalElements());
      }
    } else {
      supplierOrderPage = supplierOrderRepository.getSupplierOrdersV2(searchRequest, pageable);
    }
    return supplierOrderPage;
  }

  private Pageable getUpdatedPageable(Pageable pageable) {
    Optional<Order> sortOnTotalQty =
        pageable
            .getSort()
            .stream()
            .filter(sortCriteria -> TOTAL_QUANTITY.equalsIgnoreCase(sortCriteria.getProperty()))
            .findAny();

    return sortOnTotalQty.isPresent()
        ? PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            JpaSort.unsafe(sortOnTotalQty.get().getDirection(), "(totalQuantity)"))
        : pageable;
  }

  @Override
  public SearchOrderResponse getSupplierOrder(final Integer orderId, final String source) {
    log.info("Retrieving Supplier Order : " + orderId);
    SearchOrderResponse searchOrderResponse = new SearchOrderResponse();
    final Optional<SupplierOrder> optionalSupplierOrder = supplierOrderRepository.findById(orderId);

    if (optionalSupplierOrder.isPresent()) {
      final SupplierOrder supplierOrder = optionalSupplierOrder.get();
      final List<ChangeReasonResponse> changeReasons =
          referenceDataQueryHandler.getAllChangeReason();
      final Map<Integer, ChangeReasonResponse> changeReasonMap =
          changeReasons
              .stream()
              .collect(Collectors.toMap(ChangeReasonResponse::getChangeReasonId, cr -> cr));
      final String orderchangeReason =
          changeReasonMap.get(supplierOrder.getChangeReasonId()) != null
              ? changeReasonMap.get(supplierOrder.getChangeReasonId()).getDescription()
              : null;

      searchOrderResponse =
          SearchOrderResponse.builder()
              .orderId(supplierOrder.getOrderId())
              .customerOrderId(supplierOrder.getCustOrderId())
              .divisionId(supplierOrder.getOrderCustomer().getDivisionId())
              .customerId(supplierOrder.getOrderCustomer().getCustomerId())
              .supplierId(supplierOrder.getSupplierId())
              .supplier(
                  supplierOrder.getSupplierTypeId()
                      + SLASH
                      + supplierOrder.getSupplierId()
                      + SLASH
                      + supplierOrder.getSupplierName())
              .srcOrderId2(supplierOrder.getSupplierOrderEnriched().getSrcOrderId2())
              .palletQty(supplierOrder.getSupplierOrderEnriched().getTotalItemCntrPallet())
              .orderQty(supplierOrder.getSupplierOrderEnriched().getTotalItemQty())
              .orderWt(supplierOrder.getSupplierOrderEnriched().getTotalItemCntrWgt())
              .orderVolume(supplierOrder.getSupplierOrderEnriched().getTotalItemCntrCubeVolume())
              .orderType(OrderType.getOrderType(supplierOrder.getOrderTypeId()).getLabel())
              .status(
                  UI_SOURCE.equalsIgnoreCase(source)
                      ? getOrderStatus(supplierOrder)
                      : OrderStatus.getOrderStatus(supplierOrder.getOrderStatusId()).getLabel())
              .createdOn(supplierOrder.getSupplierOrderEnriched().getCreateTs().toLocalDate())
              .createdTimeStamp(supplierOrder.getCreateTs())
              .processingDate(supplierOrder.getSupplierOrderEnriched().getScheduledProcessDate())
              .deliveryDate(supplierOrder.getSupplierOrderEnriched().getScheduledDeliveryDate())
              .createdBy(supplierOrder.getCreatedBy())
              .scheduledCutOffTime(
                  supplierOrder.getSupplierOrderEnriched().getScheduledCutoffTime())
              .routeId(supplierOrder.getSupplierOrderEnriched().getRouteId())
              .routeCode(supplierOrder.getSupplierOrderEnriched().getRouteCode())
              .srcOrderCreateTs(supplierOrder.getSupplierOrderEnriched().getSrcOrderCreateTs())
              .autoAllocationStartTime(supplierOrder.getSupplierOrderEnriched().getStartTime())
              .autoAllocationEndTime(supplierOrder.getSupplierOrderEnriched().getEndTime())
              .changeReason(orderchangeReason)
              .isLockedForEdit(
                  isNull(supplierOrder.getIsLockedForEdit())
                      ? CHAR_N
                      : supplierOrder.getIsLockedForEdit())
              .build();

      final List<SearchItemResponse> searchItemResponses = new ArrayList<>();
      supplierOrder
          .getOrderLines()
          .forEach(
              line -> {
                final String linechangeReason =
                    changeReasonMap.get(line.getChangeReasonId()) != null
                        ? changeReasonMap.get(line.getChangeReasonId()).getDescription()
                        : null;
                final SearchItemResponse searchItemResponse =
                    SearchItemResponse.builder()
                        .lineNumber(line.getLineNbr())
                        .itemNumber(line.getProductId())
                        .origItemNumber(line.getOrigProductId())
                        .itemStatus(
                            OrderLineStatus.getLineStatus(line.getOrderLineStatusId()).getLabel())
                        .description(line.getOrderLineEnriched().getProductDesc())
                        .size(line.getOrderLineEnriched().getShipUnitPackQty())
                        .pack(line.getOrderLineEnriched().getPackDesc())
                        .tixhi(
                        		line.getOrderLineEnriched().getPalletLayer()
                                + CROSS
                                + line.getOrderLineEnriched().getPalletHeight())
                        .originalQuantity(line.getOrderedItemQty())
                        .quantity(line.getOrderLineEnriched().getCurrentItemQty())
                        .palletQuantity(line.getOrderLineEnriched().getPalletQty())
                        .supplierProductId(line.getOrderLineEnriched().getSupplierProductId())
                        .itemChangeReason(linechangeReason)
                        .build();
                searchItemResponses.add(searchItemResponse);
              });
      searchOrderResponse.setSearchItemResponses(searchItemResponses);
      log.info("Found : " + searchItemResponses.size() + " items under supplier order " + orderId);
    }
    return searchOrderResponse;
  }

  private void processOperator(final SupplierOrderSearchRequest supplierOrderSearchRequest) {
    OperatorType operatorType = supplierOrderSearchRequest.getOperator();
    if (operatorType != null && supplierOrderSearchRequest.getTotalQuantity() != null) {
      switch (operatorType) {
        case EQUAL:
          supplierOrderSearchRequest.setEq(OperatorType.EQUAL);
          break;
        case LESS_THAN:
          supplierOrderSearchRequest.setLt(OperatorType.LESS_THAN);
          break;
        case LESS_THAN_EQUAL_TO:
          supplierOrderSearchRequest.setLte(OperatorType.LESS_THAN_EQUAL_TO);
          break;
        case GREATER_THAN:
          supplierOrderSearchRequest.setGt(OperatorType.GREATER_THAN);
          break;
        case GREATER_THAN_EQUAL_TO:
          supplierOrderSearchRequest.setGte(OperatorType.GREATER_THAN_EQUAL_TO);
          break;
        default:
          throw new ApplicationException("Operator " + operatorType + " not supported");
      }
    } else {
      supplierOrderSearchRequest.setTotalQuantity(0L);
    }
  }

  private Integer getTotalQuantity(final SupplierOrder supplierOrder, boolean useCalculated) {
    if (useCalculated) {
      return supplierOrder.getTotalQuantity() != null
          ? supplierOrder.getTotalQuantity().intValue()
          : null;
    } else {
      return supplierOrder.getSupplierOrderEnriched().getTotalItemQty();
    }
  }
}
