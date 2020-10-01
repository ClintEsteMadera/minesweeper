package com.jchiocchio.error.handler;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationError {

    private final String[] messages;

    private final String url;
}
