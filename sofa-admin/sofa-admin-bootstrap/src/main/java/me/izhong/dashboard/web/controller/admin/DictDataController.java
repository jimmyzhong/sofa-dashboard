package me.izhong.dashboard.web.controller.admin;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.db.mongo.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysDictData;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysDictDataService;
import me.izhong.dashboard.manage.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/system/dict/data")
public class DictDataController {
    private String prefix = "system/dict/data";

    @Autowired
    private SysDictDataService sysDictDataService;

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @GetMapping()
    public String dictData() {
        return prefix + "/data";
    }

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @AjaxWrapper
    public PageModel list(SysDictData sysDictData, HttpServletRequest request) {
        sysDictData.setIsDelete(false);
        PageModel<SysDictData> list = sysDictDataService.selectPage(PageRequestUtil.fromRequest(request), sysDictData);
        return list;
    }

    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.Dict.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(SysDictData sysDictData, HttpServletRequest request) {
        List<SysDictData> list = sysDictDataService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictData);
        ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
        return util.exportExcel(list, "字典数据");
    }

    @RequiresPermissions(PermissionConstants.Dict.VIEW)
    @GetMapping("/add/{dictType}")
    public String add(@PathVariable("dictType") String dictType, ModelMap mmap) {
        mmap.put("dictType", dictType);
        return prefix + "/add";
    }

    @Log(title = "字典数据", businessType = BusinessType.ADD)
    @RequiresPermissions(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysDictData addSave(@Validated SysDictData dict) {
        dict.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return sysDictDataService.insert(dict);
    }

    @GetMapping("/edit/{dictCode}")
    public String edit(@PathVariable("dictCode") Long dictCode, ModelMap mmap) {
        mmap.put("dict", sysDictDataService.selectByPId(dictCode));
        return prefix + "/edit";
    }

    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysDictData editSave(@Validated SysDictData dict) {
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysDictDataService.update(dict);
    }

    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @RequiresPermissions(PermissionConstants.Dict.REMOVE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysDictDataService.deleteByPIds(ids);
    }
}