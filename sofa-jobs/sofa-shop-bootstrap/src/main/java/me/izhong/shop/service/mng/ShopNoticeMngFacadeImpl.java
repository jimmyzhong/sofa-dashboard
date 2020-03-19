package me.izhong.shop.service.mng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.util.Convert;
import me.izhong.jobs.manage.IShopNoticeMngFacade;
import me.izhong.jobs.model.ShopNotice;
import me.izhong.shop.dao.NoticeDao;
import me.izhong.shop.entity.Notice;
import me.izhong.shop.service.INoticeService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopNoticeMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopNoticeMngFacadeImpl implements IShopNoticeMngFacade {

	@Autowired
	private NoticeDao noticeDao;
	@Autowired
	private INoticeService noticeService;

	@Override
	public void create(ShopNotice shopNotice) {
		Notice notice = new Notice();
		BeanUtils.copyProperties(shopNotice, notice);
		notice.setCreateTime(LocalDateTime.now());
		notice.setUpdateTime(LocalDateTime.now());
		noticeService.saveOrUpdate(notice);
	}

	@Override
	public void edit(ShopNotice shopNotice) {
		Notice notice = noticeService.findById(shopNotice.getId());
		notice.setTitle(shopNotice.getTitle());
		notice.setDescription(shopNotice.getDescription());
		notice.setContent(shopNotice.getContent());
		notice.setStatus(shopNotice.getStatus());
		notice.setIsTop(shopNotice.getIsTop());
		notice.setUpdateTime(LocalDateTime.now());
		noticeService.saveOrUpdate(notice);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				noticeService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopNotice> pageList(PageRequest request, String title, Integer status) {
		Notice notice = new Notice();
		if (!StringUtils.isEmpty(title)) {
			notice.setTitle(title);
		}
		if (!StringUtils.isEmpty(title)) {
			notice.setStatus(status);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<Notice> example = Example.of(notice, matcher);

        Page<Notice> page = noticeDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopNotice> list = page.getContent().stream().map(t -> {
        	ShopNotice obj = new ShopNotice();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopNotice find(Long id) {
		Notice notice = noticeService.findById(id);
		ShopNotice obj = new ShopNotice();
        BeanUtils.copyProperties(notice, obj);
        return obj;
	}
}
