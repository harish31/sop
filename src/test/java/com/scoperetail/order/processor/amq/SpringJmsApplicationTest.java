package com.scoperetail.order.processor.amq;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@SpringBootTest(
//    classes = {SupplierOrderProcessorApplication.class, H2TestProfileJPAConfig.class},
//    webEnvironment = WebEnvironment.DEFINED_PORT)
public class SpringJmsApplicationTest {

  private static ApplicationContext applicationContext;

  @Autowired
  void setContext(final ApplicationContext applicationContext) {
    SpringJmsApplicationTest.applicationContext = applicationContext;
  }

  @AfterAll
  public static void afterClass() {
    ((ConfigurableApplicationContext) applicationContext).close();
  }

//  @ClassRule 
  public static EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();

  @Autowired private Sender sender;

  @Autowired private Receiver receiver;

//  @Test
  public void testReceive() throws Exception {
    sender.send("testQueue", "Hello Spring JMS ActiveMQ!");

    receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    assertThat(receiver.getLatch().getCount()).isEqualTo(0);
  }
}
