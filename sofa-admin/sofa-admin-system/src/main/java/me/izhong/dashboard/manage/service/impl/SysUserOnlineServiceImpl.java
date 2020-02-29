package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.dao.UserOnlineDao;
import me.izhong.dashboard.manage.entity.SysUserOnline;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.service.SysUserOnlineService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SysUserOnlineServiceImpl extends CrudBaseServiceImpl<String,SysUserOnline> implements SysUserOnlineService {

    @Autowired
    private UserOnlineDao userOnlineDao;

    @Override
    public void batchDeleteOnline(List<String> sessions) {
        for (String sessionId : sessions) {
            SysUserOnline sysUserOnline = selectByPId(sessionId);
            if (sysUserOnline != null) {
                userOnlineDao.delete(sysUserOnline);
            }
        }
    }

    @Transactional
    @Override
    public void saveOnline(SysUserOnline online) {
        if (StringUtils.isBlank(online.getSessionId())) {
            throw BusinessException.build("session id is null");
        }
        SysUserOnline o = userOnlineDao.findBySessionId(online.getSessionId());
        if (o != null)
            online.setId(o.getId());

        super.insert(online);
    }

    @Transactional
    @Override
    public void forceLogout(String sessionId) {
        SysUserOnline session = selectByPId(sessionId);
        if (session == null) {
            return;
        }
        userOnlineDao.delete(session);
    }

    @Override
    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime) {
        Assert.notNull(lastAccessTime,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("lastAccessTime").lte(lastAccessTime));
        List<SysUserOnline> users = mongoTemplate.find(query, SysUserOnline.class);
        return users;
    }

}
