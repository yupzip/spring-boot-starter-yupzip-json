package com.yupzip.json.spring;

import com.yupzip.json.Json;
import com.yupzip.json.jackson.JsonMappers;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Boot auto-configuration for yupzip-json.
 *
 * <p>If the application context declares a {@link JsonMapper} bean (Jackson 3), the starter
 * wires it into yupzip-json via {@link JsonMappers#configure(JsonMapper)} so a {@code Json}
 * instance is serialised through the same mapper as the rest of the application.
 *
 * <p>If no {@link JsonMapper} bean is present, yupzip-json falls back to its built-in default
 * mapper. The starter is a no-op in that case.
 *
 * <p><b>Note on Jackson versions:</b> yupzip-json uses Jackson 3 ({@code tools.jackson}).
 * Spring Boot 3.x's auto-configuration provides a Jackson 2 {@code ObjectMapper}, not a
 * Jackson 3 {@code JsonMapper}. To activate the bridge, declare a Jackson 3 {@code JsonMapper}
 * bean in your configuration. When Spring Boot ships Jackson 3 support natively, this becomes
 * zero-configuration.
 */
@AutoConfiguration
@ConditionalOnClass({ JsonMapper.class, Json.class })
public class YupzipJsonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YupzipJsonInitializer yupzipJsonInitializer(ObjectProvider<JsonMapper> mapperProvider) {
        JsonMapper mapper = mapperProvider.getIfAvailable();
        if (mapper != null) {
            JsonMappers.configure(mapper);
        }
        return new YupzipJsonInitializer(mapper != null);
    }
}
