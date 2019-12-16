package com.scoperetail.supplier.order.processor.command.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@XmlRootElement
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {

  @XmlElement(required = true)
  private Integer orderId;

  @XmlElement private List<Integer> orderLines;
}
