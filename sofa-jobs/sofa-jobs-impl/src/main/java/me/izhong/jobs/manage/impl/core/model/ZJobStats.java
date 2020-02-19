package me.izhong.jobs.manage.impl.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.db.common.domain.TimedBasedEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_djob_stats")
public class ZJobStats extends TimedBasedEntity implements Serializable {

	@AutoId
	@PrimaryId
	@Indexed(unique = true)
	private Long statsId;

	@Indexed(unique = true)
	private String key;

	@Indexed(unique = false)
	private String type;

	private String value1;
	private String value2;
	private String value3;
	private String value4;
	private String value5;

	@Indexed(unique = false,expireAfterSeconds = 90 * 24 * 60 * 60)//3个月
	private Date expireTime;

}
