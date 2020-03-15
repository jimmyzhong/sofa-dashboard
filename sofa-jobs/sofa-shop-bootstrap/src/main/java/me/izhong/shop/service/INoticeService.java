package me.izhong.shop.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.shop.entity.Notice;

public interface INoticeService {

    void saveOrUpdate(Notice notice);

	void deleteById(Long id);

	Notice findById(Long id);

    PageModel<Notice> pageList(PageRequest request, String title, Integer status);
}
