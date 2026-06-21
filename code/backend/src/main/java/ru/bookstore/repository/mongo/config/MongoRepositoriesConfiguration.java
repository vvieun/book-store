package ru.bookstore.repository.mongo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile("mongo")
@EnableMongoRepositories(basePackages = "ru.bookstore.repository.mongo.spring_data")
public class MongoRepositoriesConfiguration {
}

