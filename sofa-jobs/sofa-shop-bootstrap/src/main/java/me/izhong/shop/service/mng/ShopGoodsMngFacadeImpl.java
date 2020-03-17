package me.izhong.shop.service.mng;

import static org.springframework.data.domain.PageRequest.of;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import io.jsonwebtoken.lang.Collections;
import me.izhong.shop.util.PageableConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IShopGoodsMngFacade;
import me.izhong.jobs.model.ShopGoods;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.dto.GoodsDTO;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IGoodsCategoryService;
import me.izhong.shop.service.IGoodsService;

@Slf4j
@Service
@SofaService(interfaceType = IShopGoodsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopGoodsMngFacadeImpl implements IShopGoodsMngFacade {
	
	@Autowired
	private GoodsDao goodsDao;
	
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsCategoryService goodsCategoryService;

	@Override
	public ShopGoods find(Long goodsId) {
		GoodsDTO goods = goodsService.findById(goodsId);
		ShopGoods shopGoods = new ShopGoods();
        BeanUtils.copyProperties(goods, shopGoods);
        if (!Collections.isEmpty(goods.getAlbumPics())) {
            shopGoods.setAlbumPics(goods.getAlbumPics());
        } else {
            shopGoods.setAlbumPics(Lists.newArrayList());
        }
        GoodsCategory categorySecondary = goodsCategoryService.findByChildrenId(goods.getProductCategoryId());
        if (categorySecondary != null) {
        	shopGoods.setProductCategoryId(categorySecondary.getId());
        	shopGoods.setProductCategorySecondaryId(goods.getProductCategoryId());
        	shopGoods.setProductCategorySecondaryName(categorySecondary.getName());
        } else {
        	shopGoods.setProductCategorySecondaryId(0L);
        	shopGoods.setProductCategorySecondaryName("");
        }
        return shopGoods;
	}

	@Override
	public void edit(ShopGoods shopGoods) {
		Goods goods = new Goods();
		BeanUtils.copyProperties(shopGoods, goods);
		if (!CollectionUtils.isEmpty(shopGoods.getAlbumPics())) {
			goods.setAlbumPics(StringUtils.join(shopGoods.getAlbumPics().toArray(), ","));
		}
		setCategoryPathInfo(goods);
		goodsService.saveOrUpdate(goods);
	}

	@Override
	public void updatePublishStatus(List<Long> ids, Integer publishStatus) {
		goodsService.updatePublishStatusByIds(ids, publishStatus);
	}

	@Override
	public void updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
		goodsService.updateRecommendStatusByIds(ids, recommendStatus);		
	}

	@Override
	public void updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
		goodsService.updateIsDeleteByIds(ids, deleteStatus);
	}

	@Override
	public PageModel<ShopGoods> pageList(PageRequest request, ShopGoods shopGoods) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(shopGoods, goods);
        removeWhiteSpaceParam(goods);

        Example<Goods> example = Example.of(goods);

        Page<Goods> userPage = goodsDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopGoods> shopGoodList = userPage.getContent().stream().map(t -> {
        	ShopGoods obj = new ShopGoods();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(userPage.getTotalElements(), shopGoodList);
	}

	@Override
	public boolean remove(Long goodsId) {
		try {
			goodsService.deleteById(goodsId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

    private void removeWhiteSpaceParam(Goods goods) {
        Field[] fields = Goods.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(goods);
                    if (StringUtils.isWhitespace(value)) {
                        field.set(goods, null);
                    }
                    field.setAccessible(false);
                }
            }
        } catch (Exception e) {
        }
    }

	@Override
	public void create(ShopGoods shopGoods) {
		Goods goods = new Goods();
		BeanUtils.copyProperties(shopGoods, goods);
		if (!CollectionUtils.isEmpty(shopGoods.getAlbumPics())) {
			goods.setAlbumPics(StringUtils.join(shopGoods.getAlbumPics().toArray(), ","));
		}
		setCategoryPathInfo(goods);
		goodsService.saveOrUpdate(goods);
	}

	private void setCategoryPathInfo(Goods goods) {
		if (goods.getProductCategoryId() != null) {
			GoodsCategory cat = goodsCategoryService.findById(goods.getProductCategoryId());
			goods.setCategoryPath(cat.getPath());
		}
	}
}