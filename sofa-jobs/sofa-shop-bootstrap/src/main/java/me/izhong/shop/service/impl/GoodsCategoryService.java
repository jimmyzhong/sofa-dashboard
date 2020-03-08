package me.izhong.shop.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import me.izhong.common.domain.PageModel;
import me.izhong.shop.dto.GoodsCategoryDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IGoodsCategoryService;

@Slf4j
@Service
public class GoodsCategoryService implements IGoodsCategoryService {
	
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	@Override
	@Transactional
	public GoodsCategory saveOrUpdate(GoodsCategory goodsCategory) {
		return goodsCategoryDao.save(goodsCategory);
	}

	@Override
	@Transactional
	public GoodsCategory saveOrUpdateWithPathInfo(GoodsCategory goodsCategory) {
		GoodsCategory category = goodsCategoryDao.save(goodsCategory);
		String parentPath = null;
		if (category.getParentId() != 0) {
			parentPath = goodsCategoryDao.findById(category.getParentId()).orElseGet(GoodsCategory::new).getPath();
		}
		String path = (parentPath == null ? "" : parentPath) + category.getId() + "/";
		category.setPath(path);
		return goodsCategoryDao.save(category);
	}

	@Override
	public GoodsCategory findById(Long categoryId) {
		return goodsCategoryDao.findById(categoryId).orElseThrow(() -> new RuntimeException("unable to find goodsCategory by " + categoryId));
	}

	@Override
	public GoodsCategory findByChildrenId(Long categoryId) {
		return goodsCategoryDao.findByChildrenId(categoryId);
	}

	@Override
	public List<GoodsCategory> findByParentId(Long parentId) {
		return goodsCategoryDao.findByParentId(parentId);
	}

	@Override
	public List<GoodsCategory> findByLevel1() {
		return goodsCategoryDao.findByLevelAndShowStatus(0, 1);
	}

	@Override
	@Transactional
	public void updateShowStatusByIds(List<Long> ids, Integer showStatus) {
		goodsCategoryDao.updateShowStatus(ids, showStatus);
	}

	@Override
	@Transactional
	public void deleteById(Long categoryId) {
		goodsCategoryDao.deleteById(categoryId);
	}

	@Override
	public PageModel<GoodsCategoryDTO> list(PageQueryParamDTO pageQuery) {
		GoodsCategory category = new GoodsCategory();
		if (!StringUtils.isEmpty(pageQuery.getCategoryPath())) {
			category.setPath(pageQuery.getCategoryPath());
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("path", ExampleMatcher.GenericPropertyMatchers.startsWith());

		Example<GoodsCategory> example = Example.of(category, matcher);
		Sort sort = Sort.unsorted();
		if (!StringUtils.isEmpty(pageQuery.getOrderByColumn()) && !StringUtils.isEmpty(pageQuery.getIsAsc())) {
			sort = Sort.by("asc".equalsIgnoreCase(pageQuery.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					pageQuery.getOrderByColumn());
		}
		Pageable pageableReq = PageRequest.of(Long.valueOf(pageQuery.getPageNum()-1).intValue(),
				Long.valueOf(pageQuery.getPageSize()).intValue(), sort);

		Page<GoodsCategory> page = goodsCategoryDao.findAll(example, pageableReq);
		List<GoodsCategoryDTO> dtoList = page.getContent().stream().map(c->{
			GoodsCategoryDTO dto = new GoodsCategoryDTO();
			BeanUtils.copyProperties(c, dto);
			return dto;
		}).collect(Collectors.toList());
		return PageModel.instance(page.getTotalElements(), dtoList);
	}
}
