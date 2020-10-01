package com.jchiocchio.builder;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

@Component
public class ApiTestClient {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MapperHelper mapperHelper;

    public MvcBuilder getById(String basePath, String endpoint, UUID... ids) {
        return this.get(this.buildURI(basePath, endpoint, ids));
    }

    public MvcBuilder get(String uri) {
        return new MvcBuilder(MockMvcRequestBuilders.get(uri), mvc, mapperHelper);
    }

    public MvcBuilder post(String basePath, String endpoint, Object... replacements) {
        String uri = this.buildURI(basePath, endpoint, replacements);

        return new MvcBuilder(MockMvcRequestBuilders.post(uri), mvc, mapperHelper);
    }

    public MvcBuilder patch(String basePath, String endpoint, Object... replacements) {
        String uri = this.buildURI(basePath, endpoint, replacements);

        return new MvcBuilder(MockMvcRequestBuilders.patch(uri), mvc, mapperHelper);
    }

    public MvcBuilder delete(String basePath, String endpoint, Object... replacements) {
        String uri = this.buildURI(basePath, endpoint, replacements);

        return new MvcBuilder(MockMvcRequestBuilders.delete(uri), mvc, mapperHelper);
    }

    private String buildURI(String basePath, String endpoint, Object[] replacements) {
        String uri = basePath + endpoint;

        if (isNotEmpty(replacements)) {
            uri = format(uri, replacements);
        }
        return uri;
    }
}
