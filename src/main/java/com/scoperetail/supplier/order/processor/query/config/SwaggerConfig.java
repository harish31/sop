package com.scoperetail.supplier.order.processor.query.config;

import static com.scoperetail.commons.constants.SwaggerConstants.API_1_0;
import static com.scoperetail.commons.constants.SwaggerConstants.API_LICENSE_URL_STR;
import static com.scoperetail.commons.constants.SwaggerConstants.ENDPOINTS_TO_ACCESS_THE_SOP_DETAILS_STR;
import static com.scoperetail.commons.constants.SwaggerConstants.LICENSE_OF_API_STR;
import static com.scoperetail.commons.constants.SwaggerConstants.SOP_BASE_PACKAGE_CMD;
import static com.scoperetail.commons.constants.SwaggerConstants.SOP_BASE_PACKAGE_QUERY;
import static com.scoperetail.commons.constants.SwaggerConstants.SOP_REST_API_STR;
import static com.scoperetail.commons.constants.SwaggerConstants.SPRING_WEB_MVC_STR;
import static com.scoperetail.commons.constants.SwaggerConstants.TERMS_OF_SERVICE;
import static com.scoperetail.commons.constants.SwaggerConstants.VISHWANATH_CONTACT_NAME;
import static com.scoperetail.commons.constants.SwaggerConstants.VISWHWANATH_EMAIL;
import static com.scoperetail.commons.constants.SwaggerConstants.WWW_XPANXION_TEST_COM;
import static java.util.Collections.emptyList;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket api() {
    return new Docket(SWAGGER_2)
        .select()
        .apis(
            Predicates.or(
                RequestHandlerSelectors.basePackage(SPRING_WEB_MVC_STR),
                RequestHandlerSelectors.basePackage(SOP_BASE_PACKAGE_QUERY),
                RequestHandlerSelectors.basePackage(SOP_BASE_PACKAGE_CMD),
                RequestHandlerSelectors.basePackage(SOP_REST_API_STR)))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfo(
        SOP_REST_API_STR,
        ENDPOINTS_TO_ACCESS_THE_SOP_DETAILS_STR,
        API_1_0,
        TERMS_OF_SERVICE,
        new Contact(VISHWANATH_CONTACT_NAME, WWW_XPANXION_TEST_COM, VISWHWANATH_EMAIL),
        LICENSE_OF_API_STR,
        API_LICENSE_URL_STR,
        emptyList());
  }
}
