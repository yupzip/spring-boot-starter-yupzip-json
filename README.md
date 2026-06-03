# spring-boot-starter-yupzip-json

Spring Boot starter that lets [yupzip-json](https://github.com/yupzip/yupzip-json) share the application's `JsonMapper` so a `Json` instance is serialised by the same mapper as the rest of the application.
# Status
![Build](https://github.com/yupzip/spring-boot-starter-yupzip-json/actions/workflows/build.yml/badge.svg)
[![Coverage](https://coveralls.io/repos/github/yupzip/spring-boot-starter-yupzip-json/badge.svg?branch=master)](https://coveralls.io/github/yupzip/spring-boot-starter-yupzip-json?branch=master)
## Requirements

- JDK 17+
- **Spring Boot 4.0+** — earlier versions ship Jackson 2 and do not provide a `JsonMapper` (Jackson 3) bean for the starter to pick up. See *Spring Boot 3.x notes* below if you cannot upgrade yet.

## Getting started

### Maven
```xml
<dependency>
    <groupId>com.yupzip.json</groupId>
    <artifactId>spring-boot-starter-yupzip-json</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.yupzip.json:spring-boot-starter-yupzip-json:1.1.0'
```

## How it works

The starter's auto-configuration looks for a `tools.jackson.databind.json.JsonMapper` bean in the application context:
- **If present**, it is registered with yupzip via `JsonMappers.configure(mapper)`. Every `Json` operation that touches Jackson — `parse`, `convertTo`, `toString`, etc. — uses the same mapper as the rest of the application.
- **If absent**, yupzip continues to use its built-in default mapper (no failure).

A `YupzipJsonInitializer` bean is added to the context; check `isUsingSpringMapper()` if you want to assert in tests that the bridge actually kicked in. The bean is eagerly instantiated (`@Lazy(false)`) so the bridge runs at startup regardless of the consumer's `spring.main.lazy-initialization` setting.

On Spring Boot 4.0+, `JacksonAutoConfiguration` (in the `spring-boot-jackson` module) produces a Jackson 3 `JsonMapper` bean configured from `spring.jackson.*` — so the starter is zero-config: add the dependency, configure Jackson as you would normally, done.

## Spring Boot 3.x notes

Spring Boot 3.x's `JacksonAutoConfiguration` produces a Jackson 2 `ObjectMapper`, not a Jackson 3 `JsonMapper`. yupzip-json uses Jackson 3, so the starter cannot auto-pick anything up under Spring Boot 3. You have two choices:

1. **Upgrade to Spring Boot 4.0+** (recommended).
2. **Declare your own Jackson 3 `JsonMapper` bean** alongside the Spring-managed Jackson 2 `ObjectMapper`:
   ```java
   @Configuration
   public class JacksonConfig {

       @Bean
       public JsonMapper jsonMapper() {
           return JsonMapper.builder()
                   .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                   // ... your modules, features, visibility settings
                   .build();
       }
   }
   ```
   The starter will pick this bean up automatically. Note that `spring.jackson.*` settings only affect Spring's Jackson 2 mapper, not your Jackson 3 one — you must configure them manually.

## Verifying the bridge in tests

```java
@SpringBootTest
class JsonBridgeTest {

    @Autowired YupzipJsonInitializer initializer;

    @Test
    void wiringIsActive() {
        assertThat(initializer.isUsingSpringMapper()).isTrue();
    }
}
```

## License

Apache License 2.0.
