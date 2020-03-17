package me.izhong.shop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.izhong.shop.entity.Template;

@Repository
public interface TemplateDao extends JpaRepository<Template, Long> {
}