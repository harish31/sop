package com.scoperetail.supplier.order.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.WebApplicationInitializer;

@SpringBootApplication(
    scanBasePackages = {"com.scoperetail.supplier", "com.scoperetail.contrive"},
    exclude = {LiquibaseAutoConfiguration.class, RepositoryRestMvcAutoConfiguration.class})
@EnableJpaRepositories({
  "com.scoperetail.order.persistence.repositories",
  "com.scoperetail.contrive.model",
  "com.scoperetail.order.persistence.repository"
})
@EntityScan({"com.scoperetail.contrive.model", "com.scoperetail.order.persistence.entity"})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class SupplierOrderProcessorApplication extends SpringBootServletInitializer implements WebApplicationInitializer {

  public static void main(final String[] args) {
    SpringApplication.run(SupplierOrderProcessorApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
    return application.sources(SupplierOrderProcessorApplication.class);
  }
}
