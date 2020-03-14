package me.izhong.dashboard.manage.dao;

import me.izhong.dashboard.manage.entity.SysDictData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataDao extends MongoRepository<SysDictData, Long> {

    List<SysDictData> findAllByDictTypeOrderByDictSortAsc(String dictType);

    int deleteAllByDictCode(Long dictCode);

    SysDictData findByDictCode(Long dictCode);

    SysDictData findByDictTypeAndDictValue(String dictType, String dictValue);

    int deleteAllByDictCodeIn(List<Long> delIds);
}
