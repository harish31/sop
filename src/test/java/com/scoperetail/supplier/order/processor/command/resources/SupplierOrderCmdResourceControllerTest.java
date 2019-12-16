package com.scoperetail.supplier.order.processor.command.resources;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.scoperetail.order.processor.config.AbstractBaseTest;

public class SupplierOrderCmdResourceControllerTest extends AbstractBaseTest {
  public static final String APPLICATION = "application";
  public static final String UTF_8 = "utf8";
  public static final String JSON = "json";

 // @Test
  public void updateSupplierOrdersTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(
            MockMvcRequestBuilders.patch("/v1/manage/orders")
                .content(
                    "[{\"orderId\":1,\"deliveryDate\":\"12/12/2018\",\"processingDate\":\"12/12/2018\",\"orderStatusId\":202,\"changeReasonId\":1,\"itemUpdateRequest\":[{\"itemNumber\":1,\"action\":\"\",\"quantity\":55,\"itemStatusId\":1,\"itemChangeReasonId\":1}]}]\r\n"
                        + "")
                .contentType(APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }

  @Test
  public void updateSupplierOrderTest() throws Exception {
    final MediaType contentType = new MediaType(APPLICATION, JSON, Charset.forName(UTF_8));
    mockMvc
        .perform(
            MockMvcRequestBuilders.patch("/v1/manage/order")
                .content(
                    "{\"orderId\":1,\"deliveryDate\":\"12/12/2018\",\"processingDate\":\"12/12/2018\",\"orderStatusId\":202,\"changeReasonId\":1,\"itemUpdateRequest\":[{\"itemNumber\":1,\"action\":\"\",\"quantity\":55,\"itemStatusId\":1,\"itemChangeReasonId\":1}]}")
                .contentType(APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(contentType));
  }
}
