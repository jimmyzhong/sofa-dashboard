package me.izhong.jobs.manage.impl.core.model;

import me.izhong.common.annotation.Search;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.PrimaryId;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_group")
public class ZJobGroup extends TimedBasedEntity implements Serializable {

    @AutoId
    @PrimaryId
    @Indexed(unique = true)
    private Long groupId;

    @Search(op = Search.Op.REGEX)
    private String groupName;

    private Long order;

}
