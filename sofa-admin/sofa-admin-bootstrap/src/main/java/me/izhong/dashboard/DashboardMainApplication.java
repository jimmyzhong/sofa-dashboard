package me.izhong.dashboard;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.Arrays;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class,
        DataSourceAutoConfiguration.class})
//,MongoAutoConfiguration.class
@EnableCaching
@ComponentScan(value = {"me.izhong"})
@Slf4j
public class DashboardMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardMainApplication.class, args);
    }

//    @Value("${dashboard.mongodb.uri}")
//    private String mongoUri;

//    @Bean(name = "mongoTemplate")
//    public MongoTemplate mongoTemplate() throws Exception {
//        return new MongoTemplate(primaryFactory());
//    }
//
//    public MongoDbFactory primaryFactory() throws Exception {
//        log.info("mongo uri:{}",mongoUri);
//        MongoClientURI mongoClientURI = new MongoClientURI(mongoUri);
//        return new SimpleMongoDbFactory(new MongoClient(mongoClientURI),mongoClientURI.getDatabase());
//    }

    @PostConstruct
    public void init() {
        //Annotation[] anns = DashboardMainApplication.class.getDeclaredAnnotations();
        //Arrays.stream(anns).forEach(e -> System.out.println(e.annotationType()));
    }
}
