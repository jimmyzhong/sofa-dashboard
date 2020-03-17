package me.izhong.shop.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class PersistedEntity implements Serializable {
    @Column(name="CREATE_TIME")
    protected LocalDateTime createTime;
    @Column(name="CREATE_BY")
    protected String createdBy;
}
