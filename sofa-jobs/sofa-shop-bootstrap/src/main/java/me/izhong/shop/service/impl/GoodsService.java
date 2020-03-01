package me.izhong.shop.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import me.izhong.common.domain.PageModel;
import me.izhong.shop.dao.GoodsAttributesDao;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.GoodsAttributes;
import me.izhong.shop.entity.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.service.IGoodsService;

@Slf4j
@Service
public class GoodsService implements IGoodsService {

	@Autowired
	private GoodsDao goodsDao;
	@Autowired
	private GoodsAttributesDao attributesDao;

	@Override
	@Transactional
	public Goods saveOrUpdate(Goods goods) {
		return goodsDao.save(goods);
	}

	@Override
	public Goods findById(Long goodsId) {
		return goodsDao.findById(goodsId).orElseThrow(() -> new RuntimeException("unable to find goods by " + goodsId));
	}

	@Override
	public void updatePublishStatusByIds(List<Long> ids, Integer publishStatus) {
		goodsDao.updatePublishStatus(ids, publishStatus);
	}

	@Override
	public void updateRecommendStatusByIds(List<Long> ids, Integer recommendStatus) {
		goodsDao.updateRecommendStatus(ids, recommendStatus);
	}

	@Override
	public void updateIsDeleteByIds(List<Long> ids, Integer deleteStatus) {
		goodsDao.updateIsDelete(ids, deleteStatus);
	}

	@Override
	public void deleteById(Long goodsId) {
		goodsDao.deleteById(goodsId);
	}

	@Override
    public void checkGoodsName(Goods goods, String goodsName) {
        if (!StringUtils.isEmpty(goodsName) && !StringUtils.equals(goods.getProductName(), goodsName)) {
            if (goodsDao.findByName(goodsName) != null ) {
                throw BusinessException.build(goodsName  + "已经存在");
            }
        }
        goods.setProductName(goodsName);
    }

	@Override
	public PageModel<GoodsDTO> list(PageQueryParamDTO queryParam) {
		Goods goods = new Goods();
		if (!StringUtils.isEmpty(queryParam.getQuery())) {
			goods.setProductName(queryParam.getQuery());
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());

		Example<Goods> example = Example.of(goods, matcher);

		Sort sort = Sort.unsorted();
		// TODO 限制排序的列名
		if (!StringUtils.isEmpty(queryParam.getOrderByColumn()) && !StringUtils.isEmpty(queryParam.getIsAsc())) {
			sort = Sort.by("asc".equalsIgnoreCase(queryParam.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					queryParam.getOrderByColumn());
		}

		Pageable pageableReq = PageRequest.of(Long.valueOf(queryParam.getPageNum()-1).intValue(),
				Long.valueOf(queryParam.getPageSize()).intValue(), sort);
		Page<Goods> page = goodsDao.findAll(example, pageableReq);
		List<GoodsDTO> dtoList = page.getContent().stream().map(g->GoodsDTO.builder()
				.id(g.getId()).productName(g.getProductName()).price(g.getPrice())
				.promotionPrice(g.getPromotionPrice()).productSn(g.getProductSn())
				.productPic(g.getProductPic()).build())
				.collect(Collectors.toList());
		return PageModel.instance(page.getTotalElements(), dtoList);
	}

	@Override
	public GoodsDTO findGoodsWithAttrById(Long goodsId) {
		Goods goods = findById(goodsId);
		List<GoodsAttributes> attributes = attributesDao.findGoodsAttributesByProductId(goodsId);
		GoodsDTO dto = new GoodsDTO();
		BeanUtils.copyProperties(goods, dto);
		dto.setAttributes(attributes);
		return dto;
	}
}
