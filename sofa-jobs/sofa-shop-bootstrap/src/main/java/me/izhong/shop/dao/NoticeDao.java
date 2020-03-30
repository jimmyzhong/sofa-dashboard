package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Notice;

@Repository
public interface NoticeDao extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {
}
