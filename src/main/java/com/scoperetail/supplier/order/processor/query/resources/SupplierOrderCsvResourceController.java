package com.scoperetail.supplier.order.processor.query.resources;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.scoperetail.commons.manage.order.dto.request.SupplierOrderSearchRequest;
import com.scoperetail.supplier.order.processor.query.handler.SupplierOrderCsvHandler;
import com.scoperetail.supplier.order.processor.query.model.SearchOrderResponseCsv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

@RestController
@CrossOrigin
public class SupplierOrderCsvResourceController {

  public static final DateTimeFormatter FORMATTER =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendValue(ChronoField.YEAR, 4)
          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
          .appendValue(ChronoField.DAY_OF_MONTH, 2)
          .appendLiteral('_')
          .appendValue(ChronoField.HOUR_OF_DAY, 2)
          .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
          .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
          .toFormatter();
  private static ObjectWriter csvObjectWriter;

  static {
    CsvMapper mapper = new CsvMapper();
    CsvSchema schema = mapper.schemaFor(SearchOrderResponseCsv.class).withHeader();
    csvObjectWriter = mapper.writer(schema);
  }

  @Autowired private SupplierOrderCsvHandler supplierOrderCsvHandler;

  @PostMapping(
      path = "/v1/manage/order/search/download",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public void downloadSupplierOrdersCsv(
      HttpServletResponse response,
      @Valid @RequestBody SupplierOrderSearchRequest supplierOrderSearchRequest,
      Pageable pageable)
      throws IOException {
    response.setHeader(
        "Access-Control-Allow-Headers",
        "Content-Disposition");

    final String fileName = generateFileName();
    response.setHeader("Content-Disposition", fileName);
    response.setContentType("text/csv;filename="+fileName);
    Page<SearchOrderResponseCsv> searchOrderResponseCsvPage =
        supplierOrderCsvHandler.getSearchOrderResponseCsv(supplierOrderSearchRequest, pageable);
    List<SearchOrderResponseCsv> searchOrderResponseCsvList =
        searchOrderResponseCsvPage.getContent();
    csvObjectWriter.writeValue(response.getWriter(), searchOrderResponseCsvList);
    response.flushBuffer();
  }

  /**
   * Generate filename to be set in http response header Ex: COSMOS_20190118_072010.csv
   *
   * @return
   */
  private static String generateFileName() {
    return "attachment; filename=COSMOS_" + LocalDateTime.now().format(FORMATTER) + ".csv";
  }
}
