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
import me.izhong.jobs.manage.IShopSuppliersMngFacade;
import me.izhong.jobs.model.ShopSuppliers;
import me.izhong.shop.dao.SuppliersDao;
import me.izhong.shop.entity.Suppliers;
import me.izhong.shop.service.ISuppliersService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopSuppliersMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopSuppliersMngFacadeImpl implements IShopSuppliersMngFacade {

	@Autowired
	private SuppliersDao suppliersDao;

	@Autowired
	private ISuppliersService suppliersService;

	@Override
	public void create(ShopSuppliers shopSuppliers) {
		Suppliers suppliers = new Suppliers();
		BeanUtils.copyProperties(shopSuppliers, suppliers);
		suppliers.setCreateTime(LocalDateTime.now());
		suppliers.setUpdateTime(LocalDateTime.now());
		suppliersService.saveOrUpdate(suppliers);
	}

	@Override
	public void edit(ShopSuppliers shopSuppliers) {
		Suppliers suppliers = suppliersService.findById(shopSuppliers.getId());
		suppliers.setName(shopSuppliers.getName());
		suppliers.setLogo(shopSuppliers.getLogo());
		suppliers.setDescription(shopSuppliers.getDescription());
		suppliers.setContent(shopSuppliers.getContent());
		suppliers.setUpdateTime(LocalDateTime.now());
		suppliersService.saveOrUpdate(suppliers);
	}

	@Override
	public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				suppliersService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
	}

	@Override
	public PageModel<ShopSuppliers> pageList(PageRequest request, String name) {
		Suppliers suppliers = new Suppliers();
		if (!StringUtils.isEmpty(name)) {
			suppliers.setName(name);
		}
		Specification<Suppliers> specification = getSuppliersQuerySpeci(suppliers);
		return getSuppliersPageModel(request, specification);
	}

    private Specification<Suppliers> getSuppliersQuerySpeci(Suppliers suppliers) {
        return (r, cq, cb) -> {
        	List<Predicate> predicates = Lists.newArrayList();
        	if (!StringUtils.isEmpty(suppliers.getName())) {
        		predicates.add(cb.like(r.get("name"), "%" + suppliers.getName() + "%"));
        	}
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private PageModel<ShopSuppliers> getSuppliersPageModel(PageRequest pageRequest, Specification<Suppliers> specification) {
    	Page<Suppliers> page = suppliersDao.findAll(specification, PageableConvertUtil.toDataPageable(pageRequest));
        List<ShopSuppliers> list = page.getContent().stream().map(t -> {
        	ShopSuppliers shopSuppliers = new ShopSuppliers();
            BeanUtils.copyProperties(t, shopSuppliers);
            return shopSuppliers;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
    }

	@Override
	public ShopSuppliers find(Long id) {
		Suppliers suppliers = suppliersService.findById(id);
		ShopSuppliers shopSuppliers = new ShopSuppliers();
        BeanUtils.copyProperties(suppliers, shopSuppliers);
        return shopSuppliers;
	}
}
