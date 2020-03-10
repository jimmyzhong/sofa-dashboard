package me.izhong.shop.service;

import java.util.List;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.Ad;

public interface IAdService {

    void saveOrUpdate(Ad ad);

	void deleteById(Long adId);

	void updateStatus(List<Long> ids, Integer status);

    Ad findById(Long adId);

    PageModel<Ad> pageList(PageRequest request, String name, String content, String status);
}
