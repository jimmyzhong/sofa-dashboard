package me.izhong.common.domain;

import me.izhong.common.model.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Slf4j
public class PageRequest {

    private long pageSize;
    private long pageNum;
    private String orderByColumn;
    private String isAsc;
    private String status;
    private Date beginDate;
    private Date endDate;

    private String perms;
    private Set<Long> depts;
    private UserInfo loginUser;

    public volatile AtomicBoolean alreadyInjectToQuery = new AtomicBoolean();

    public PageRequest() {

    }

    public PageRequest(long pageSize, long pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    public PageRequest(long pageSize, long pageNum, String orderByColumn, String isAsc) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.orderByColumn = orderByColumn;
        this.isAsc = isAsc;
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

    public PageRequest isAsc(String isAsc) {
        this.isAsc = isAsc;
        return this;
    }

    public PageRequest status(String status) {
        this.status = status;
        return this;
    }

    public PageRequest beginDate(Date beginDate) {
        this.beginDate = beginDate;
        return this;
    }

    public PageRequest endDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

}
