package com.anz.swagger.config;

import static java.util.Collections.singletonList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.anz.swagger.controller.InfoController;
import com.fasterxml.classmate.TypeResolver;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2 
@ComponentScan(basePackageClasses = {
    InfoController.class
})
public class Swagger2Config implements WebMvcConfigurer {
  @Bean
  public Docket petApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select() 
        .apis(RequestHandlerSelectors.any()) 
        .paths(PathSelectors.any()) 
        .build() 
        .pathMapping("/") 
        .directModelSubstitute(LocalDate.class, String.class) 
        .genericModelSubstitutes(ResponseEntity.class)
        .alternateTypeRules(
            newRule(typeResolver.resolve(DeferredResult.class,
                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                typeResolver.resolve(WildcardType.class))) 
        .useDefaultResponseMessages(false) 
        .globalResponses(HttpMethod.GET, 
            singletonList(new ResponseBuilder()
                .code("500")
                .description("500 message")
                .representation(MediaType.TEXT_XML)
                .apply(r ->
                    r.model(m ->
                        m.referenceModel(ref ->
                            ref.key(k ->
                                k.qualifiedModelName(q ->
                                    q.namespace("some:namespace")
                                        .name("ERROR")))))) 
                .build()))
        .securitySchemes(singletonList(apiKey())) 
        .securityContexts(singletonList(securityContext())) 
        .enableUrlTemplating(true) 
        .globalRequestParameters(
            singletonList(new springfox.documentation.builders.RequestParameterBuilder()
                .name("someGlobalParameter")
                .description("Description of someGlobalParameter")
                .in(ParameterType.QUERY)
                .required(true)
                .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
                .build()))
        .tags(new Tag("Student Service", "All apis relating to Students")) 
        .additionalModels(typeResolver.resolve(String.class)); 
  }

  @Autowired
  private TypeResolver typeResolver;

  private ApiKey apiKey() {
    return new ApiKey("mykey", "api_key", "header"); 
  }

  @SuppressWarnings("deprecation")
  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/anyPath.*")) 
        .build();
  }

  List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope
        = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return singletonList(
        new SecurityReference("mykey", authorizationScopes)); 
  }

  @Bean
  SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder() 
        .clientId("test-app-client-id")
        .clientSecret("test-app-client-secret")
        .realm("test-app-realm")
        .appName("test-app")
        .scopeSeparator(",")
        .additionalQueryStringParams(null)
        .useBasicAuthenticationWithAccessCodeGrant(false)
        .enableCsrfSupport(false)
        .build();
  }

  @Bean
  UiConfiguration uiConfig() {
    return UiConfigurationBuilder.builder() 
        .deepLinking(true)
        .displayOperationId(false)
        .defaultModelsExpandDepth(1)
        .defaultModelExpandDepth(1)
        .defaultModelRendering(ModelRendering.EXAMPLE)
        .displayRequestDuration(false)
        .docExpansion(DocExpansion.NONE)
        .filter(false)
        .maxDisplayedTags(null)
        .operationsSorter(OperationsSorter.ALPHA)
        .showExtensions(false)
        .showCommonExtensions(false)
        .tagsSorter(TagsSorter.ALPHA)
        .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
        .validatorUrl(null)
        .build();
  }
}
