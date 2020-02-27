package me.izhong.jobs.dto;

import java.util.List;

import lombok.Data;

@Data
public class CategoryDTO {

    private Long value;
    private String label;
    private List<CategoryDTO> children;
}
