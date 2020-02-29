package me.izhong.dashboard.web.controller.admin;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.domain.Ztree;
import me.izhong.dashboard.manage.entity.SysDictType;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysDictTypeService;
import me.izhong.dashboard.manage.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/dict")
public class DictTypeController {
    private String prefix = "system/dict/type";

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @GetMapping()
    public String dictType() {
        return prefix + "/type";
    }

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(SysDictType sysDictType, HttpServletRequest request) {
        sysDictType.setIsDelete(false);
        PageModel<SysDictType> list = sysDictTypeService.selectPage(PageRequestUtil.fromRequest(request), sysDictType);
        return list;
    }

    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.Dict.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public Object export(SysDictType sysDictType, HttpServletRequest request) {

        List<SysDictType> list = sysDictTypeService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictType);
        ExcelUtil<SysDictType> util = new ExcelUtil<SysDictType>(SysDictType.class);
        String name = util.exportExcel(list, "字典类型");
        Map map = new HashMap<>();
        map.put("name", name);
        return name;
    }


    @RequiresPermissions(PermissionConstants.Dict.ADD)
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }


    @Log(title = "字典类型", businessType = BusinessType.ADD)
    @RequiresPermissions(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysDictType addSave(String dictName, String dictType, String status, String remark) {
        SysDictType dict = new SysDictType();
        dict.setDictType(dictType);
        dict.setDictName(dictName);
        dict.setStatus(status);
        dict.setRemark(remark);
        dict.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)){
            throw BusinessException.build("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return sysDictTypeService.insertDictType(dict);
    }

    @RequiresPermissions(PermissionConstants.Dict.EDIT)
    @GetMapping("/edit/{dictId}")
    public String edit(@PathVariable("dictId") Long dictId, ModelMap mmap) {
        mmap.put("dict", sysDictTypeService.selectByPId(dictId));
        return prefix + "/edit";
    }

    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysDictType editSave(Long dictId, String dictName, String dictType, String status, String remark) {
        SysDictType dict = new SysDictType();
        dict.setDictId(dictId);
        dict.setDictType(dictType);
        dict.setDictName(dictName);
        dict.setStatus(status);
        dict.setRemark(remark);
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)){
            throw BusinessException.build("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return sysDictTypeService.updateDictType(dict);
    }

    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @RequiresPermissions(PermissionConstants.Dict.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) throws BusinessException {
        return sysDictTypeService.deleteByPIds(ids);
    }

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @GetMapping("/detail/{dictId}")
    public String detail(@PathVariable("dictId") Long dictId, ModelMap mmap) {
        mmap.put("dict", sysDictTypeService.selectByPId(dictId));
        mmap.put("dictList", sysDictTypeService.selectAll());
        return "system/dict/data/data";
    }

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @PostMapping("/checkDictTypeUnique")
    @ResponseBody
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        SysDictType dict = new SysDictType();
        dict.setDictId(dictId);
        dict.setDictType(dictType);
        return sysDictTypeService.checkDictTypeUnique(dict);
    }

    /**
     * 选择字典树
     */
    @GetMapping("/selectDictTree/{columnId}/{dictType}")
    public String selectDeptTree(@PathVariable("columnId") Long columnId,
                                 @PathVariable("dictType") String dictType,
                                 ModelMap mmap)
    {
        mmap.put("columnId", columnId);
        mmap.put("dict", sysDictTypeService.selectDictTypeByType(dictType));
        return prefix + "/tree";
    }

    /**
     * 加载字典列表树
     */
    @GetMapping("/treeData")
    @ResponseBody
    public List<Ztree> treeData(HttpServletRequest request)
    {
        List<Ztree> ztrees = sysDictTypeService.selectDictTree(PageRequestUtil.fromRequestIgnorePageSize(request), new SysDictType());
        return ztrees;
    }
}
