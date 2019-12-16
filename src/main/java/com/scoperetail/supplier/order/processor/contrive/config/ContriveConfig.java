package com.scoperetail.supplier.order.processor.contrive.config;

import com.scoperetail.commons.dto.Broker;
import com.scoperetail.commons.util.XMLUtil;
import com.scoperetail.parley.impl.jms.JMSBrokerInfo;
import com.scoperetail.supplier.order.processor.commons.constants.SupplierOrderConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "parley")
public class ContriveConfig {

  private List<Broker> broker;

  @Bean
  public List<JMSBrokerInfo> getJmsBrokerInfos() {
    final List<JMSBrokerInfo> infos = new ArrayList<>();

    for (final Broker parleyBroker : broker) {
      final JMSBrokerInfo broker = new JMSBrokerInfo();
      broker.setId(parleyBroker.getId());
      broker.setHost(parleyBroker.getHost());
      broker.setPort(parleyBroker.getPort());
      broker.setProtocol(parleyBroker.getProtocol());
      broker.setJmsProvider(parleyBroker.getJmsProvider());
      broker.setUsername(parleyBroker.getUsername());
      broker.setPassword(parleyBroker.getPassword());
      broker.setChannel(parleyBroker.getChannel());
      broker.setQueueManagerName(parleyBroker.getQueueManagerName());
      broker.setSendSessionCacheSize(Integer.valueOf(parleyBroker.getSendSessionCacheSize()));

      broker.setProviderURL(parleyBroker.getProviderURL());
      broker.setJndiConnectionFactory(parleyBroker.getJndiConnectionFactory());
      infos.add(broker);
    }
    return infos;
  }

  @Bean
  public Schema supplierOrderInboundSchema() throws SAXException {
    return XMLUtil.getSchema(this.getClass(), SupplierOrderConstants.CUSTOMER_ORDER_XSD);
  }

  @Bean
  public Schema customerOrderOutboundSchema() throws SAXException {
    return XMLUtil.getSchema(this.getClass(), SupplierOrderConstants.ORDER_OUTBOUND_XSD);
  }

  @Bean
  public Schema camsDownloadCompleteEventInboundSchema() throws SAXException {
    return XMLUtil.getSchema(this.getClass(), SupplierOrderConstants.CAMS_EVENT_XSD);
  }
  
  @Bean
  public Schema orderVisibilityOutboundSchema() throws SAXException {
    return XMLUtil.getSchema(this.getClass(), SupplierOrderConstants.ORDER_VISIBILITY_XSD);
  }
}
