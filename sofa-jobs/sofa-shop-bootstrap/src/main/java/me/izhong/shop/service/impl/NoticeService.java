package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.NoticeDao;
import me.izhong.shop.entity.Notice;
import me.izhong.shop.service.INoticeService;

@Service
public class NoticeService implements INoticeService {

	@Autowired
	private NoticeDao noticeDao;

	@Override
	@Transactional
	public void saveOrUpdate(Notice notice) {
		noticeDao.save(notice);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		noticeDao.deleteById(id);
	}

	@Override
	public Notice findById(Long id) {
		return noticeDao.findById(id).orElseThrow(()-> BusinessException.build("找不到公告" + id));
	}

	@Override
	@Transactional(readOnly=true)
	public PageModel<Notice> pageList(PageRequest request, String title, Integer status) {
		Notice notice = new Notice();
		if (!StringUtils.isEmpty(title)) {
			notice.setTitle(title);
		}
		if (status != null) {
			notice.setStatus(status);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());

		Example<Notice> example = Example.of(notice, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
			sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					request.getOrderByColumn());
		}

		Pageable pageableReq = of(
				Long.valueOf(request.getPageNum()-1).intValue(),
				Long.valueOf(request.getPageSize()).intValue(), sort);
		Page<Notice> page = noticeDao.findAll(example, pageableReq);
		return PageModel.instance(page.getTotalElements(), page.getContent());
	}

}
