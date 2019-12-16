package com.scoperetail.supplier.order.processor.query.resources;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.scoperetail.order.processor.config.AbstractBaseTest;

public class ReferenceDataQryControllerTest extends AbstractBaseTest {
  public static final String APPLICATION = "application";
  public static final String UTF_8 = "utf8";
  public static final String JSON = "json";

  @Test
  public void getAllOrderTypeTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order/ordertypes"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }

  @Test
  public void getAllOrderStatusTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order/orderstatus"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }

  @Test
  public void getAllLineStatusTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order/linestatus"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }

  @Test
  public void getAllChangeReasonTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order/changereason"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }
}
