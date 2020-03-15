package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

import java.time.LocalDateTime;
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
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopAdMngFacade;
import me.izhong.jobs.model.ShopAd;
import me.izhong.shop.dao.AdDao;
import me.izhong.shop.entity.Ad;
import me.izhong.shop.service.IAdService;

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
	public PageModel<ShopAd> pageList(PageRequest request, String name, String content, String status) {
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
		}

        Example<Ad> example = Example.of(ad);
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        Pageable pageableReq = of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
        Page<Ad> userPage = adDao.findAll(example, pageableReq);
        List<ShopAd> shopGoodCategoryList = userPage.getContent().stream().map(t -> {
        	ShopAd obj = new ShopAd();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(userPage.getTotalElements(), shopGoodCategoryList);
	}

	@Override
	public ShopAd find(Long adId) {
		Ad ad = adService.findById(adId);
		ShopAd shopAd = new ShopAd();
        BeanUtils.copyProperties(ad, shopAd);
        return shopAd;
	}
}
