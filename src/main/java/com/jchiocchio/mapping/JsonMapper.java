package com.jchiocchio.mapping;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class JsonMapper {

    @Autowired
    private ObjectMapper objectMapper;

    public String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(object))
                           .orElseThrow(() -> serializationException(object, null));
        } catch (JsonProcessingException e) {
            throw serializationException(object, e);
        }
    }

    public <T> T jsonToObject(byte[] bytes, TypeReference<T> objectType) {
        try {
            return objectMapper.readValue(bytes, objectType);
        } catch (IOException e) {
            throw new RuntimeException(format("Could not deserialize JSON to %s", objectType.getType().getTypeName()),
                                       e);
        }
    }

    private RuntimeException serializationException(Object object, Exception e) {
        return new RuntimeException(format("Could not serialize %s to JSON: %s",
                                           object.getClass().getSimpleName(),
                                           object.toString()), e);
    }
}
