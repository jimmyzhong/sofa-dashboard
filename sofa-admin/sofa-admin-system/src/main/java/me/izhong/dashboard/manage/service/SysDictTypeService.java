package me.izhong.dashboard.manage.service;

import me.izhong.common.domain.PageRequest;
import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.domain.Ztree;
import me.izhong.dashboard.manage.entity.SysDictType;

import java.util.List;


public interface SysDictTypeService extends CrudBaseService<Long,SysDictType> {

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    public SysDictType selectDictTypeByType(String dictType);

    public SysDictType insertDictType(SysDictType sysDictType);

    public SysDictType updateDictType(SysDictType sysDictType);

    public boolean checkDictTypeUnique(SysDictType sysDictType);

    /**
     * 查询字典类型树
     *
     * @param dictType 字典类型
     * @return 所有字典类型
     */
    public List<Ztree> selectDictTree(PageRequest request, SysDictType dictType);
}
