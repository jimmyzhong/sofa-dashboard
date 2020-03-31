package me.izhong.shop.service.impl;

import me.izhong.shop.dao.IDGeneratorDao;
import me.izhong.shop.entity.IDGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IDGeneratorService {
    @Autowired
    IDGeneratorDao dao;

    @Transactional
    public String nextID(String tableName, String variable) {
        IDGenerator gen = dao.findFirstByTableNameAndVariableForNextValue(tableName, variable);
        if (gen == null) {
            gen = new IDGenerator();
            gen.setTableName(tableName);
            gen.setVariable(variable);
            gen.setIncrement(0L);
        }
        gen.setIncrement(gen.getIncrement() + 1);
        if (StringUtils.isEmpty(variable)) {
            gen.setNext(gen.getIncrement().toString());
        } else {
            gen.setNext(gen.getIncrement().toString() + variable);
        }
        dao.save(gen);
        return gen.getNext();
    }
}
