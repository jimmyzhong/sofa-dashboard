package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.dao.*;
import me.izhong.dashboard.manage.entity.*;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.expection.user.UserNotFoundException;
import me.izhong.dashboard.manage.service.SysConfigService;
import me.izhong.dashboard.manage.service.SysPostService;
import me.izhong.dashboard.manage.service.SysRoleService;
import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.common.util.Convert;
import me.izhong.db.mongo.util.CriteriaUtil;
import me.izhong.dashboard.manage.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Service
@Slf4j
public class SysUserServiceImpl extends CrudBaseServiceImpl<Long,SysUser> implements SysUserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private DeptDao deptDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private UserPostDao userPostDao;

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private SysConfigService sysConfigService;

    @Override
    public SysUser login(String username, String password) {
        Assert.notNull(username, "用户名不能为空");
        Assert.notNull(password, "密码不能为空");
        SysUser user = userDao.findByLoginName(username);
        if (!StringUtils.equals(password, user.getPassword())) {
            throw new RuntimeException("密码不正确");
        }
        if (user != null)
            user.setPassword(null);
        return user;
    }

    @Override
    public SysUser findUser(Long id) {
        SysUser u = userDao.findByUserId(id);
        checkDeptModify(u);
        return u;
    }

    @Override
    public SysUser findUserByLoginName(String loginName) {
        Assert.notNull(loginName, "用户名不能为空");
        SysUser u = userDao.findByLoginName(loginName);
        checkDeptModify(u);
        return u;
    }


    private void checkDeptModify(SysUser u) {
        if (u == null)
            return;
        SysDept sysDept = deptDao.findByDeptId(u.getDeptId());
        if (sysDept != null) {
            if (!StringUtils.equalsIgnoreCase(u.getDeptName(), sysDept.getDeptName())) {
                u.setDeptName(sysDept.getDeptName());
                userDao.save(u);
            }
        }
    }

    @Override
    public SysUser findUserByEmail(String email) {
        Assert.notNull(email, "邮箱不能为空");
        List<SysUser> us = userDao.findByEmail(email);
        for (SysUser u : us) {
            if (u.getEmailLoginEnable() != null && u.getEmailLoginEnable().booleanValue())
                return u;
        }
        return null;
    }

    @Override
    public SysUser findUserByPhoneNumber(String phoneNumber) {
        Assert.notNull(phoneNumber, "手机号不能为空");
        List<SysUser> us = userDao.findByPhoneNumber(phoneNumber);
        for (SysUser u : us) {
            if (u.getPhoneNumberLoginEnable() != null && u.getPhoneNumberLoginEnable().booleanValue())
                return u;
        }
        return null;
    }

    @Transactional
    @Override
    public SysUser saveUserAndPerms(SysUser user) throws BusinessException {
        checkUserAllowed(user,"操作");
        user = saveUser(user);
        doUserPerms(user);
        return user;
    }

    @Transactional
    @Override
    public SysUser recordLoginIp(Long userId, String loginIp) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setLoginIp(loginIp);
            dbuser.setLoginDate(new Date());
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser updateMyInfos(Long userId,String userName,String email,String phoneNumber,String sex) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setUserName(userName);
            dbuser.setEmail(email);
            dbuser.setPhoneNumber(phoneNumber);
            dbuser.setSex(sex);
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser updateMyAvatar(Long userId,String avatar) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setAvatar(avatar);
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser saveUser(SysUser user) throws BusinessException {
        Assert.notNull(user, "用户不能为空");
        try {
            boolean isNew = true;
            SysUser dbuser;
            if (user.getUserId() != null) {
                dbuser = userDao.findByUserId(user.getUserId());
                if(dbuser != null) {
                    isNew = false;
                    user.setId(dbuser.getId());
                }
            }
            if (!checkLoginNameUnique(user)) {
                throw BusinessException.build(String.format("用户名字[%s]重复", user.getLoginName()));
            }

            if(isNew) {
                return super.insert(user);
            } else {
                return super.update(user);
            }
        } catch (Exception e) {
            log.error("", e);
            if (e instanceof BusinessException)
                throw e;
            throw BusinessException.build("用户保存失败" + e.getMessage(), e);
        }
    }

    @Override
    public List<SysUser> findUsersByUserIds(Long[] ids) {
        Assert.notNull(ids,"");
        return userDao.findAllByUserIdIn(ids);
    }

    @Override
    public PageModel getPage(PageRequest pageRequest, SysUser searchUser) {

        Query query = new Query();
        addSearchToQuery(searchUser, query);

        PageRequestUtil.injectQuery(pageRequest,query);

        return super.selectPage(query, pageRequest,searchUser);
    }

    @Override
    public long getListSize(SysUser searchUser, Date bTime, Date eTime) {
        Query query = new Query();

        addSearchToQuery(searchUser, query);

        return mongoTemplate.count(query, SysUser.class);
    }


    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Assert.notNull(user, "user 不能为空");
        Assert.notNull(user.getPhoneNumber(), "phoneNumber 不能为空");
        Long userId = user.getUserId() == null ? 0L : user.getUserId();
        SysUser sysUser = findUserByPhoneNumber(user.getPhoneNumber());
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkEmailUnique(SysUser user) {
        Assert.notNull(user, "user 不能为空");
        Assert.notNull(user.getEmail(), "email 不能为空");
        Long userId = user.getUserId() == null ? 0L : user.getUserId();
        SysUser sysUser = findUserByEmail(user.getEmail());
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkLoginNameUnique(SysUser user) {
        Assert.notNull(user, "user 不能为空");
        Assert.notNull(user.getLoginName(), "loginName 不能为空");
        Long userId = user.getUserId() == null ? 0L : user.getUserId();
        SysUser sysUser = findUserByLoginName(user.getLoginName());
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return false;
        }
        return true;
    }

    /**
     * @param roleId
     * @param deptIds 部门用户隔离
     * @return
     */
    @Override
    public PageModel<SysUser> selectAllocatedList(PageRequest request, Long roleId, SysUser user, List<Long> deptIds) {
        return doGetUserAllocatedList(request, roleId, user, deptIds, true);
    }

    @Override
    public PageModel<SysUser> selectUnallocatedList(PageRequest request, Long roleId, SysUser user, List<Long> deptIds) {
        return doGetUserAllocatedList(request, roleId, user, deptIds, false);
    }

    @Transactional
    @Override
    public long deleteUserByIds(String ids) throws BusinessException {
        Long[] userIds = Convert.toLongArray(ids);
        for (Long userId : userIds) {
            deleteAllUserInfoByUserId(userId);
        }
        return userIds.length;
    }

    @Transactional
    @Override
    public void deleteAllUserInfoByUserId(Long userId) {
        checkUserAllowed(new SysUser(userId),"删除");
        //删除用户角色
        long delRoleCount = sysRoleService.deleteAuthUsers(userId);
        //删除用户岗位
        long delPostCount = sysPostService.deleteAuthUsers(userId);
        //逻辑删除用户信息
        long delUserCount = deleteByPId(userId);
        log.info("删除用户Id{},删除用户角色数量{}, 删除用户岗位数量{}, 数据库删除记录数量{}",userId,delRoleCount,delPostCount,delUserCount);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId) {
        List<SysRole> list = sysRoleService.selectAllRolesByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        for (SysRole sysRole : list) {
            if(sysRole.isFlag())
                idsStr.append(sysRole.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        List<SysPost> list = sysPostService.selectPostsByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        for (SysPost sysPost : list) {
            if(sysPost.isFlag())
                idsStr.append(sysPost.getPostName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    @Transactional
    @Override
    public SysUser resetUserPwd(Long userId, String newPassword, String salt) {
        Assert.notNull(salt,"");
        SysUser dbUser = findUser(userId);
        if (dbUser == null)
            throw new UserNotFoundException();
        if (StringUtils.isBlank(newPassword)) {
            throw BusinessException.build("password不能为空");
        }
        dbUser.setSalt(salt);
        dbUser.setPassword(newPassword);
        dbUser.setPasswordUpdateTime(new Date());
        userDao.save(dbUser);
        return dbUser;
    }

    @Override
    public String importUser(List<SysUser> userList, boolean isUpdateSupport, String operName) {
        if (userList == null || userList.size() == 0) {
            throw BusinessException.build("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String password = sysConfigService.selectConfigByKey("sys.user.initPassword");
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                SysUser u = userDao.findByLoginName(user.getLoginName());
                checkUserAllowed(u,"导入");
                if (u == null) {
                    user.setSalt(Global.getSalt());
                    user.setPassword(MD5Util.encode(Global.getSalt() + password));
                    user.setCreateBy(operName);
                    user = saveUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                } else if (isUpdateSupport) {
                    user.setUpdateBy(operName);
                    user = saveUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getLoginName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getLoginName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw BusinessException.build(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 给用户授予角色
     *
     * @param user
     */
    private void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (roles != null && roles.length > 0) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<>();
            for (Long roleId : roles) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getUserId());
                ur.setRoleId(roleId);
                list.add(ur);
            }
            if (list.size() > 0) {
                userRoleDao.saveAll(list);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    private void insertUserPost(SysUser user) {
        Long[] posts = user.getPostIds();
        if (posts != null && posts.length > 0) {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<>();
            for (Long postId : user.getPostIds()) {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            if (list.size() > 0) {
                userPostDao.saveAll(list);
            }
        }
    }

    private void addSearchToQuery(SysUser searchUser, Query query) {
        List<Criteria> criterias = buildCriterias(searchUser);
        if (criterias.size() > 0) {
            criterias.forEach(e -> query.addCriteria(e));
        }
    }


    private void addSearchToAggregate(SysUser searchUser, List<AggregationOperation> aggregationOperations) {
        List<Criteria> criterias = buildCriterias(searchUser);
        if (criterias.size() > 0) {
            criterias.forEach(e -> aggregationOperations.add(match(e)));
        }
    }


    private List<Criteria> buildCriterias(SysUser searchUser) {
        List<Criteria> criterias = new ArrayList<>();
        if (searchUser != null) {
            if (StringUtils.isNotBlank(searchUser.getLoginName())) {
                criterias.add(Criteria.where("loginName").regex(searchUser.getLoginName()));
            }
            if (StringUtils.isNotBlank(searchUser.getPassword())) {
                criterias.add(Criteria.where("password").is(searchUser.getPassword()));
            }
            if (searchUser.getDeptId() != null) {
                //criterias.add(Criteria.where("deptId").is(searchUser.getDeptId()));   ggg fvv
                List<Long> allDescDeptIds = null;
                if (searchUser.getDeptId() != null) {
                    allDescDeptIds = new ArrayList<>();
                    allDescDeptIds.add(searchUser.getDeptId());
                    SysDept sysDept = deptDao.findByDeptId(searchUser.getDeptId());
                    if (sysDept != null) {
                        allDescDeptIds.addAll(sysDept.getDescendents());
                    }
                }
                if (allDescDeptIds != null) {
                    //查找这个deptID下面的所有部门 的 用户
                    criterias.add(Criteria.where("deptId").in(allDescDeptIds));
                }
            }
        }
        //ncriterias.add(CriteriaUtil.notDeleteCriteria());
        return criterias;
    }


    private void doUserPerms(SysUser user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleDao.deleteAllByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostDao.deleteAllByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
    }

    private PageModel<SysUser> doGetUserAllocatedList(PageRequest request,
                                                      Long roleId, SysUser user, List<Long> deptIds, boolean inRoles) {
        Assert.notNull(roleId, "roleId cant null");
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        String sys_role_dept_prefix = "sys_dept.";
        LookupOperation lookupOperation = LookupOperation.newLookup().
                from("sys_dept").
                localField("deptId").
                foreignField("deptId").
                as("sys_dept");
        aggregationOperations.add(lookupOperation);


        String sys_user_role_prefix = "sys_user_role.";
        LookupOperation lookupOperationUserRole = LookupOperation.newLookup().
                from("sys_user_role").
                localField("userId").
                foreignField("userId").
                as("sys_user_role");
        aggregationOperations.add(lookupOperationUserRole);


//        String sys_role_prefix = "sys_role.";
//        LookupOperation lookupOperationSysRole = LookupOperation.newLookup().
//                from(sys_user_role_prefix + "roleId").
//                localField(sys_role_menu_prefix + "roleId").
//                foreignField("roleId").
//                as("sys_role");
//        aggregationOperations.add(lookupOperationSysRole);


        if (inRoles) {
            aggregationOperations.add(match(
                    Criteria.where(sys_user_role_prefix + "roleId").is(roleId))
            );
        } else {
            aggregationOperations.add(match(
                    Criteria.where(sys_user_role_prefix + "roleId").ne(roleId))
            );
        }

        if (StringUtils.isNotBlank(user.getLoginName())) {
            aggregationOperations.add(match(Criteria.where("loginName").regex(user.getLoginName().trim())));
        }
        if (StringUtils.isNotBlank(user.getPhoneNumber())) {
            aggregationOperations.add(match(Criteria.where("phoneNumber").is(user.getPhoneNumber().trim())));
        }

        //只显示正常
        aggregationOperations.add(Aggregation.match(CriteriaUtil.notDeleteCriteria()));

        //计算总数量
        List<AggregationOperation> aggregationOperationCount = new ArrayList<>();
        aggregationOperationCount.addAll(aggregationOperations);
        aggregationOperationCount.add(Aggregation.count().as("c"));
        Map countMap = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperationCount),
                SysUser.class, Map.class).getUniqueMappedResult();
        long count = countMap == null ? 0 : (Integer) countMap.get("c");

        PageRequestUtil.injectAggregationOnlyPage(request,aggregationOperations);

        //获取list
        List<SysUser> sysUserList = mongoTemplate.aggregate(Aggregation.newAggregation(aggregationOperations),
                SysUser.class, SysUser.class).getMappedResults();
        return PageModel.instance(count, sysUserList);
    }


    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user,String actionName) {
        if (user.getUserId() != null && user.isAdmin()) {
            throw BusinessException.build("不允许" + StringUtils.defaultIfBlank(actionName,"操作") + "超级管理员用户" + StringUtils.defaultIfBlank(user.getLoginName(),""));
        }
    }
}
