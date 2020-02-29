package me.izhong.db.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoConfig  {

    @Bean
    MongoTransactionManager mongoTransactionManager(MongoDbFactory factory) {
        return new MongoTransactionManager(factory);
    }
}
