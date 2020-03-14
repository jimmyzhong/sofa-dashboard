package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.dashboard.manage.entity.SysNotice;
import me.izhong.dashboard.manage.service.SysNoticeService;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeServiceImpl extends CrudBaseServiceImpl<Long,SysNotice> implements SysNoticeService {

}
