package com.scoperetail.supplier.order.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.scoperetail.supplier.order.processor.command.model.CustomerOrder;

public class GenerateCustomerOrderXML {
  public static void main(final String[] args) throws FileNotFoundException, JAXBException {
    new GenerateCustomerOrderXML().runMarshaller();
  }

  private void runMarshaller() throws JAXBException, FileNotFoundException {
    final CustomerOrder co = new CustomerOrder(100, Arrays.asList(1, 2, 3));

    final JAXBContext context = JAXBContext.newInstance(CustomerOrder.class);
    final Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    final FileOutputStream fileOutputStream = new FileOutputStream(new File("CustomerOrder.xml"));

    marshaller.marshal(co, fileOutputStream);
    
    System.out.println("Done");
  }
}
