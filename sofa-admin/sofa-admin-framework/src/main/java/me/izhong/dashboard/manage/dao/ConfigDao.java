package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigDao extends MongoRepository<SysConfig, Long> {


    SysConfig findByConfigId(Long configId);

    SysConfig findByConfigKey(String configKey);

    int deleteAllByConfigIdIn(List<Long> delIds);
}
