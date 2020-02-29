package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.entity.SysOperLog;
import me.izhong.dashboard.manage.service.SysOperLogService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysOperLogServiceImpl extends CrudBaseServiceImpl<Long,SysOperLog> implements SysOperLogService {

}
