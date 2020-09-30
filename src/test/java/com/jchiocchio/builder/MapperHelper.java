package com.jchiocchio.builder;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jchiocchio.mapping.DTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import ma.glasnost.orika.MapperFacade;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@Component
public class MapperHelper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MapperFacade orikaMapper;

    @SneakyThrows
    public String toJson(Object arbitraryObject) {
        return objectMapper.writeValueAsString(arbitraryObject);
    }

    /**
     *
     * @param entities will be converted to their respective DTO and then to JSON. {entities.length} MUST be greater
     *                 than zero, otherwise, you should use {@link MvcBuilder#andExpectEmptyArray()} or
     *                 {@link MvcBuilder#andExpectEmptyArray(String)} for asserting an empty array.
     * @return Json String
     *
     */
    public String toJsonDTO(Object... entities) {
        checkArgument(entities.length > 0, "At least one entity needs to be provided");

        var dtoClass = findDTOClass(entities[0].getClass());

        var listOfInstances = toDTO(dtoClass, entities);

        return toJson(listOfInstances);
    }

    public String toJsonDTO(List<?> entities) {
        return toJsonDTO(entities.toArray());
    }

    @SneakyThrows
    public String toJsonDTO(Object entity) {
        var dto = orikaMapper.map(entity, findDTOClass(entity.getClass()));

        return objectMapper.writeValueAsString(dto);
    }

    private Class<?> findDTOClass(Class<?> clazz) {
        DTO dtoAnnotation = AnnotationUtils.findAnnotation(clazz, DTO.class);

        checkArgument(dtoAnnotation != null, "DTO annotation was not found in " + clazz.getName());

        return dtoAnnotation.value();
    }

    private <T> List<T> toDTO(Class<T> dtoClass, Object... instances) {
        return Stream.of(instances).map(instance -> orikaMapper.map(instance, dtoClass)).collect(toList());
    }

    @SneakyThrows
    protected String toJson(List<?> instances) {
        return objectMapper.writeValueAsString(instances);
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    protected <T> String toJsonPage(List<T> instances) {
        PageImpl<T> page = new PageImpl<>(instances);

        return objectMapper.writeValueAsString(page);
    }

}
