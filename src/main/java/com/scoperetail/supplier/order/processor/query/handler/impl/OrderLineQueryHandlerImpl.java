package com.scoperetail.supplier.order.processor.query.handler.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.scoperetail.common.rest.client.service.api.ProductService;
import com.scoperetail.commons.enums.ErrorCode;
import com.scoperetail.commons.enums.OrderLineStatus;
import com.scoperetail.commons.enums.SubstitutionType;
import com.scoperetail.commons.orderLine.search.dto.request.OrderDetailsFinderRequest;
import com.scoperetail.commons.orderLine.search.dto.request.OrderLineSearchRequest;
import com.scoperetail.commons.response.BaseResponse;
import com.scoperetail.order.persistence.entity.AdjustmentReason;
import com.scoperetail.order.persistence.query.mapper.AdjustedOrderLineSearchMapper;
import com.scoperetail.order.persistence.query.mapper.OrderedLineDetailsDto;
import com.scoperetail.order.persistence.query.mapper.RejectedOrderLineSearchMapper;
import com.scoperetail.order.persistence.query.mapper.SubstitutedOrderLineSearchMapper;
import com.scoperetail.order.persistence.repository.AdjustmentReasonRepository;
import com.scoperetail.order.persistence.repository.OrderLineRepository;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants;
import com.scoperetail.supplier.order.processor.query.handler.OrderLineQueryHandler;
import com.scoperetail.supplier.order.processor.query.model.AdjustedLinesSearchResponse;
import com.scoperetail.supplier.order.processor.query.model.AdjustmentReasonsResponse;
import com.scoperetail.supplier.order.processor.query.model.RejectedLinesSearchResponse;
import com.scoperetail.supplier.order.processor.query.model.SubstitutedLinesSearchResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderLineQueryHandlerImpl implements OrderLineQueryHandler {

  @Autowired private SupplierOrderRepository supplierOrderRepository;

  @Autowired private OrderLineRepository orderLineRepository;

  @Autowired private AdjustmentReasonRepository adjustmentReasonRepository;

  @Autowired private ProductService productService;

  @Value("#{'${adjustment.reasons.source.ui}'.split(',')}")
  private List<Integer> adjustmentReasonsIds;

  @Override
  public BaseResponse searchSubstitutedLines(
      OrderLineSearchRequest searchRequest, final Pageable pageable, final List<String> sortBy) {
    log.info("Fetching Substituted order lines:");

    if (CollectionUtils.isEmpty(searchRequest.getCodes())) {
      searchRequest.setCodes(
          Stream.of(SubstitutionType.values())
              .map(subType -> subType.getCode())
              .collect(Collectors.toList()));
    }

    Page<SubstitutedOrderLineSearchMapper> page =
        supplierOrderRepository.searchSubstitutedLines(searchRequest, pageable);

    Page<SubstitutedLinesSearchResponse> pageResponse = processRecords(page);

    // Sort results
    if (CollectionUtils.isNotEmpty(pageResponse.getContent())) {
      List<SubstitutedLinesSearchResponse> substitutionSearchList =
          new ArrayList<>(pageResponse.getContent());
      sortResponse(substitutionSearchList, sortBy);
      pageResponse = new PageImpl<>(substitutionSearchList, pageable, page.getTotalElements());
    }

    log.info("Found : " + pageResponse.getContent().size() + " matching order lines");
    return new BaseResponse(pageResponse);
  }

  private Page<SubstitutedLinesSearchResponse> processRecords(
      Page<SubstitutedOrderLineSearchMapper> page) {

    final List<OrderDetailsFinderRequest> orderDetailsFinderList = new ArrayList<>();

    page.getContent()
        .stream()
        .filter(mapper -> mapper.getSubsTypeId().equals(SubstitutionType.SUB_ALSO.getCode()))
        .forEach(
            record -> {
              final OrderDetailsFinderRequest dto = new OrderDetailsFinderRequest();
              dto.setOrderId(record.getOrderId());
              dto.setOriginalProductId(record.getOriginalProductId());
              orderDetailsFinderList.add(dto);
            });

    final Map<MultiKey<Long>, OrderedLineDetailsDto> multiMap = new HashMap<>();

    if (CollectionUtils.isNotEmpty(orderDetailsFinderList)) {
      // fetch Original Ordered Qty for SUB_ALSO subsType
      orderLineRepository
          .getOriginalOrderedQty(orderDetailsFinderList)
          .stream()
          .forEach(
              record -> {
                final MultiKey<Long> key =
                    new MultiKey<>(Long.valueOf(record.getOrderId()), record.getProductId());
                multiMap.put(key, record);
              });
    }

    return page.map(
        subsOrderLineSearchMapper -> {
          SubstitutedLinesSearchResponse response =
              SubstitutedLinesSearchResponse.builder()
                  .orderId(subsOrderLineSearchMapper.getOrderId())
                  .customerId(subsOrderLineSearchMapper.getCustomerId())
                  .supplierId(subsOrderLineSearchMapper.getSupplierId())
                  .subsType(
                      SubstitutionType.getSubstitutionTypeByCode(subsOrderLineSearchMapper.getSubsTypeId()).getDescription())
                  .build();
          return populateRecords(response, subsOrderLineSearchMapper, multiMap);
        });
  }

  private SubstitutedLinesSearchResponse populateRecords(
      SubstitutedLinesSearchResponse response,
      SubstitutedOrderLineSearchMapper subsOrderLineSearchMapper,
      Map<MultiKey<Long>, OrderedLineDetailsDto> multiMap) {

    switch (SubstitutionType.getSubstitutionTypeByCode(subsOrderLineSearchMapper.getSubsTypeId())) {
      case SUB_WHEN_OUT:
        response.setOriginalProductId(subsOrderLineSearchMapper.getOriginalProductId());
        response.setSubsProductId(subsOrderLineSearchMapper.getSubsProductId());
        response.setOrderedItemQty(subsOrderLineSearchMapper.getOrderedItemQty());
        response.setSubsOrderedQty(subsOrderLineSearchMapper.getSubsOrderedQty());
        response.setProductDesc(subsOrderLineSearchMapper.getProductDesc());
        response.setSubsProductDesc(subsOrderLineSearchMapper.getSubProductDesc());
        break;
      case DEAL_SUB:
      case SUB_ALWAYS:
      case USER_SUB:
        response.setOriginalProductId(subsOrderLineSearchMapper.getSubsProductId());
        response.setSubsProductId(subsOrderLineSearchMapper.getCurrentProductId());
        response.setOrderedItemQty(subsOrderLineSearchMapper.getSubsOrderedQty());
        response.setSubsOrderedQty(subsOrderLineSearchMapper.getCurrentItemQty());
        response.setProductDesc(subsOrderLineSearchMapper.getSubProductDesc());
        response.setSubsProductDesc(subsOrderLineSearchMapper.getProductDesc());
        break;
      case SUB_ALSO:
        response.setOriginalProductId(subsOrderLineSearchMapper.getOriginalProductId());
        response.setSubsProductId(subsOrderLineSearchMapper.getCurrentProductId());
        final MultiKey<Long> key =
            new MultiKey<>(
                Long.valueOf(subsOrderLineSearchMapper.getOrderId()),
                subsOrderLineSearchMapper.getOriginalProductId());
        OrderedLineDetailsDto orderedItemQtyDto = multiMap.get(key);
        response.setOrderedItemQty(orderedItemQtyDto.getOrderedItemQty());
        response.setSubsOrderedQty(subsOrderLineSearchMapper.getCurrentItemQty());
        response.setProductDesc(orderedItemQtyDto.getProductDesc());
        response.setSubsProductDesc(subsOrderLineSearchMapper.getProductDesc());
        break;
    }
    return response;
  }

  /**
   * Method to sort the given result list on the basis of the request parameter
   *
   * @param substitutedSearchList
   * @param sortBy
   */
  void sortResponse(
      List<SubstitutedLinesSearchResponse> substitutedSearchList, List<String> sortBy) {
    if (sortBy != null) {
      sortOnSingleField(substitutedSearchList, sortBy);
    } else {
      sortOnMultipleFields(substitutedSearchList);
    }
  }

  /**
   * Method used to sort the result list on default multiple fields in order of ASC of
   * supplierId,productId,customerId
   *
   * @param substitutedSearchList
   */
  private void sortOnMultipleFields(List<SubstitutedLinesSearchResponse> substitutedSearchList) {
    Comparator<SubstitutedLinesSearchResponse> comparator =
        (o1, o2) -> {
          Integer result = o1.getSupplierId().compareTo(o2.getSupplierId());
          if (result == 0) {
            result = o1.getOriginalProductId().compareTo(o2.getOriginalProductId());
            if (result == 0) {
              result = o1.getCustomerId().compareTo(o2.getCustomerId());
            }
          }
          return result;
        };
    substitutedSearchList.sort(comparator);
  }

  /**
   * Method used to sort the result list on given single search field with given order ASC or DESC
   *
   * @param substitutedSearchList
   * @param sortBy
   */
  private void sortOnSingleField(
      List<SubstitutedLinesSearchResponse> substitutedSearchList, List<String> sortBy) {
    String property = sortBy.get(0);
    String direction = sortBy.get(1);
    Comparator<SubstitutedLinesSearchResponse> comparator =
        (o1, o2) -> o1.getOriginalProductId().compareTo(o2.getOriginalProductId());
    if (property.equalsIgnoreCase(SupplierOrderConstants.ORIGINAL_PRODUCT_ID)
        && SupplierOrderConstants.DESC_ORDER.equalsIgnoreCase(direction)) {
      comparator = (o1, o2) -> o2.getOriginalProductId().compareTo(o1.getOriginalProductId());
    } else if (property.equalsIgnoreCase(SupplierOrderConstants.SUB_PRODUCT_ID)) {
      if (SupplierOrderConstants.ASC_ORDER.equalsIgnoreCase(direction)) {
        comparator = (o1, o2) -> o1.getSubsProductId().compareTo(o2.getSubsProductId());
      } else {
        comparator = (o1, o2) -> o2.getSubsProductId().compareTo(o1.getSubsProductId());
      }
    } else if (property.equalsIgnoreCase(SupplierOrderConstants.ORIGINAL_PRODUCT_DESC)) {
      if (SupplierOrderConstants.ASC_ORDER.equalsIgnoreCase(direction)) {
        comparator =
            Comparator.comparing(
                SubstitutedLinesSearchResponse::getProductDesc,
                Comparator.nullsFirst(Comparator.naturalOrder()));
      } else {
        comparator =
            Comparator.comparing(
                SubstitutedLinesSearchResponse::getProductDesc,
                Comparator.nullsLast((Comparator.reverseOrder())));
      }
    }
    substitutedSearchList.sort(comparator);
  }

  @Override
  public BaseResponse searchRejectedLines(OrderLineSearchRequest searchRequest, Pageable pageable) {
	  
    log.info("Fetching Rejected and Cancelled order lines:");

    if (CollectionUtils.isEmpty(searchRequest.getCodes())) {
      searchRequest.setCodes(null);
    }

    Page<RejectedOrderLineSearchMapper> page =
        orderLineRepository.searchRejectedLines(
            searchRequest,
            Arrays.asList(OrderLineStatus.REJECTED.getCode(), OrderLineStatus.CANCELLED.getCode()),
            pageable);

    Page<RejectedLinesSearchResponse> pageResponse =
        page.map(
            p -> {
              return RejectedLinesSearchResponse.builder()
                  .orderId(p.getOrderId())
                  .customerId(p.getCustomerId())
                  .productId(p.getProductId())
                  .prodDesc(p.getProductDesc())
                  .supplierId(p.getSupplierId())
                  .errorCode(p.getRejectReasonCode())
                  .errorDesc(
                      Optional.ofNullable(p.getRejectReasonCode()).isPresent()
                          ? ErrorCode.getErrorCode(p.getRejectReasonCode()).getLabel()
                          : null)
                  .build();
            });

    log.info("Found : " + pageResponse.getContent().size() + " matching order lines");
    return new BaseResponse(pageResponse);
  }

  @Override
  public BaseResponse searchAdjustedLines(
      OrderLineSearchRequest searchRequest, final Pageable pageable) {

    log.info("Fetching Products with change in quantity");
    if (CollectionUtils.isEmpty(searchRequest.getCodes())) {
      searchRequest.setCodes(adjustmentReasonsIds);
    }

    Page<AdjustedOrderLineSearchMapper> page =
        supplierOrderRepository.searchAdjustedLines(searchRequest, pageable);

    Page<AdjustedLinesSearchResponse> pageResponse =
        page.map(
            p -> {
              return AdjustedLinesSearchResponse.builder()
                  .orderId(p.getOrderId())
                  .customerId(p.getCustomerId())
                  .customerName(p.getCustomerName())
                  .productId(p.getProductId())
                  .prodDesc(p.getProductDesc())
                  .customerName(p.getCustomerName())
                  .supplierId(p.getSupplierId())
                  .orderedItemQty(p.getOrderedItemQty())
                  .adjItemQty(p.getAdjustmentItemQty())
                  .changeRsnCode(p.getAdjustmentReasonId())
                  .changeRsnDesc(
                     com.scoperetail.commons.enums.AdjustmentReason.getAdjustmentReasonByCode(p.getAdjustmentReasonId()).getDescription()
                          )
                  .createTs(p.getCreateTs())
                  .build();
            });

    log.info("Found : " + pageResponse.getContent().size() + " matching order lines");
    return new BaseResponse(pageResponse);
  }

  @Override
  public List<AdjustmentReasonsResponse> retrieveAdjustmentReasons(final String source) {
    log.info("Fetching adjustment reasons:");
    final List<AdjustmentReasonsResponse> adjustmentReasonsResponse =
        new ArrayList<AdjustmentReasonsResponse>();

    final List<AdjustmentReason> adjustmentReasons =
        SupplierOrderConstants.UI_SOURCE.equalsIgnoreCase(source)
            ? adjustmentReasonRepository.findAllById(adjustmentReasonsIds)
            : adjustmentReasonRepository.findAll();

    adjustmentReasons
        .stream()
        .forEach(
            adjRsn ->
                adjustmentReasonsResponse.add(
                    AdjustmentReasonsResponse.builder()
                        .adjReasonId(adjRsn.getAdjustmentReasonId())
                        .adjReasonCd(adjRsn.getAdjustmentReasonCd())
                        .adjDesc(adjRsn.getDescription())
                        .build()));
    return adjustmentReasonsResponse;
  }
  
}
