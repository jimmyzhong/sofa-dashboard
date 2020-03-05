package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.jobs.dto.CategoryDTO;
import me.izhong.jobs.manage.IShopGoodsCategoryMngFacade;
import me.izhong.jobs.model.ShopGoodsCategory;
import me.izhong.shop.dao.GoodsCategoryDao;
import me.izhong.shop.entity.GoodsCategory;
import me.izhong.shop.service.IGoodsCategoryService;

@Slf4j
@Service
@SofaService(interfaceType = IShopGoodsCategoryMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopGoodsCategoryMngFacadeImpl implements IShopGoodsCategoryMngFacade {
	
	@Autowired
	private GoodsCategoryDao goodsCategoryDao;

	@Autowired
	private IGoodsCategoryService goodsCategoryService;

	@Override
	public ShopGoodsCategory findById(Long categoryId) {
		GoodsCategory goodsCategory = goodsCategoryService.findById(categoryId);
		ShopGoodsCategory obj = new ShopGoodsCategory();
        BeanUtils.copyProperties(goodsCategory, obj);
        return obj;
	}

	@Override
	public void create(ShopGoodsCategory shopGoodsCategory) {
		GoodsCategory goodsCategory = new GoodsCategory();
		goodsCategory.setProductCount(0);
		BeanUtils.copyProperties(shopGoodsCategory, goodsCategory);
		setCategoryLevel(goodsCategory);
		goodsCategoryService.saveOrUpdate(goodsCategory);
	}

	@Override
	public void edit(ShopGoodsCategory shopGoodsCategory) {
		GoodsCategory goodsCategory = new GoodsCategory();
		BeanUtils.copyProperties(shopGoodsCategory, goodsCategory);
		setCategoryLevel(goodsCategory);
		goodsCategoryService.saveOrUpdate(goodsCategory);
	}

	@Override
	public void updateShowStatus(List<Long> ids, Integer showStatus) {
		goodsCategoryService.updateShowStatusByIds(ids, showStatus);
	}

	@Override
	public boolean remove(Long categoryId) {
		try {
			goodsCategoryService.deleteById(categoryId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public PageModel<ShopGoodsCategory> pageList(PageRequest request, Long parentId) {
		GoodsCategory goodsCategory = new GoodsCategory();
		goodsCategory.setParentId(parentId);
        Example<GoodsCategory> example = Example.of(goodsCategory);
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        Pageable pageableReq = of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<GoodsCategory> userPage = goodsCategoryDao.findAll(example, pageableReq);
        List<ShopGoodsCategory> shopGoodCategoryList = userPage.getContent().stream().map(t -> {
        	ShopGoodsCategory obj = new ShopGoodsCategory();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(userPage.getTotalElements(), shopGoodCategoryList);
	}

	@Override
	public List<CategoryDTO> queryLevel1() {
		List<GoodsCategory> list = goodsCategoryService.findByLevel1();
        return list.stream().map(t -> {
        	CategoryDTO dto = new CategoryDTO();
        	dto.setValue(t.getId());
        	dto.setLabel(t.getName());
            return dto;
        }).collect(Collectors.toList());
	}

	@Override
	public List<CategoryDTO> queryAll() {
		List<GoodsCategory> level1List = goodsCategoryService.findByLevel1();
        return level1List.stream().map(t -> {
        	CategoryDTO dto = new CategoryDTO();
        	dto.setLabel(t.getName());
        	dto.setValue(t.getId());
            List<GoodsCategory> level2List = goodsCategoryService.findByParentId(t.getId());
			List<CategoryDTO> children = new ArrayList<>(level2List.size());
			for (GoodsCategory goodsCategory : level2List) {
				CategoryDTO obj = new CategoryDTO();
				obj.setLabel(goodsCategory.getName());
				obj.setValue(goodsCategory.getId());
				children.add(obj);
			}
            dto.setChildren(children);
            return dto;
        }).collect(Collectors.toList());
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
