package me.izhong.db.common.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@Document(collection = "sys_sequence")
public class SysSeqInfo {

    @Id
    private String id;

    @Field
    private String collectionName;

    @Field
    private Long seqId;

    @Field
    private Long maxId;

}