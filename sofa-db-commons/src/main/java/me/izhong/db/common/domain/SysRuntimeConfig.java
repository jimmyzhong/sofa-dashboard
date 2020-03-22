package me.izhong.db.common.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "sys_runtime_config")
public class SysRuntimeConfig {

    @Id
    private String id;

    @Field
    private Date realmUpdateTime;

}