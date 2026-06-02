package com.yupzip.json.spring;

import com.yupzip.json.Json;
import com.yupzip.json.jackson.JsonMappers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class YupzipJsonAutoConfigurationTest {

    private JsonMapper originalMapper;

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(YupzipJsonAutoConfiguration.class));

    @BeforeEach
    void captureOriginal() {
        originalMapper = JsonMappers.current();
    }

    @AfterEach
    void restoreOriginal() {
        JsonMappers.configure(originalMapper);
    }

    @Test
    void usesBuiltInMapperWhenNoJsonMapperBeanIsPresent() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(YupzipJsonInitializer.class);
            YupzipJsonInitializer initializer = context.getBean(YupzipJsonInitializer.class);
            assertThat(initializer.isUsingSpringMapper()).isFalse();
            assertThat(JsonMappers.current()).isSameAs(originalMapper);
        });
    }

    @Test
    void adoptsUserProvidedJsonMapperBean() {
        runner.withUserConfiguration(SnakeCaseMapperConfig.class).run(context -> {
            assertThat(context).hasSingleBean(YupzipJsonInitializer.class);
            YupzipJsonInitializer initializer = context.getBean(YupzipJsonInitializer.class);
            assertThat(initializer.isUsingSpringMapper()).isTrue();

            JsonMapper springMapper = context.getBean(JsonMapper.class);
            assertThat(JsonMappers.current()).isSameAs(springMapper);

            // End-to-end: a POJO converted via yupzip should reflect the Spring-managed
            // naming strategy.
            Person person = new Person();
            person.firstName = "John";
            Json json = Json.parse(person);
            assertThat(json.string("first_name")).isEqualTo("John");
        });
    }

    @Test
    void picksUpSpringBootJacksonAutoConfigurationMapper() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        YupzipJsonAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(JsonMapper.class);
                    YupzipJsonInitializer initializer = context.getBean(YupzipJsonInitializer.class);
                    assertThat(initializer.isUsingSpringMapper()).isTrue();
                    assertThat(JsonMappers.current()).isSameAs(context.getBean(JsonMapper.class));
                });
    }

    @Test
    void runsAtStartupEvenWhenLazyInitializationIsEnabled() {
        // Regression test: with spring.main.lazy-initialization=true, only beans that are
        // referenced get instantiated. The initializer is annotated @Lazy(false) so it must
        // be created at startup regardless — and therefore must wire JsonMappers before any
        // consumer code runs.
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        YupzipJsonAutoConfiguration.class))
                .withPropertyValues("spring.main.lazy-initialization=true")
                .run(context -> {
                    // Without resolving YupzipJsonInitializer here, confirm the wiring still ran.
                    JsonMapper springMapper = context.getBean(JsonMapper.class);
                    assertThat(JsonMappers.current()).isSameAs(springMapper);
                });
    }

    @Configuration
    static class SnakeCaseMapperConfig {
        @Bean
        public JsonMapper jsonMapper() {
            return JsonMapper.builder()
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .build();
        }
    }

    static class Person {
        public String firstName;
    }
}
