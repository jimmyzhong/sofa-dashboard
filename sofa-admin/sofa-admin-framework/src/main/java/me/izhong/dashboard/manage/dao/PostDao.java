package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDao extends MongoRepository<SysPost, Long> {


    SysPost findByPostName(String postName);

    SysPost findByPostCode(String postCode);

    SysPost findByPostId(Long postId);

    List<SysPost> findAllByPostIdIn(List<Long> longs);
}
