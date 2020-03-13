package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysUserPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPostDao extends MongoRepository<SysUserPost, Long> {

    long deleteAllByUserId(Long userId);

    List<SysUserPost> findAllByPostIdInAndUserId(List<Long> postIds, Long userId);
}
