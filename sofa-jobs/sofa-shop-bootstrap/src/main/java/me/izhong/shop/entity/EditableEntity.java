package me.izhong.shop.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class EditableEntity extends PersistedEntity {
    private LocalDateTime updateTime;
    private String updatedBy;
}
