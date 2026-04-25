package ru.bmstu.iu7.bookstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore Recommendations API")
                        .version("1.0.0")
                        .description("REST API книжного магазина с системой рекомендаций")
                        .contact(new Contact()
                                .name("ИУ7-61Б")
                                .email("student@bmstu.ru")));
    }
}
