package me.izhong.dashboard.manage.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.entity.SysUserOnline;

import java.util.Date;
import java.util.List;

public interface SysUserOnlineService {

    public void batchDeleteOnline(List<String> sessionIds);

    public void saveOnline(SysUserOnline online);

    public void forceLogout(String sessionId);

    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime);

    SysUserOnline selectByPId(String sessionId);

    PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline);

    void deleteById(String sessionId);

    List<SysUserOnline> findAll();
}
