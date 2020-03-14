package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysOperLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperLogDao extends MongoRepository<SysOperLog, Long> {

    SysOperLog findByOperId(Long operId);

    int deleteByOperIdIn(List<Long> delIds);
}
