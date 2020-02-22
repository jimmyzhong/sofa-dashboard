package me.izhong.common.domain;

import java.io.Serializable;
import java.util.List;

public class PageModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long count;
    private List<T> rows;

    public PageModel() {

    }

    public PageModel(Long count, List<T> rows) {
        this.count = count;
        this.rows = rows;
    }

    public static <T> PageModel<T> instance(Long count, List<T> rows) {
        return new PageModel<T>(count, rows);
    }

    public static <T> PageModel<T> instance(Integer count, List<T> rows) {
        return new PageModel<T>(count.longValue(), rows);
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
