package com.scoperetail.order.processor.command.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.scoperetail.commons.enums.OrderStatus;
import com.scoperetail.order.processor.config.AbstractBaseTest;
import com.scoperetail.supplier.order.processor.command.handler.impl.SupplierOrderCmdHandlerImpl;

class SupplierOrderCmdHandlerImplTest extends AbstractBaseTest {

  @Autowired private SupplierOrderCmdHandlerImpl cmdHandlerImpl;
  @Autowired private Environment env;
  

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void test_only_PDchanged() {
    final Boolean pdchanged= true;
    final Boolean statuschanged= false;
    final Boolean linesUpdated=false;
    final OrderStatus currentStatus= OrderStatus.ON_HOLD;
    final OrderStatus newStatus= OrderStatus.ACTIVE;   
    final StringBuilder enrichmnetExecutionType= new StringBuilder();
    
    
    final Set<String> enrichmentsToBeRun =
        cmdHandlerImpl.identifyEnrichments(
            true, false, OrderStatus.ON_HOLD, OrderStatus.ACTIVE, false, enrichmnetExecutionType);

    Assertions.assertEquals("ASYNC",enrichmnetExecutionType.toString());
    
//    env.getProperty("")
//    Assertions.assertEquals(, actual);
  }
}
