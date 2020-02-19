package me.izhong.db.common.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import me.izhong.common.annotation.CreateTimeAdvise;
import me.izhong.common.annotation.Search;
import me.izhong.common.annotation.UpdateTimeAdvise;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class TimedBasedEntity implements Serializable {

    @Id
    @JSONField(serialize = false,deserialize = false)
    private ObjectId id;

    @Indexed
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @CreateTimeAdvise
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimeAdvise
    private Date updateTime;

    private String createBy;

    private String updateBy;

    @Search
    @JSONField(deserialize = false)
    private Boolean isDelete;

    /**
     * 备注
     */
    private String remark;
}
