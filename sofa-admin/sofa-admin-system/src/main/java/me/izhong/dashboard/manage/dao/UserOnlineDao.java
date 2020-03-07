package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysUserOnline;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOnlineDao extends MongoRepository<SysUserOnline, String> {

    SysUserOnline findBySessionId(String sessionId);

}
