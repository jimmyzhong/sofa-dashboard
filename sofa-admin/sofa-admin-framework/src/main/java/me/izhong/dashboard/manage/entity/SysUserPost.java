package me.izhong.dashboard.manage.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_user_post")
@Data
@CompoundIndexes({
        @CompoundIndex(name = "user_post", def = "{'userId': 1, 'postId': 1}", unique = true)
})
public class SysUserPost {

    @Id
    @JSONField(serialize = false,deserialize = false)
    private ObjectId id;

    /**
     * 用户ID
     */
    @Indexed
    private Long userId;

    /**
     * 岗位ID
     */
    @Indexed
    private Long postId;
}
