package me.izhong.shop.service.impl;

import java.lang.reflect.Field;
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
import me.izhong.jobs.manage.IShopGoodsMngFacade;
import me.izhong.jobs.model.ShopGoods;
import me.izhong.shop.dao.GoodsDao;
import me.izhong.shop.entity.Goods;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IGoodsService;

import static org.springframework.data.domain.PageRequest.of;

@Slf4j
@Service
@SofaService(interfaceType = IShopGoodsMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopGoodsMngFacadeImpl implements IShopGoodsMngFacade {
	
	@Autowired
	private GoodsDao goodsDao;
	
	@Autowired
	private IGoodsService goodsService;

	@Override
	public ShopGoods find(Long goodsId) {
		Goods goods = goodsService.findById(goodsId);
		ShopGoods shopGoods = new ShopGoods();
        BeanUtils.copyProperties(goods, shopGoods);
        return shopGoods;
	}

	@Override
	public void edit(ShopGoods goods) {
		Goods dbGoods = goodsService.findById(goods.getId());
		BeanUtils.copyProperties(goods, dbGoods);
		goodsService.saveOrUpdate(dbGoods);
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
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        Pageable pageableReq = of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<Goods> userPage = goodsDao.findAll(example, pageableReq);
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
        Field[] fields = User.class.getDeclaredFields();
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
		goodsService.saveOrUpdate(goods);
	}
}
