package com.scoperetail.order.processor.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import com.scoperetail.supplier.order.processor.SupplierOrderProcessorApplication;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {SupplierOrderProcessorApplication.class, H2TestProfileJPAConfig.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractBaseTest {

  protected MockMvc mockMvc;
  @Autowired private WebApplicationContext webApplicationContext;

  @BeforeEach
  public void setupMockMvc() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  protected String getJsonData(final String jsonFilePath) throws IOException {
    final File file = ResourceUtils.getFile("classpath:" + jsonFilePath);
    return new String(Files.readAllBytes(file.toPath()));
  }
}
