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
import me.izhong.jobs.manage.IShopAdMngFacade;
import me.izhong.jobs.model.ShopAd;
import me.izhong.shop.dao.AdDao;
import me.izhong.shop.entity.Ad;
import me.izhong.shop.service.IAdService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopAdMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopAdMngFacadeImpl implements IShopAdMngFacade {
	
	@Autowired
	private AdDao adDao;

	@Autowired
	private IAdService adService;

	@Override
	public void create(ShopAd shopAd) {
		Ad ad = new Ad();
		BeanUtils.copyProperties(shopAd, ad);
		ad.setPosition(1);
		ad.setCreateTime(LocalDateTime.now());
		ad.setUpdateTime(LocalDateTime.now());
		adService.saveOrUpdate(ad);
	}

	@Override
	public void edit(ShopAd shopAd) {
		Ad ad = adService.findById(shopAd.getId());
		ad.setAdName(shopAd.getAdName());
		ad.setAdType(shopAd.getAdType());
		ad.setAdLink(shopAd.getAdLink());
		ad.setStatus(shopAd.getStatus());
		if (!StringUtils.isEmpty(shopAd.getImageUrl())) {
			ad.setImageUrl(shopAd.getImageUrl());
		}
		if (!StringUtils.isEmpty(shopAd.getContent())) {
			ad.setContent(shopAd.getContent());
		}
		ad.setSort(shopAd.getSort());
		ad.setUpdateTime(LocalDateTime.now());
		adService.saveOrUpdate(ad);
	}

	@Override
	public void updateShowStatus(List<Long> ids, Integer showStatus) {
		adService.updateStatus(ids, showStatus);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				adService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopAd> pageList(PageRequest request, String name, Integer status) {
		Ad ad = new Ad();
		if (!StringUtils.isEmpty(name)) {
			ad.setAdName(name);
		}
		if (status != null) {
			ad.setStatus(status);
		}
		Specification<Ad> specification = getAdQuerySpeci(ad);
		return getAdPageModel(request, specification);
	}

    private Specification<Ad> getAdQuerySpeci(Ad ad) {
        return (r, cq, cb) -> {
        	List<Predicate> predicates = Lists.newArrayList();
        	if (!StringUtils.isEmpty(ad.getAdName())) {
        		predicates.add(cb.like(r.get("adName"), "%" + ad.getAdName() + "%"));
        	}
        	if (ad.getStatus() != null) {
        		predicates.add(cb.equal(r.get("status"), ad.getStatus()));
        	}
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private PageModel<ShopAd> getAdPageModel(PageRequest pageRequest, Specification<Ad> specification) {
    	Page<Ad> page = adDao.findAll(specification, PageableConvertUtil.toDataPageable(pageRequest));
        List<ShopAd> list = page.getContent().stream().map(t -> {
        	ShopAd shopAd = new ShopAd();
            BeanUtils.copyProperties(t, shopAd);
            return shopAd;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
    }

	@Override
	public ShopAd find(Long adId) {
		Ad ad = adService.findById(adId);
		ShopAd shopAd = new ShopAd();
        BeanUtils.copyProperties(ad, shopAd);
        return shopAd;
	}
}
