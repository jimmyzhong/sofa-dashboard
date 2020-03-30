package me.izhong.shop.service.mng;

import java.lang.reflect.Field;
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
import me.izhong.jobs.manage.IShopUserMngFacade;
import me.izhong.jobs.model.ShopUser;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.util.PageableConvertUtil;

@Slf4j
@Service
@SofaService(interfaceType = IShopUserMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopUserMngFacadeImpl implements IShopUserMngFacade {

    @Autowired
    private UserDao userDao;

    @Autowired
    private IUserService userService;

    @Override
    public ShopUser find(Long userId) {
        User user = userService.findById(userId);
        System.out.println(user);
        ShopUser shopUser = new ShopUser();
        BeanUtils.copyProperties(user, shopUser);
        return shopUser;
    }

    @Override
    public boolean disable(Long userId) {
        userService.lock(userId, true);
        return true;
    }

    @Override
    public boolean enable(Long userId) {
        userService.lock(userId, false);
        return true;
    }

    @Override
    public ShopUser edit(ShopUser user) {
        User dbUser = userService.findById(user.getId());

        userService.attemptModifyPhone(dbUser, user.getPhone());
        //userService.attemptModifyEmail(dbUser, user.getEmail());
        //userService.attemptModifyLoginName(dbUser, user.getLoginName());
        userService.attemptModifyPassword(dbUser, user.getPassword());

        if (!StringUtils.isEmpty(user.getName())) {
            dbUser.setName(user.getName());
        }
        dbUser.setNickName(user.getNickName());
        userService.saveOrUpdate(dbUser);
        return user;
    }

    @Override
    public PageModel<ShopUser> pageList(PageRequest request, ShopUser shopUser) {
        User user = new User();
        BeanUtils.copyProperties(shopUser, user);
        removeWhiteSpaceParam(user);
        Specification<User> specification = getUserQuerySpeci(user);
        return getUserPageModel(request, specification);
    }

    private Specification<User> getUserQuerySpeci(User user) {
        return (r, cq, cb) -> {
        	List<Predicate> predicates = Lists.newArrayList();
        	if (!StringUtils.isEmpty(user.getNickName())) {
        		predicates.add(cb.like(r.get("nickName"), "%" + user.getNickName() + "%"));
        	}
        	if (!StringUtils.isEmpty(user.getPhone())) {
        		predicates.add(cb.like(r.get("phone"), "%" + user.getPhone() + "%"));
        	}
        	if (user.getInviteUserId() != null) {
        		predicates.add(cb.equal(r.get("inviteUserId"), user.getInviteUserId()));
        	}
        	if (user.getInviteUserId() != null) {
        		predicates.add(cb.equal(r.get("inviteUserId2"), user.getInviteUserId2()));
        	}
            if (user.getIsLocked() != null) {
            	predicates.add(cb.equal(r.<Boolean>get("isLocked"), user.getIsLocked()));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private PageModel<ShopUser> getUserPageModel(PageRequest pageRequest, Specification<User> specification) {
    	Page<User> page = userDao.findAll(specification, PageableConvertUtil.toDataPageable(pageRequest));
        List<ShopUser> list = page.getContent().stream().map(t -> {
            ShopUser shopUser = new ShopUser();
            BeanUtils.copyProperties(t, shopUser);
            return shopUser;
        }).collect(Collectors.toList());
        return PageModel.instance(page.getTotalElements(), list);
    }

    private void removeWhiteSpaceParam(User user) {
        Field[] fields = User.class.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    String value = (String) field.get(user);
                    if (StringUtils.isWhitespace(value)) {
                        field.set(user, null);
                    }
                    field.setAccessible(false);
                }
            }
        }catch (Exception e) {
            //ignore
        }
    }

    @Override
    public boolean remove(String ids) {
		try {
	    	Long[] uids = Convert.toLongArray(ids);
			for (Long uid : uids) {
				userService.deleteById(uid);
			}
			return true;
		} catch (Exception e) {
			log.info("delete error:", e);
			return false;
		}
    }
}
