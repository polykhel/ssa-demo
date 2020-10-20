package com.polykhel.ssa.config.apidoc.customizer;

import com.polykhel.ssa.config.CoreProperties;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * A swagger customizer to setup {@link springfox.documentation.spring.web.plugins.Docket} with custom settings.
 */
public class CustomSwaggerCustomizer implements SwaggerCustomizer, Ordered {

    /**
     * The default order for the customizer.
     */
    public static final int DEFAULT_ORDER = 0;
    private final CoreProperties.Swagger properties;
    private int order = DEFAULT_ORDER;

    public CustomSwaggerCustomizer(CoreProperties.Swagger properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public void customize(Docket docket) {
        Contact contact = new Contact(
            properties.getContactName(),
            properties.getContactUrl(),
            properties.getContactEmail()
        );

        ApiInfo apiInfo = new ApiInfo(
            properties.getTitle(),
            properties.getDescription(),
            properties.getVersion(),
            properties.getTermsOfServiceUrl(),
            contact,
            properties.getLicense(),
            properties.getLicenseUrl(),
            new ArrayList<>()
        );

        docket.host(properties.getHost())
            .protocols(new HashSet<>(Arrays.asList(properties.getProtocols())))
            .apiInfo(apiInfo)
            .useDefaultResponseMessages(properties.isUseDefaultResponseMessages())
            .forCodeGeneration(true)
            .directModelSubstitute(ByteBuffer.class, String.class)
            .genericModelSubstitutes(ResponseEntity.class)
            .ignoredParameterTypes(Pageable.class)
            .select()
            .paths(regex(properties.getDefaultIncludePattern()))
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Setter for the field <code>order</code>
     *
     * @param order an integer
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
