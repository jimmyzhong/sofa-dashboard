package me.izhong.shop.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alipay.sofa.rpc.common.utils.JSONUtils;
import io.jsonwebtoken.lang.Collections;
import me.izhong.shop.consts.ProductTypeEnum;
import me.izhong.shop.dao.GoodsStoreDao;
import me.izhong.shop.entity.GoodsStore;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.dao.GoodsAttributesDao;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsAttributes;
import me.izhong.shop.service.IGoodsService;

@Slf4j
@Service
public class GoodsService implements IGoodsService {

	@Autowired
	private GoodsDao goodsDao;
	@Autowired
	private GoodsAttributesDao attributesDao;
	@Autowired
	private GoodsStoreDao storeDao;
	@Autowired
	private ResaleService resaleService;
	@Autowired private IUserService userService;

	@Override
	@Transactional
	public void saveOrUpdate(Goods goods) {
		// 默认普通商品
		if (goods.getProductType() == null) {
			goods.setProductType(ProductTypeEnum.NORMAL.getType());
		}
		// 默认非首页商品
		if (goods.getOnIndexPage() == null) {
			goods.setOnIndexPage(false);
		}
		Goods g = goodsDao.save(goods);
		saveGoodsStock(g);
	}

	private void saveGoodsStock(Goods g) {
		GoodsStore gs = storeDao.findByProductIdAndProductAttrId(g.getId(), null);
		if (gs == null) {
			gs = new GoodsStore();
		}
		gs.setProductId(g.getId());
		if (gs.getPreStore() != null && gs.getPreStore() > 0) {
			gs.setPreStore(gs.getPreStore() + (gs.getStore() - g.getStock()));
		} else {
			gs.setPreStore(g.getStock());
		}
		gs.setStore(g.getStock());
		storeDao.save(gs);
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
            if (goodsDao.findByProductName(goodsName) != null ) {
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
		if (!StringUtils.isEmpty(queryParam.getCategoryPath())) {
			goods.setCategoryPath(queryParam.getCategoryPath());
		}
		if (queryParam.getProductType() != null) {
			goods.setProductType(queryParam.getProductType());
		} else {
			goods.setProductType(null);
		}
		if (queryParam.getOnIndexPage() != null) {
			goods.setOnIndexPage(queryParam.getOnIndexPage());
		} else {
			goods.setOnIndexPage(null);
		}


		if (queryParam.getUserId() != null) {
			goods.setCreatedBy(queryParam.getUserId());
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("productName", ExampleMatcher.GenericPropertyMatchers.contains())
				.withMatcher("categoryPath", ExampleMatcher.GenericPropertyMatchers.startsWith());

		Example<Goods> example = Example.of(goods, matcher);

		Sort sort = Sort.unsorted();
		// TODO 限制排序的列名
		if (!StringUtils.isEmpty(queryParam.getOrderByColumn()) && !StringUtils.isEmpty(queryParam.getOrderDirection())) {
			sort = Sort.by("asc".equalsIgnoreCase(queryParam.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
					queryParam.getOrderByColumn());
		}

		Pageable pageableReq = PageRequest.of(Long.valueOf(queryParam.getPageNum()-1).intValue(),
				Long.valueOf(queryParam.getPageSize()).intValue(), sort);
		Page<Goods> page = goodsDao.findAll(example, pageableReq); // TODO join goods stock table to get latest stock number
		List<GoodsDTO> dtoList = page.getContent().stream().map(g-> {
			Long createdBy = g.getCreatedBy();
			User createdByUser = new User();
			createdByUser.setId(createdBy);
			if (createdBy != null) {
				// TODO join is better
				createdByUser = userService.findById(createdBy);
			}
			return GoodsDTO.builder()
					.id(g.getId()).productName(g.getProductName()).price(g.getPrice())
					.promotionPrice(g.getPromotionPrice()).productSn(g.getProductSn())
					.productPic(g.getProductPic()).productCategoryPath(g.getCategoryPath())
					.productType(g.getProductType()).originalPrice(g.getOriginalPrice())
					.stock(g.getStock()).nextPriceTime(formatDateTime(generateNextPriceTime(g)))
					.description(g.getDescription())
					.createdBy(createdByUser.getId()).avatarOfCreatedBy(createdByUser.getAvatar())
					.nameOfCreatedBy(createdByUser.getName())
					.build();
		}).collect(Collectors.toList());
		return PageModel.instance(page.getTotalElements(), dtoList);
	}

	private String formatDateTime(LocalDateTime dateTime) {
		if (dateTime != null) {
			return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		return null;
	}
	private LocalDateTime generateNextPriceTime(Goods g) {
		if (g.getProductType() != null && ProductTypeEnum.RESALE.getType() == g.getProductType()) {
			return resaleService.nextPriceTime(g.getCreateTime());
		}
		return null;
	}

	@Override
	public GoodsDTO findById(Long goodsId) {
		Goods goods = goodsDao.findById(goodsId).orElseThrow(() -> new RuntimeException("unable to find goods by " + goodsId));
		GoodsDTO dto = new GoodsDTO();
		BeanUtils.copyProperties(goods, dto, "albumPics");
		dto.setNextPriceTime(formatDateTime(generateNextPriceTime(goods)));
		if(!StringUtils.isEmpty(goods.getAlbumPics())) {
			dto.setAlbumPics(JSONArray.parseArray(goods.getAlbumPics(), String.class));
		}
		return dto;
	}

	@Override
	public GoodsDTO findGoodsWithAttrById(Long goodsId) {
		GoodsDTO dto = findById(goodsId);
		List<GoodsAttributes> attributes = attributesDao.findGoodsAttributesByProductId(goodsId);
		dto.setAttributes(CollectionUtils.isEmpty(attributes) ? Lists.newArrayList() : attributes);
		return dto;
	}

	@Override
	public GoodsDTO findGoodsWithAttrById(Long goodsId, Long goodsAttrId) {
		GoodsDTO dto = findById(goodsId);
		if (goodsAttrId != null) {
			Optional<GoodsAttributes> attr = attributesDao.findById(goodsAttrId);
			dto.setAttributes(attr.isPresent() ? Arrays.asList(attr.get()) : Lists.newArrayList());
		}
		return dto;
	}

	@Override
	public void updateStore(Long goodId, Long goodAttrId, Integer newStore) {

	}
}
