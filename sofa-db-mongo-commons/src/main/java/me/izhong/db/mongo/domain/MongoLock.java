package me.izhong.db.mongo.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sys_lock")
@Getter
@Setter
public class MongoLock {

    @Id
    private String key;
    private long value;
    private long expire;

}
