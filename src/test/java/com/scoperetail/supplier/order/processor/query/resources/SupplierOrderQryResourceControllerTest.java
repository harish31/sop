package com.scoperetail.supplier.order.processor.query.resources;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.scoperetail.order.processor.config.AbstractBaseTest;

public class SupplierOrderQryResourceControllerTest extends AbstractBaseTest {

  public static final String APPLICATION = "application";
  public static final String UTF_8 = "utf8";
  public static final String JSON = "json";
  private static final String JSON_PATH = "json/";

  @Test
  public void updateSupplierOrdersTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    final String searchOrderReqjson = getJsonData(JSON_PATH + "customerOrder.json");
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/manage/order/search")
                .content(searchOrderReqjson)
                .contentType(APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }

  @Test
  public void getSupplierOrder() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/manage/order/1").contentType(APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }
}
