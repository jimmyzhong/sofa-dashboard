package me.izhong.dashboard.manage.service.impl;

import me.izhong.common.domain.PageRequest;
import me.izhong.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.constants.UserConstants;
import me.izhong.dashboard.manage.dao.DictTypeDao;
import me.izhong.dashboard.manage.domain.Ztree;
import me.izhong.dashboard.manage.entity.SysDictData;
import me.izhong.dashboard.manage.entity.SysDictType;
import me.izhong.dashboard.manage.service.SysDictDataService;
import me.izhong.dashboard.manage.service.SysDictTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class SysDictTypeServiceImpl extends CrudBaseServiceImpl<Long,SysDictType> implements SysDictTypeService {

    @Autowired
    private DictTypeDao dictTypeDao;

    @Autowired
    private SysDictDataService sysDictDataService;

    @Override
    public SysDictType selectDictTypeByType(String dictType) {
        return dictTypeDao.findDictTypeByDictType(dictType);
    }

    @Override
    public SysDictType insertDictType(SysDictType sysDictType) {
        return super.insert(sysDictType);
    }

    @Override
    @Transactional
    public SysDictType updateDictType(SysDictType sysDictType) {
        //修改data
        SysDictType db = selectByPId(sysDictType.getDictId());
        if(!StringUtils.equals(db.getDictType(), sysDictType.getDictType())) {
            List<SysDictData> datas = sysDictDataService.selectDictDataByType(db.getDictType());
            if (datas != null && datas.size() > 0) {
                datas.forEach(e -> {
                    e.setDictType(sysDictType.getDictType());
                    sysDictDataService.update(e);
                });
            }
        }
        return super.update(sysDictType);
    }

    @Override
    public boolean checkDictTypeUnique(SysDictType dict) {
        Long dictId = dict.getDictId() == null ? -1L : dict.getDictId();
        SysDictType sysDictType = dictTypeDao.findDictTypeByDictType(dict.getDictType());
        if (sysDictType != null && sysDictType.getDictId().longValue() != dictId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 查询字典类型树
     *
     * @return 所有字典类型
     */
    public List<Ztree> selectDictTree(PageRequest request, SysDictType sysDictType)
    {
        List<SysDictType> dictList = selectList(sysDictType);
        List<Ztree> ztrees = new ArrayList<Ztree>();
        for (SysDictType dict : dictList)
        {
            if (UserConstants.DICT_NORMAL.equals(dict.getStatus()))
            {
                Ztree ztree = new Ztree();
                ztree.setId(dict.getDictId());
                ztree.setName(transDictName(dict));
                ztree.setTitle(dict.getDictType());
                ztrees.add(ztree);
            }
        }
        return ztrees;
    }

    public String transDictName(SysDictType dictType)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(" + dictType.getDictName() + ")");
        sb.append("&nbsp;&nbsp;&nbsp;" + dictType.getDictType());
        return sb.toString();
    }
}
