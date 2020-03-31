package me.izhong.shop.dao;

import me.izhong.shop.entity.IDGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;

public interface IDGeneratorDao extends JpaRepository<IDGenerator, Integer> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from IDGenerator t where t.tableName =?1 and t.variable = ?2")
    IDGenerator findFirstByTableNameAndVariableForNextValue(String tableName, String variable);
}
