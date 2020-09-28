package com.jchiocchio.mapping;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import lombok.SneakyThrows;
import ma.glasnost.orika.MapperFacade;

/**
 * Intercepts REST requests and maps JSON payloads (deserialized to DTOs) to their corresponding model objects.
 */
@ControllerAdvice(basePackages = "com.jchiocchio.controller")
public class DTOToModelMappingAdvice extends RequestBodyAdviceAdapter implements MappingAdvice {

    @Autowired
    private MapperFacade orikaMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        return this.supportsParameter(methodParameter, converterType);
    }

    @Override
    @SneakyThrows
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter methodParameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) {

        DTO annotation = this.findDTOAnnotation(methodParameter);

        Class<?> dtoClass = annotation.value();

        Object dto = objectMapper.readValue(inputMessage.getBody(), dtoClass);

        Object model = orikaMapper.map(dto, this.getClassFrom(targetType));

        ByteArrayInputStream serializedModel = new ByteArrayInputStream(objectMapper.writeValueAsBytes(model));

        return new MappingJacksonInputMessage(serializedModel, inputMessage.getHeaders());
    }
}
