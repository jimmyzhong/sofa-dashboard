package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopLotsCategoryMngFacade;
import me.izhong.jobs.model.ShopLotsCategory;
import me.izhong.shop.dao.LotsCategoryDao;
import me.izhong.shop.entity.LotsCategory;
import me.izhong.shop.service.ILotsCategoryService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopLotsCategoryMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopLotsCategoryMngFacadeImpl implements IShopLotsCategoryMngFacade {
	
	@Autowired
	private LotsCategoryDao lotsCategoryDao;

	@Autowired
	private ILotsCategoryService lotsCategoryService;

	@Override
	public void create(ShopLotsCategory shopLotsCategory) {
		LotsCategory lotsCategory = new LotsCategory();
		BeanUtils.copyProperties(shopLotsCategory, lotsCategory);
		lotsCategory.setCreateTime(LocalDateTime.now());
		lotsCategory.setUpdateTime(LocalDateTime.now());
		lotsCategoryService.saveOrUpdate(lotsCategory);
	}

	@Override
	public void edit(ShopLotsCategory shopLotsCategory) {
		LotsCategory lotsCategory = lotsCategoryService.findById(shopLotsCategory.getId());
		lotsCategory.setName(shopLotsCategory.getName());
		lotsCategory.setLogo(shopLotsCategory.getLogo());
		lotsCategory.setPassword(shopLotsCategory.getPassword());
		lotsCategory.setAdmin(shopLotsCategory.getAdmin());
		lotsCategory.setUpdateTime(LocalDateTime.now());
		lotsCategoryService.saveOrUpdate(lotsCategory);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Integer[] uids = Convert.toIntArray(ids);
			for (Integer uid : uids) {
				lotsCategoryService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopLotsCategory> pageList(PageRequest request, String name) {
		LotsCategory lotsCategory = new LotsCategory();
		if (!StringUtils.isEmpty(name)) {
			lotsCategory.setName(name);
		}
		Specification<LotsCategory> specification = getLotsCategoryQuerySpeci(lotsCategory);
		return getLotsCategoryPageModel(request, specification);
	}

    private Specification<LotsCategory> getLotsCategoryQuerySpeci(LotsCategory lotsCategory) {
        return (r, cq, cb) -> {
        	List<Predicate> predicates = Lists.newArrayList();
        	if (!StringUtils.isEmpty(lotsCategory.getName())) {
        		predicates.add(cb.like(r.get("name"), "%" + lotsCategory.getName() + "%"));
        	}
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private PageModel<ShopLotsCategory> getLotsCategoryPageModel(PageRequest pageRequest, Specification<LotsCategory> specification) {
    	Page<LotsCategory> page = lotsCategoryDao.findAll(specification, PageableConvertUtil.toDataPageable(pageRequest));
        List<ShopLotsCategory> list = page.getContent().stream().map(t -> {
        	ShopLotsCategory shopLotsCategory = new ShopLotsCategory();
            BeanUtils.copyProperties(t, shopLotsCategory);
            return shopLotsCategory;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
    }

	@Override
	public ShopLotsCategory find(Integer id) {
		LotsCategory lotsCategory = lotsCategoryService.findById(id);
		ShopLotsCategory shopLotsCategory = new ShopLotsCategory();
		BeanUtils.copyProperties(lotsCategory, shopLotsCategory);
		return shopLotsCategory;
	}
}
