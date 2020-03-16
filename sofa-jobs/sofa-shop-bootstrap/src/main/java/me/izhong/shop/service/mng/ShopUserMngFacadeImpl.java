package me.izhong.shop.service.mng;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.manage.IShopUserMngFacade;
import me.izhong.jobs.model.ShopUser;
import me.izhong.shop.dao.UserDao;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.util.PageableConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@SofaService(interfaceType = IShopUserMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class ShopUserMngFacadeImpl implements IShopUserMngFacade {

    @Autowired
    UserDao userDao;

    @Autowired
    IUserService userService;

    @Override
    public ShopUser find(Long userId) {
        User user = userService.findById(userId);
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

        ExampleMatcher userMatcher = ExampleMatcher.matchingAny()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.ignoreCase())
                .withIgnorePaths("password");

        Example<User> example = Example.of(user, userMatcher);

        Page<User> userPage = userDao.findAll(example, PageableConvertUtil.toDataPageable(request));
        List<ShopUser> shopUsers = userPage.getContent().stream().map(u->{
            ShopUser suser = new ShopUser();
            BeanUtils.copyProperties(u, suser);
            return suser;
        }).collect(Collectors.toList());
        return PageModel.instance(userPage.getTotalElements(), shopUsers);
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
    public boolean remove(Long userId) {
        Optional<User> user = userDao.findById(userId);
        if (user.isPresent()){
            userDao.delete(user.get());
            return true;
        }
        return false;
    }
}
