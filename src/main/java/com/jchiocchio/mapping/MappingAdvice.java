package com.jchiocchio.mapping;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import lombok.SneakyThrows;

public interface MappingAdvice {

    default boolean supportsParameter(MethodParameter methodParameter,
                                      Class<? extends HttpMessageConverter<?>> converterType) {

        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType) &&
               findDTOAnnotation(methodParameter) != null;
    }

    @SneakyThrows
    default DTO findDTOAnnotation(MethodParameter methodParameter) {
        Class<?> classToSearchForTheAnnotation = methodParameter.getParameterType();

        if (Page.class.isAssignableFrom(classToSearchForTheAnnotation)) {
            Type[] actualTypeArguments =
                ((ParameterizedType) methodParameter.getGenericParameterType()).getActualTypeArguments();

            if (ArrayUtils.isEmpty(actualTypeArguments)) {
                return null;
            }
            classToSearchForTheAnnotation = this.getClassFrom(actualTypeArguments[0]);
        }
        return AnnotationUtils.findAnnotation(classToSearchForTheAnnotation, DTO.class);
    }

    default Class<?> getClassFrom(Type type) throws ClassNotFoundException {
        return type instanceof Class ? (Class<?>) type : Class.forName(type.getTypeName());
    }
}
