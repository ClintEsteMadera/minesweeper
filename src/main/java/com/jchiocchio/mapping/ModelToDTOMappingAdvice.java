package com.jchiocchio.mapping;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import ma.glasnost.orika.MapperFacade;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Intercepts REST responses and maps the models returned by controllers, to their associated DTOs, before JSON
 * serialization takes place.
 */
@SuppressWarnings("unchecked")
@ControllerAdvice(basePackages = "com.jchiocchio.controller")
public class ModelToDTOMappingAdvice extends AbstractMappingJacksonResponseBodyAdvice implements MappingAdvice {

    @Autowired
    private MapperFacade orikaMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return this.supportsParameter(returnType, converterType);
    }

    @Override
    protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
                                           MediaType contentType,
                                           MethodParameter returnType,
                                           ServerHttpRequest request,
                                           ServerHttpResponse response) {

        DTO annotation = this.findDTOAnnotation(returnType);

        Class<?> dtoClass = annotation.value();

        Object valueReturnedByTheController = bodyContainer.getValue();

        Object valueToSerializeToJSON = getValueToSerializeToJSON(valueReturnedByTheController, dtoClass, request);

        bodyContainer.setValue(valueToSerializeToJSON);
    }

    private Object getValueToSerializeToJSON(Object valueReturnedByTheController, Class<?> dtoClass,
                                             ServerHttpRequest request) {
        Object valueToSerializeToJSON;
        if (Page.class.isAssignableFrom(valueReturnedByTheController.getClass())) {
            Page page = (Page) valueReturnedByTheController;

            Function mappingFunction = each -> orikaMapper.map(each, dtoClass);

            if (paginationWasNotRequested(request)) {
                valueToSerializeToJSON =
                    page.getContent().stream().map(mappingFunction).collect(toList());
            } else {
                valueToSerializeToJSON = page.map(mappingFunction);
            }
        } else {
            valueToSerializeToJSON = orikaMapper.map(valueReturnedByTheController, dtoClass);
        }
        return valueToSerializeToJSON;
    }

    private boolean paginationWasNotRequested(ServerHttpRequest request) {
        return request instanceof ServletServerHttpRequest &&
               isBlank(((ServletServerHttpRequest) request).getServletRequest().getParameter("page"));
    }
}
