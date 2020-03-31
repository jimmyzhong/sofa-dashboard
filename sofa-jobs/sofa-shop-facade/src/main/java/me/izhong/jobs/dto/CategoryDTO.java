package me.izhong.jobs.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO implements Serializable {

    private Long value;
    private String label;
    private List<CategoryDTO> children;
}
