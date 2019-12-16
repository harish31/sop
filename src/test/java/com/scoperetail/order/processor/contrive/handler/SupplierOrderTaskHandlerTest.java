package com.scoperetail.order.processor.contrive.handler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import com.scoperetail.common.rest.client.service.impl.CustomerOrderServiceImpl;
import com.scoperetail.common.rest.client.service.impl.CustomerServiceImpl;
import com.scoperetail.common.rest.client.service.impl.ProductSupplierServiceImpl;
import com.scoperetail.common.rest.client.service.impl.SourceOfSupplyServiceImpl;
import com.scoperetail.common.rest.client.service.impl.SupplierServiceImpl;
import com.scoperetail.contrive.impl.ContriveManager;
import com.scoperetail.order.persistence.entity.SupplierOrder;
import com.scoperetail.order.persistence.repository.SupplierOrderRepository;
import com.scoperetail.order.processor.amq.ActiveMqHelper;
import com.scoperetail.order.processor.config.AbstractBaseTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupplierOrderTaskHandlerTest extends AbstractBaseTest {

  private static final String TEST_SUPPLIER_ORDER_OUT = "test_supplier-order-out";
  private static final String BASE_PATH = "src/test/resources/sop";
  private static final String JSON_PATH = "json/";
  private static final String HOST_URL = "http://localhost:80";

  @Autowired private ContriveManager contriveManager;
  @Autowired private CustomerOrderServiceImpl customerOrderService;
  @Autowired private CustomerServiceImpl customerService;
  @Autowired private SourceOfSupplyServiceImpl sourceOfSupplyService;
  @Autowired private ProductSupplierServiceImpl productSupplierService;
  @Autowired private SupplierServiceImpl supplierService;
  @Autowired private SupplierOrderRepository supplierOrderRepository;

//  @Test
  public void createSupplierOrderTest() throws Exception {
    setUpTestData();

    final byte[] encoded = Files.readAllBytes(Paths.get(BASE_PATH + "/customerOrder.xml"));
    final String message = new String(encoded, UTF_8);
    contriveManager.send(TEST_SUPPLIER_ORDER_OUT, message);

    Thread.sleep(5000);
    final List<SupplierOrder> supplierOrders = supplierOrderRepository.findAll();
    Assertions.assertNotNull(supplierOrders);
    Assertions.assertEquals(1, supplierOrders.size());
  }

  @Test
  public void createSupplierOrder_WithoutOrderLinesTest() throws Exception {
    mockCustomerOrder_NoOrderLines();

    final byte[] encoded = Files.readAllBytes(Paths.get(BASE_PATH + "/customerOrder.xml"));
    final String message = new String(encoded, UTF_8);
    contriveManager.send(TEST_SUPPLIER_ORDER_OUT, message);

    Thread.sleep(5000);
  }

  @Test
  public void createSupplierOrder_NoCustomerDataTest() throws Exception {
    mockCustomerOrder();
    mockCustomer_NoCustomerData();

    final byte[] encoded = Files.readAllBytes(Paths.get(BASE_PATH + "/customerOrder.xml"));
    final String message = new String(encoded, UTF_8);
    contriveManager.send(TEST_SUPPLIER_ORDER_OUT, message);

    Thread.sleep(5000);
  }

  @Test
  public void createSupplierOrder_customerWithNoCOGTest() throws Exception {
    mockCustomerOrder();
    mockCustomer_customerWithNoCOGData();

    final byte[] encoded = Files.readAllBytes(Paths.get(BASE_PATH + "/customerOrder.xml"));
    final String message = new String(encoded, UTF_8);
    contriveManager.send(TEST_SUPPLIER_ORDER_OUT, message);

    Thread.sleep(5000);
  }

//  @Test
  public void createSupplierOrder_HandleNoSupplierFoundTest() throws Exception {
    mockCustomerOrder();
    mockCustomer();
    mockSOS();
    mockSuppliers();
    mockProductSuppliers_noSupplierData();

    final byte[] encoded = Files.readAllBytes(Paths.get(BASE_PATH + "/customerOrder.xml"));
    final String message = new String(encoded, UTF_8);
    contriveManager.send(TEST_SUPPLIER_ORDER_OUT, message);

    Thread.sleep(10000);
  }

  public void setUpTestData() throws IOException {
    mockCustomerOrder();
    mockCustomer();
    mockSOS();
    mockSuppliers();
    mockProductSuppliers();
  }

  private void mockCustomerOrder() throws IOException {
    final String customerOrderData = getJsonData(JSON_PATH + "customerOrder.json");
    final RestTemplate rt = customerOrderService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer customerOrderserver = MockRestServiceServer.createServer(gateway);
    customerOrderserver
        .expect(once(), requestTo(HOST_URL + "/customer-order-processor/v1/orders/210"))
        .andRespond(withSuccess(customerOrderData, APPLICATION_JSON));
  }

  private void mockCustomerOrder_NoOrderLines() throws IOException {
    final String customerOrderData =
        getJsonData(JSON_PATH + "customerOrder_withoutOrderLines.json");
    final RestTemplate rt = customerOrderService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer customerOrderserver = MockRestServiceServer.createServer(gateway);
    customerOrderserver
        .expect(once(), requestTo(HOST_URL + "/customer-order-processor/v1/orders/210"))
        .andRespond(withSuccess(customerOrderData, APPLICATION_JSON));
  }

  private void mockCustomer() throws IOException {
    final String customerData = getJsonData(JSON_PATH + "customer.json");
    final RestTemplate rt = customerService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer customerServer = MockRestServiceServer.createServer(gateway);
    customerServer
        .expect(once(), requestTo(HOST_URL + "/customer-master/v1/customers/6/groups"))
        .andRespond(withSuccess(customerData, APPLICATION_JSON));
  }

  private void mockCustomer_NoCustomerData() throws IOException {
    final String customerData = getJsonData(JSON_PATH + "customer_noData.json");
    final RestTemplate rt = customerService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer customerServer = MockRestServiceServer.createServer(gateway);
    customerServer
        .expect(once(), requestTo(HOST_URL + "/customer-master/v1/customers/6/groups"))
        .andRespond(withSuccess(customerData, APPLICATION_JSON));
  }

  private void mockCustomer_customerWithNoCOGData() throws IOException {
    final String customerData = getJsonData(JSON_PATH + "customer_noCOG.json");
    final RestTemplate rt = customerService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer customerServer = MockRestServiceServer.createServer(gateway);
    customerServer
        .expect(once(), requestTo(HOST_URL + "/customer-master/v1/customers/6/groups"))
        .andRespond(withSuccess(customerData, APPLICATION_JSON));
  }

  private void mockSOS() throws IOException {
    final String sosData = getJsonData(JSON_PATH + "sos.json");
    final RestTemplate rt = sourceOfSupplyService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer sosServer = MockRestServiceServer.createServer(gateway);
    sosServer
        .expect(once(), requestTo(HOST_URL + "/source-of-supply/v1/sos/customerId/6/cogId/4"))
        .andRespond(withSuccess(sosData, APPLICATION_JSON));
  }

  private void mockSuppliers() throws IOException {
    final String suppliersData = getJsonData(JSON_PATH + "suppliers.json");
    final RestTemplate rt = supplierService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer supplierServer = MockRestServiceServer.createServer(gateway);
    supplierServer
        .expect(
            once(),
            requestTo(
                HOST_URL + "/supplier-master/v1/suppliers/5201,5210,5210,5210,5210,5201,5210,5210"))
        .andRespond(withSuccess(suppliersData, APPLICATION_JSON));
  }

  private void mockProductSuppliers() throws IOException {
    final String productSuppliersData = getJsonData(JSON_PATH + "product_supplier.json");
    final RestTemplate rt = productSupplierService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer productSupplierServer = MockRestServiceServer.createServer(gateway);
    productSupplierServer
        .expect(
            once(),
            requestTo(HOST_URL + "/product-supplier/v1/products/1010044/suppliers/5201,5210,5211"))
        .andRespond(withSuccess(productSuppliersData, APPLICATION_JSON));

    productSupplierServer
        .expect(
            once(),
            requestTo(HOST_URL + "/product-supplier/v1/products/70300444/suppliers/5201,5210,5211"))
        .andRespond(withSuccess(productSuppliersData, APPLICATION_JSON));
  }

  private void mockProductSuppliers_noSupplierData() throws IOException {
    final String productSuppliersData = getJsonData(JSON_PATH + "product_supplier_noData.json");
    final RestTemplate rt = productSupplierService.getRestTemplate();
    final RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(rt);
    final MockRestServiceServer productSupplierServer = MockRestServiceServer.createServer(gateway);
    productSupplierServer
        .expect(
            once(),
            requestTo(HOST_URL + "/product-supplier/v1/products/1010044/suppliers/5201,5210,5211"))
        .andRespond(withSuccess(productSuppliersData, APPLICATION_JSON));

    productSupplierServer
        .expect(
            once(),
            requestTo(HOST_URL + "/product-supplier/v1/products/70300444/suppliers/5201,5210,5211"))
        .andRespond(withSuccess(productSuppliersData, APPLICATION_JSON));
  }

  @AfterAll
  public static void afterSetUp() {
    log.debug("Executing after setup method");
    new ActiveMqHelper().removeAllMessages();
  }
}
