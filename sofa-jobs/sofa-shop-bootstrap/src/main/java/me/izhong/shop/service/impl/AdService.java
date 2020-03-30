package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import java.util.List;

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
import me.izhong.shop.dao.AdDao;
import me.izhong.shop.entity.Ad;
import me.izhong.shop.service.IAdService;

@Service
public class AdService implements IAdService {

	@Autowired
	private AdDao adDao;

	@Override
	@Transactional
	public void saveOrUpdate(Ad ad) {
		adDao.save(ad);
	}

	@Override
	@Transactional
	public void deleteById(Long adId) {
		adDao.deleteById(adId);
	}

	@Override
	@Transactional
	public void updateStatus(List<Long> ids, Integer status) {
		for (Long id : ids) {
			adDao.updateStatusById(id, status);
		}
	}

	@Override
	public Ad findById(Long adId) {
		return adDao.findById(adId).orElseThrow(()-> BusinessException.build("找不到广告" + adId));
	}

	@Override
	@Transactional(readOnly=true)
	public PageModel<Ad> pageList(PageRequest request, String name, String content, String status) {
		Ad ad = new Ad();
		ad.setPosition(1);
		if (!StringUtils.isEmpty(name)) {
			ad.setAdName(name);
		}
		if (!StringUtils.isEmpty(content)) {
			ad.setContent(content);
		}
		if (!StringUtils.isEmpty(status)) {
			ad.setStatus(Integer.valueOf(status));
		} else {
			ad.setStatus(1);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("status", ExampleMatcher.GenericPropertyMatchers.exact())
				.withMatcher("adName", ExampleMatcher.GenericPropertyMatchers.contains())
				.withMatcher("content", ExampleMatcher.GenericPropertyMatchers.contains());

		Example<Ad> example = Example.of(ad, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
			sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					request.getOrderByColumn());
		}

		Pageable pageableReq = of(
				Long.valueOf(request.getPageNum()-1).intValue(),
				Long.valueOf(request.getPageSize()).intValue(), sort);
		Page<Ad> adPage = adDao.findAll(example, pageableReq);
		adPage.getContent().stream().forEach(ads->ads.setContent(null));
		return PageModel.instance(adPage.getTotalElements(), adPage.getContent());
	}

}
