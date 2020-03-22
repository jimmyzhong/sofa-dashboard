package me.izhong.shop.service;

import me.izhong.shop.entity.Versions;

public interface IVersionsService {

    void saveOrUpdate(Versions versions);

    Versions findById(Long id);
}
