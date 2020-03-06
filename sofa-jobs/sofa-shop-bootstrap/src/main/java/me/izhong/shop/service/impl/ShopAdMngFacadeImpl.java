package me.izhong.shop.service.impl;

import static org.springframework.data.domain.PageRequest.of;

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
		adService.saveOrUpdate(ad);
	}

	@Override
	public void edit(ShopAd shopAd) {
		Ad ad = adService.findById(shopAd.getId());
		BeanUtils.copyProperties(shopAd, ad);
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
			return false;
		}
	}

	@Override
	public PageModel<ShopAd> pageList(PageRequest request, String name, String content) {
		Ad ad = new Ad();
		ad.setAdName(name);
		ad.setContent(content);
		ad.setPosition(1);
		ad.setStatus(1);

        Example<Ad> example = Example.of(ad);
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getIsAsc())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getIsAsc()) ? Sort.Direction.ASC: Sort.Direction.DESC,
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
