package me.izhong.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class PersistedEntity implements Serializable {
    private LocalDateTime createTime;
    private String createdBy;
}
