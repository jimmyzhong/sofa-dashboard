package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.dashboard.manage.dao.MenuDao;
import me.izhong.dashboard.manage.dao.RoleMenuDao;
import me.izhong.dashboard.manage.entity.SysMenu;
import me.izhong.dashboard.manage.entity.SysRoleMenu;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.db.mongo.util.CriteriaUtil;
import me.izhong.dashboard.manage.domain.Ztree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Service
public class SysMenuServiceImpl extends CrudBaseServiceImpl<Long,SysMenu> implements SysMenuService {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 首页菜单显示
     * @param userId
     * @return
     */
    @Override
    public List<SysMenu> selectVisibleMenusByUser(Long userId) {
        Assert.notNull(userId,"用户ID不能为空");
        List<SysMenu> sysMenus = doSelectPermsByUserId(userId, true);
        if(sysMenus != null && sysMenus.size() > 0) {
            sysMenus = sysMenus.stream().filter(e -> StringUtils.equals("0", e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        }
        sortMenus(sysMenus);
        return getChildPerms(sysMenus, 0);
    }

    /**
     * 返回正常菜单
     *
     * @return
     */
    @Override
    public List<SysMenu> selectMenusByUser(Long userId) {
        Assert.notNull(userId,"");
        List<SysMenu> sysMenus;
        // 管理员显示所有菜单信息
        if (userId == null || userId.equals(1L)) {
            sysMenus = doSelectPermsByUserId(null, true);
        } else {
            sysMenus = doSelectPermsByUserId(userId, true);
        }
        sortMenus(sysMenus);
        return getChildPerms(sysMenus, 0);
    }

    private void sortMenus(List<SysMenu> sysMenus) {
        if (sysMenus == null || sysMenus.size() == 0)
            return;
        Collections.sort(sysMenus, (o1, o2) -> {
            if(o1.getParentId() == null) {
                return -1;
            } else if(o2.getParentId() == null) {
                return 1;
            }
            if(o1.getParentId() == o2.getParentId()) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else if (o1.getParentId() > o2.getParentId()) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    @Override
    public List<SysMenu> selectMenuList(SysMenu sysMenu) {
        List<SysMenu> results = selectList(sysMenu);
        sortMenus(results);
        return results;
    }

    @Override
    public List<SysMenu> selectMenuAll() {
        List<SysMenu> sysMenus = selectAll();
        sortMenus(sysMenus);
        return sysMenus;
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        //查询用户所有的权限 String格式
        List<SysMenu> perms = doSelectPermsByUserId(userId, false);
        Set<String> permsSet = new HashSet<>();
        for (SysMenu perm : perms) {
            if (StringUtils.isNotEmpty(perm.getPerms())) {
                permsSet.addAll(Arrays.asList(perm.getPerms().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * role下面的菜单权限,给角色分配权限
     *
     * @param roleId 角色ID
     * @return
     */
    @Override
    public List<Ztree> roleMenuTreeData(Long roleId) {
        List<Ztree> ztrees = null;
        List<SysMenu> sysMenuList = selectMenuList(null);
        //只显示正常的
        sysMenuList = sysMenuList.stream().filter(e -> StringUtils.equals("0",e.getVisible()))
                .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());

        sortMenus(sysMenuList);
        if (roleId != null) {
            List<SysRoleMenu> sysRoleMenus = roleMenuDao.findAllByRoleId(roleId);
            List<Long> roleMenuList = sysRoleMenus == null ? null : sysRoleMenus.stream().map(e -> e.getMenuId()).collect(toList());
            ztrees = initZtree(sysMenuList, roleMenuList, true);
        } else {
            ztrees = initZtree(sysMenuList, null, true);
        }
        return ztrees;
    }

    @Override
    public List<String> selectPermsByRoleId(Long roleId) {
        Assert.notNull(roleId,"");
        List<SysRoleMenu> sysRoleMenus = roleMenuDao.findAllByRoleId(roleId);
        List<Long> menuIds = sysRoleMenus == null ? null : sysRoleMenus.stream().map(e -> e.getMenuId()).collect(toList());
        List<SysMenu> sysMenus = menuDao.findAllByMenuIdIn(menuIds);
        return sysMenus == null? null: sysMenus.stream().map(e -> e.getPerms()).filter(e -> StringUtils.isNotBlank(e)).collect(toList());
    }

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    @Override
    public List<Ztree> menuTreeData() {
        List<SysMenu> sysMenuList = menuDao.findAll();
        sortMenus(sysMenuList);
        List<Ztree> ztrees = initZtree(sysMenuList);
        return ztrees;
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList 菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList) {
        return initZtree(sysMenuList, null, false);
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList     菜单列表,所有的菜单
     * @param roleMenuList 角色已存在菜单列表，部分菜单
     * @param permsFlag    是否需要显示权限标识
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList, List<Long> roleMenuList, boolean permsFlag) {
        List<Ztree> ztrees = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            Ztree ztree = new Ztree();
            ztree.setId(sysMenu.getMenuId());
            ztree.setPId(sysMenu.getParentId());
            ztree.setName(transMenuName(sysMenu,permsFlag));
            ztree.setTitle(sysMenu.getMenuName());
            if (roleMenuList != null) {
                ztree.setChecked(roleMenuList.contains(sysMenu.getMenuId()));
            }
            ztrees.add(ztree);
        }
        return ztrees;
    }

    public String transMenuName(SysMenu sysMenu, boolean permsFlag) {
        StringBuffer sb = new StringBuffer();
        sb.append(sysMenu.getMenuName());
        if (permsFlag) {
            sb.append("<font color=\"#888\">&nbsp;&nbsp;&nbsp;" + sysMenu.getPerms() + "</font>");
        }
        return sb.toString();
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        SysMenu m = selectByPId(menuId);
        if (m != null) {
            SysMenu parentSysMenu = menuDao.findByMenuId(m.getParentId());
            if (parentSysMenu != null) {
                m.setParentName(parentSysMenu.getMenuName());
            }
        }
        return m;
    }

    @Override
    public int selectCountMenuByParentId(Long parentId) {
        return menuDao.findAllByParentIdAndIsDelete(parentId,false).size();
    }

    @Override
    public int selectCountRoleMenuByMenuId(Long menuId) {
        return 0;
    }

    @Override
    public int insertMenu(SysMenu sysMenu) {
        checkMenuValid(sysMenu);
        super.insert(sysMenu);
        return 1;
    }

    @Override
    public int updateMenu(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "");
        Assert.notNull(sysMenu.getMenuId(), "menuId不能未空");
        SysMenu dbSysMenu = menuDao.findByMenuId(sysMenu.getMenuId());
        if (dbSysMenu != null) {
            sysMenu.setId(dbSysMenu.getId());
        } else {
            throw BusinessException.build("菜单未找到 menuId=" + sysMenu.getMenuId());
        }
        checkMenuValid(sysMenu);
        menuDao.save(sysMenu);
        return 1;
    }

    private void checkMenuValid(SysMenu m) throws BusinessException {
        if(!checkMenuNameUnique(m))
            throw BusinessException.build("菜单名称重复");
        if(StringUtils.isBlank(m.getMenuType())){
            throw BusinessException.build("菜单类型不能为空");
        }
        if(StringUtils.equalsAny(m.getMenuType(),"C","F") && StringUtils.isBlank(m.getPerms())){
            throw BusinessException.build("权限标识不能为空");
        }
//        if(StringUtils.isBlank(m.getOrderNum())){
//            throw BusinessException.build("显示排序不能为空");
//        }
        if(!StringUtils.equalsAny(m.getMenuType(),"M","C","F")) {
            throw BusinessException.build("菜单类型不正确");
        }
        if(StringUtils.equalsAny(m.getMenuType(),"M","C")) {
            if(StringUtils.isBlank(m.getVisible())) {
                throw BusinessException.build("菜单菜单状态不能为空");
            }
            if(StringUtils.equals(m.getMenuType(),"C") && StringUtils.isBlank(m.getTarget())) {
                throw BusinessException.build("打开方式不能为空");
            }
        }

        if(m.getParentId() > 0) {
            SysMenu parentMenu = menuDao.findByMenuId(m.getParentId());
            if(parentMenu == null) {
                throw BusinessException.build("父菜单不存在");
            }
            if(StringUtils.equalsAny(m.getMenuType(),"M","C")) {
                if (!StringUtils.equals(parentMenu.getMenuType(), "M")) {
                    throw BusinessException.build("父菜单不是目录，不能选择");
                }
            } else if(StringUtils.equals(m.getMenuType(),"F")) {
                if (StringUtils.equals(parentMenu.getMenuType(), "F")) {
                    throw BusinessException.build("父菜单不是目录或者菜单，不能选择");
                }
            }
        }

        List<SysMenu> childs = menuDao.findAllByParentId(m.getMenuId());
        if(childs != null && childs.size() > 0) {
            if(!StringUtils.equalsAny(m.getMenuType(),"M","C")) {
                throw BusinessException.build("菜单有子成员，类型只能修改为目录或者菜单，不能修改为其他类型");
            }
        }

    }

    @Override
    public boolean checkMenuNameUnique(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "sysMenu 不能为空");
        Assert.notNull(sysMenu.getMenuName(), "menuName 不能为空");
        Long parentId = sysMenu.getParentId() == null ? 0L : sysMenu.getParentId();
        SysMenu m = menuDao.findByMenuName(sysMenu.getMenuName());
        if(m!= null && m.getMenuId().equals(sysMenu.getMenuId())) {
            return true;
        }else if (m != null && !m.getParentId().equals(parentId)) {
            return false;
        }
        return true;
    }

    /**
     * 用户userId所拥有的权限
     *
     * @param userId
     * @param menuOnly 只显示菜单，不显示按钮
     * @return
     */
    private List<SysMenu> doSelectPermsByUserId(Long userId, boolean menuOnly) {

        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        String sys_role_menu_prefix = "sys_role_menu.";
        LookupOperation lookupOperation = LookupOperation.newLookup().
                from("sys_role_menu").
                localField("menuId").
                foreignField("menuId").
                as("sys_role_menu");
        aggregationOperations.add(lookupOperation);

        String sys_user_role_prefix = "sys_user_role.";
        LookupOperation lookupOperationUserMenu = LookupOperation.newLookup().
                from("sys_user_role").
                localField(sys_role_menu_prefix + "roleId").
                foreignField("roleId").
                as("sys_user_role");
        aggregationOperations.add(lookupOperationUserMenu);

        //查询某个用户的权限
        if (userId != null)
            aggregationOperations.add(match(Criteria.where(sys_user_role_prefix + "userId").is(userId)));

        //只显示正常
        if (menuOnly) {
            aggregationOperations.add(match(Criteria.where("menuType").in(new String[]{"M", "C"})));
        } else {
            aggregationOperations.add(match(Criteria.where("menuType").in(new String[]{"M", "C", "F"})));
        }
        aggregationOperations.add(match(CriteriaUtil.notDeleteCriteria()));

        //aggregationOperations.add(skip(start));
        //aggregationOperations.add(limit(size));

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        //List<Document> results3 = mongoTemplate.aggregate(aggregation, SysMenu.class, Document.class).getMappedResults();
        List<SysMenu> dbSysMenus = mongoTemplate.aggregate(aggregation, SysMenu.class, SysMenu.class).getMappedResults();
        return new ArrayList<SysMenu>() {{
            addAll(dbSysMenus);
        }};

//        List<SysMenu> perms = new ArrayList<>();
//        if(results2 != null) {
//            Iterator<Document> it = results2.iterator();
//            while (it.hasNext()) {
//                Map<String,Object> resultsMap =  new HashMap<>();
//                Document doc = it.next();
//                doc.entrySet().forEach(e -> {
//                    resultsMap.put(e.getKey(),e.getValue());
//                });
//                String jsonStringUser = JSONObject.toJSONString(resultsMap);
//                SysMenu m2  = JSONObject.parseObject(jsonStringUser,SysMenu.class);
//                m2.setMenuId((Long) resultsMap.get("_id"));
//                perms.add(m2);
//            }
//        }

//        return perms;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public static List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private static void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                // 判断是否有子节点
                Iterator<SysMenu> it = childList.iterator();
                while (it.hasNext()) {
                    SysMenu n = it.next();
                    recursionFn(list, n);
                }
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private static List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {

        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    List<SysMenu> returnList = new ArrayList<SysMenu>();

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list   分类表
     * @param typeId 传入的父节点ID
     * @param prefix 子节点前缀
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int typeId, String prefix) {
        if (list == null) {
            return null;
        }
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu node = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (node.getParentId() == typeId) {
                recursionFn(list, node, prefix);
            }
            // 二、遍历所有的父节点下的所有子节点
            /*
             * if (node.getParentId()==0) { recursionFn(list, node); }
             */
        }
        return returnList;
    }

    private void recursionFn(List<SysMenu> list, SysMenu node, String p) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, node);
        if (hasChild(list, node)) {
            // 判断是否有子节点
            returnList.add(node);
            Iterator<SysMenu> it = childList.iterator();
            while (it.hasNext()) {
                SysMenu n = (SysMenu) it.next();
                n.setMenuName(p + n.getMenuName());
                recursionFn(list, n, p + p);
            }
        } else {
            returnList.add(node);
        }
    }

    /**
     * 判断是否有子节点
     */
    private static boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }
}
