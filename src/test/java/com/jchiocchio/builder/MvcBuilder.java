package com.jchiocchio.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import lombok.SneakyThrows;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MvcBuilder {

    private static final String JSON_ROOT_PATH = "$";

    private final MockMvc mvc;

    private final boolean applyDefaults;

    private final MockHttpServletRequestBuilder requestBuilder;

    private final MapperHelper mapper;

    private final List<ResultMatcher> matchers = new ArrayList<>();

    private ResultMatcher expectedStatus = status().isOk();

    public MvcBuilder(MockHttpServletRequestBuilder requestBuilder, MockMvc mvc, MapperHelper mapper) {
        this(requestBuilder, mvc, mapper, true);
    }

    public MvcBuilder(MockHttpServletRequestBuilder requestBuilder, MockMvc mvc, MapperHelper mapper, boolean applyDefaults) {
        this.requestBuilder = requestBuilder;
        this.mvc = mvc;
        this.mapper = mapper;
        this.applyDefaults = applyDefaults;
    }

    @SneakyThrows
    public ResultActions perform() {
        if (applyDefaults) {
            this.applyRequestDefaults();
            this.applyDefaultExpectationsForResponse();
        }
        return mvc.perform(requestBuilder)
           .andExpect(expectedStatus)
           .andExpect(matchAll(matchers.toArray(new ResultMatcher[0])));
    }

    // Matchers builder interface

    /**
     * You MUST NOT use this method to add an expected status, use {@link MvcBuilder#withExpectedStatus(ResultMatcher)
     * withStatus(expectedStatus)} instead.
     */
    public MvcBuilder andExpect(ResultMatcher resultMatcher) {
        this.matchers.add(resultMatcher);
        return this;
    }

    public MvcBuilder andExpectArrayOf(int size, String path) {
        matchers.add(jsonPath(path).isArray());
        matchers.add(jsonPath(path, hasSize(size)));
        return this;
    }

    private MvcBuilder andExpectArrayOf(int size) {
        return this.andExpectArrayOf(size, JSON_ROOT_PATH);
    }

    public MvcBuilder andExpectEmptyArray() {
        return this.andExpectEmptyArray(JSON_ROOT_PATH);
    }

    public MvcBuilder andExpectEmptyArray(String path) {
        matchers.add(jsonPath(path).isArray());
        matchers.add(jsonPath(path, is(empty())));
        return this;
    }

    public MvcBuilder andExpectPageOf(int size) {
        matchers.add(jsonPath("$.content").isArray());
        matchers.add(jsonPath("$.content", hasSize(size)));
        return this;
    }

    public MvcBuilder andExpectJsonContentOf(Object... entities) {
        andExpectArrayOf(entities.length);
        matchers.add(content().json(mapper.toJsonDTO(entities)));
        return this;
    }

    public MvcBuilder andExpectJsonContentOf(List<?> entities) {
        andExpectArrayOf(entities.size());
        matchers.add(content().json(mapper.toJsonDTO(entities)));
        return this;
    }

    public MvcBuilder andExpectJsonContentOf(Object entity) {
        matchers.add(content().json(mapper.toJsonDTO(entity)));
        return this;
    }

    public MvcBuilder andExpectErrorMessages(String... errorMessages) {
        matchers.add(jsonPath("$.messages", containsInAnyOrder(errorMessages)));
        return this;
    }

    public MvcBuilder withExpectedStatus(ResultMatcher status) {
        this.expectedStatus = status;
        return this;
    }

    // Request builder interface

    public MvcBuilder withHeader(String name, Object... values) {
        this.requestBuilder.header(name, values);
        return this;
    }

    public MvcBuilder withParam(String name, String... values) {
        requestBuilder.param(name, values);
        return this;
    }

    public MvcBuilder withContent(String content) {
        requestBuilder.content(content);
        return this;
    }

    public MvcBuilder withContent(Object arbitraryObject) {
        return withContent(mapper.toJson(arbitraryObject));
    }

    public MvcBuilder withContentAsDTO(Object entity) {
        return withContent(mapper.toJsonDTO(entity));
    }

    private void applyRequestDefaults() {
        requestBuilder.contentType(APPLICATION_JSON);
    }

    private void applyDefaultExpectationsForResponse() {
        this.matchers.add(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }
}
