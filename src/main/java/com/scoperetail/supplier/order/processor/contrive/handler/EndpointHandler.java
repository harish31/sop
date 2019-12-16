package com.scoperetail.supplier.order.processor.contrive.handler;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.scoperetail.commons.dto.ParleyQueue;
import com.scoperetail.parley.impl.common.Constants;
import com.scoperetail.parley.impl.jms.JMSEndpoint;
import com.scoperetail.parley.spi.IEndpoint;
import com.scoperetail.parley.spi.IEndpointHandler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "parley")
public class EndpointHandler implements IEndpointHandler, InitializingBean {

  private List<ParleyQueue> source;
  private List<ParleyQueue> target;
  private Map<String, IEndpoint> sourceEndPoints = new HashMap<>();
  private Map<String, IEndpoint> targetEndPoints = new HashMap<>();

  @Override
  public IEndpoint getSourceByName(final String sourceName) {
    return sourceEndPoints.get(sourceName);
  }

  @Override
  public IEndpoint getTargetByName(final String targetName) {
    return targetEndPoints.get(targetName);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final int sourceCount = source.size();
    log.info("Sources found: " + sourceCount);

    final int targetCount = target.size();
    log.info("Targets found: " + targetCount);
    if (sourceCount + targetCount > 0) {
      parseSourceAndTargets();
    }
    log.info("All sources: {}", sourceEndPoints);
    log.info("All targets: {}", targetEndPoints);
  }

  /** Parses the source and targets. true, if successful */
  public void parseSourceAndTargets() {
    source
        .stream()
        .forEach(
            parleySource -> {
              if (!EMPTY.equals(parleySource.getName()) && !EMPTY.equals(parleySource.getType())) {
                /** Prepare sources. */
                prepareEndPoints(
                    parleySource.getName(),
                    parleySource.getType(),
                    parleySource.getBrokerId(),
                    parleySource.getEndpoint(),
                    parleySource.getConcurrency(),
                    parleySource.getClientID(),
                    sourceEndPoints);
              }
            });

    target
        .stream()
        .forEach(
            parleyTarget -> {
              if (!EMPTY.equals(parleyTarget.getName()) && !EMPTY.equals(parleyTarget.getType())) {
                /** Prepare targets. */
                prepareEndPoints(
                    parleyTarget.getName(),
                    parleyTarget.getType(),
                    parleyTarget.getBrokerId(),
                    parleyTarget.getEndpoint(),
                    parleyTarget.getConcurrency(),
                    parleyTarget.getClientID(),
                    targetEndPoints);
              }
            });
  }

  private void prepareEndPoints(
      final String endpointName,
      final String endpointType,
      final String brokerId,
      final String endpoint,
      final String concurrency,
      final String clientID,
      final Map<String, IEndpoint> endPoints) {

    JMSEndpoint jmsEndpoint = null;
    switch (endpointType) {
      case Constants.JMSQUEUE:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setEndpointType(Constants.QUEUE);
        jmsEndpoint.setProtocol(Constants.JMS);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.JMSTOPIC:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setEndpointType(Constants.TOPIC);
        jmsEndpoint.setProtocol(Constants.JMS);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.TOPIC:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setEndpointType(Constants.TOPIC);
        jmsEndpoint.setProtocol(Constants.JMS);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.AMQPQUEUE:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setEndpointType(Constants.QUEUE);
        jmsEndpoint.setProtocol(Constants.AMQP);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.AMQPTOPIC:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setEndpointType(Constants.TOPIC);
        jmsEndpoint.setProtocol(Constants.AMQP);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.WEBLOGICQUEUE:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setProtocol(Constants.WEBLOGIC);
        jmsEndpoint.setEndpointType(Constants.QUEUE);
        endPoints.put(endpointName, jmsEndpoint);
        break;
      case Constants.WEBLOGICTOPIC:
        jmsEndpoint = createJmsEndPoint(endpointName, brokerId, endpoint, concurrency, clientID);
        jmsEndpoint.setProtocol(Constants.WEBLOGIC);
        jmsEndpoint.setEndpointType(Constants.TOPIC);
        endPoints.put(endpointName, jmsEndpoint);

      default:
        log.info("Invalid EndPoint Type:::");
    }
  }

  private JMSEndpoint createJmsEndPoint(
      final String endpointName,
      final String brokerId,
      final String endpoint,
      final String concurrency,
      final String clientID) {
    final JMSEndpoint jmsEndpoint = new JMSEndpoint();
    jmsEndpoint.setEndpointName(endpointName);
    jmsEndpoint.setBrokerId(brokerId);
    jmsEndpoint.setClientID(clientID);
    jmsEndpoint.setConcurrency(concurrency);
    jmsEndpoint.setEndpoint(endpoint);
    return jmsEndpoint;
  }
}
