package me.izhong.dashboard.manage.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.entity.SysLoginInfo;
import me.izhong.dashboard.manage.service.SysLoginInfoService;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SysLoginInfoServiceImpl extends CrudBaseServiceImpl<Long,SysLoginInfo> implements SysLoginInfoService {

}
