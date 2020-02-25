package me.izhong.shop.service.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.manage.IShopGoodsCategoryMngFacade;
import me.izhong.jobs.model.ShopGoodsCategory;
import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IGoodsCategoryService;
import me.izhong.shop.service.IGoodsService;

import static org.springframework.data.domain.PageRequest.of;

@Slf4j
@Service
@SofaService(interfaceType = IShopGoodsCategoryMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopGoodsCategoryMngFacadeImpl implements IShopGoodsCategoryMngFacade {
	
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsCategoryService goodsCategoryService;

	@Override
	public ShopGoodsCategory find(Long goodsId) {
		GoodsCategory goodsCategory = goodsCategoryService.findById(goodsId);
		ShopGoodsCategory obj = new ShopGoodsCategory();
        BeanUtils.copyProperties(goodsCategory, obj);
        return obj;
	}

	@Override
	public boolean disable(Long goodsId) {
		return false;
	}

	@Override
	public boolean enable(Long goodsId) {
		return false;
	}

	@Override
	public void edit(ShopGoodsCategory shopGoodsCategory) {
		GoodsCategory goodsCategory = new GoodsCategory();
		BeanUtils.copyProperties(shopGoodsCategory, goodsCategory);
		setCategoryLevel(goodsCategory);
		GoodsCategory obj = goodsCategoryService.findById(shopGoodsCategory.getId());
		if (!StringUtils.isEmpty(goodsCategory.getName()) && !StringUtils.equals(goodsCategory.getName(), obj.getName())) {
			if (obj.getParentId() != 0) {
				Goods goods = new Goods();
				goods.setProductCategoryId(obj.getId());
				goods.setProductCategoryName(obj.getName());
				goodsService.saveOrUpdate(goods);
			}
		}
		goodsCategoryService.saveOrUpdate(obj);
	}

	@Override
	public void updateShowStatus(List<Long> ids, Integer publishStatus) {
		goodsCategoryService.updateShowStatusByIds(publishStatus, ids);
	}

	@Override
	public PageModel<ShopGoodsCategory> pageList(PageRequest request, Long type, String name) {
		GoodsCategory goodsCategory = new GoodsCategory();
		goodsCategory.setName(name);
		goodsCategory.setParentId(type == 0L ? 0L : 1L);
        removeWhiteSpaceParam(goodsCategory);

        ExampleMatcher userMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains());

        Example<GoodsCategory> example = Example.of(goodsCategory, userMatcher);
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        Pageable pageableReq = of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<GoodsCategory> userPage = goodsCategoryDao.findAll(example, pageableReq);
        List<ShopGoodsCategory> shopGoodCategoryList = userPage.getContent().stream().map(t->{
        	ShopGoodsCategory obj = new ShopGoodsCategory();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(userPage.getTotalElements(), shopGoodCategoryList);
	}

	@Override
	public boolean remove(Long goodsId) {
		try {
			goodsCategoryService.deleteById(goodsId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void create(ShopGoodsCategory shopGoodsCategory) {
		GoodsCategory goodsCategory = new GoodsCategory();
		goodsCategory.setProductCount(0);
		BeanUtils.copyProperties(shopGoodsCategory, goodsCategory);
		setCategoryLevel(goodsCategory);
		goodsCategoryService.saveOrUpdate(goodsCategory);
	}

    private void removeWhiteSpaceParam(GoodsCategory goodsCategory) {
        Field[] fields = User.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(goodsCategory);
                    if (StringUtils.isWhitespace(value)) {
                        field.set(goodsCategory, null);
                    }
                    field.setAccessible(false);
                }
            }
        } catch (Exception e) {
        }
    }

    private void setCategoryLevel(GoodsCategory goodsCategory) {
        //没有父分类时为一级分类
        if (goodsCategory.getParentId() == 0L) {
        	goodsCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
        	GoodsCategory parentCategory = goodsCategoryService.findById(goodsCategory.getParentId());
            if (parentCategory != null) {
            	goodsCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
            	goodsCategory.setLevel(0);
            }
        }
    }
}
