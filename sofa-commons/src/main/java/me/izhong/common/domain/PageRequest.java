package me.izhong.common.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Slf4j
public class PageRequest {

    /**
     * 每页多少条数据
     */
    private long pageSize;
    /**
     * 请求的页 从1开始
     */
    private long pageNum;
    /**
     * 排序的列名称
     */
    private String orderByColumn;
    /**
     * 升序 asc
     * 降序 desc
     */
    private String orderDirection;
    private String status;
    private Date beginCreateTime;
    private Date endCreateTime;

    /**
     *  当前查询对象能看到的部门数据
     */
    private Set<Long> depts;

    public volatile AtomicBoolean alreadyInjectToQuery = new AtomicBoolean();

    public PageRequest() {

    }

    public PageRequest(long pageSize, long pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    public PageRequest(long pageSize, long pageNum, String orderByColumn, String orderDirection) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.orderByColumn = orderByColumn;
        this.orderDirection = orderDirection;
    }

    public static PageRequest build() {
        return new PageRequest();
    }

    public PageRequest pageSize(long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public PageRequest pageNum(long pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public PageRequest orderBy(String column) {
        this.orderByColumn = column;
        return this;
    }

    public PageRequest orderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
        return this;
    }

    public PageRequest status(String status) {
        this.status = status;
        return this;
    }

    public PageRequest beginDate(Date beginDate) {
        this.beginCreateTime = beginDate;
        return this;
    }

    public PageRequest endDate(Date endDate) {
        this.endCreateTime = endDate;
        return this;
    }

}
