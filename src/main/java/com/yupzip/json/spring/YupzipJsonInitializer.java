package com.yupzip.json.spring;

/**
 * Marker bean produced by {@link YupzipJsonAutoConfiguration} so the wiring step is visible
 * in the Spring application context and to actuator endpoints. Holds whether a user-provided
 * {@code JsonMapper} bean was adopted; useful for diagnostics.
 */
public class YupzipJsonInitializer {

    private final boolean usingSpringMapper;

    public YupzipJsonInitializer(boolean usingSpringMapper) {
        this.usingSpringMapper = usingSpringMapper;
    }

    /** @return {@code true} if a Spring-managed {@code JsonMapper} bean was wired into yupzip-json. */
    public boolean isUsingSpringMapper() {
        return usingSpringMapper;
    }
}
