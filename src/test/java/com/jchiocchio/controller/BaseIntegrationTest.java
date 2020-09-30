package com.jchiocchio.controller;

import java.util.UUID;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jchiocchio.MinesweeperApplication;
import com.jchiocchio.builder.ApiTestClient;
import com.jchiocchio.builder.MvcBuilder;
import com.jchiocchio.entityfactory.BoardTestEntityFactory;
import com.jchiocchio.entityfactory.GameTestEntityFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MinesweeperApplication.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected GameTestEntityFactory gameTestEntityFactory;

    @Autowired
    protected BoardTestEntityFactory boardTestEntityFactory;
    
    @Autowired
    protected ApiTestClient apiTestClient;

    abstract String getBasePath();

    @BeforeEach
    public void setUp() {
        // TODO setup user?
        // this.user = userTestEntityFactory.getUser();
    }

    protected void assertNotFoundErrorWhenGettingByNonExistentId() {
        this.assertNotFoundErrorWhenGettingByNonExistentId("%s");
    }

    protected void assertNotFoundErrorWhenGettingByNonExistentId(String endpoint) {
        this.getById(endpoint, UUID.randomUUID())
            .withExpectedStatus(status().isNotFound())
            .perform();
    }

    protected MvcBuilder getById(String endpoint, UUID id) {
        return this.getByIds(endpoint, id);
    }

    protected MvcBuilder getByIds(String endpoint, UUID... ids) {
        return apiTestClient.getById(getBasePath(), endpoint, ids);
    }

    MvcBuilder get(String endpoint) {
        return apiTestClient.get(getBasePath() + endpoint);
    }

    MvcBuilder getAll() {
        return apiTestClient.get(getBasePath());
    }

    MvcBuilder post() {
        return apiTestClient.post(getBasePath(), "");
    }

    MvcBuilder patchById(UUID id) {
        String endpoint = id == null? "" : "%s";
        return apiTestClient.patch(getBasePath(), endpoint, id);
    }

    MvcBuilder patch(String endpoint, Object... replacements) {
        return apiTestClient.patch(getBasePath(), endpoint, replacements);
    }

    MvcBuilder post(String endpoint, Object... replacements) {
        return apiTestClient.post(getBasePath(), endpoint, replacements);
    }

    MvcBuilder delete(String endpoint, Object... replacements) {
        return apiTestClient.delete(getBasePath(), endpoint, replacements);
    }
}
