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
import me.izhong.jobs.manage.IShopTemplateMngFacade;
import me.izhong.jobs.model.ShopTemplate;
import me.izhong.shop.dao.TemplateDao;
import me.izhong.shop.entity.Template;
import me.izhong.shop.service.ITemplateService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopTemplateMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopTemplateMngFacadeImpl implements IShopTemplateMngFacade {

	@Autowired
	private TemplateDao templateDao;
	@Autowired
	private ITemplateService templateService;

	@Override
	public void create(ShopTemplate shopTemplate) {
		Template template = new Template();
		BeanUtils.copyProperties(shopTemplate, template);
		template.setCreateTime(LocalDateTime.now());
		template.setUpdateTime(LocalDateTime.now());
		templateService.saveOrUpdate(template);
	}

	@Override
	public void edit(ShopTemplate shopTemplate) {
		Template template = templateService.findById(shopTemplate.getId());
		template.setTitle(shopTemplate.getTitle());
		template.setContent(shopTemplate.getContent());
		template.setUpdateTime(LocalDateTime.now());
		templateService.saveOrUpdate(template);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				templateService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopTemplate> pageList(PageRequest request, String title) {
		Template template = new Template();
		if (!StringUtils.isEmpty(title)) {
			template.setTitle(title);
		}

		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<Template> example = Example.of(template, matcher);

        Page<Template> page = templateDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopTemplate> list = page.getContent().stream().map(t -> {
        	ShopTemplate obj = new ShopTemplate();
            BeanUtils.copyProperties(t, obj);
            return obj;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
	}

	@Override
	public ShopTemplate find(Long id) {
		Template template = templateService.findById(id);
		ShopTemplate obj = new ShopTemplate();
        BeanUtils.copyProperties(template, obj);
        return obj;
	}
}
