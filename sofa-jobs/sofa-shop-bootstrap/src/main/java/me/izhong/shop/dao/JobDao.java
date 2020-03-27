package me.izhong.shop.dao;

import me.izhong.shop.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobDao extends JpaRepository<Job, Integer> {
    Job findFirstByName(String name);
}
