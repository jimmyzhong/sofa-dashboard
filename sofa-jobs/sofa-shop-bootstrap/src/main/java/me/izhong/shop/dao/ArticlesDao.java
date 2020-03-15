package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Articles;

@Repository
public interface ArticlesDao extends JpaRepository<Articles, Long> {
}
