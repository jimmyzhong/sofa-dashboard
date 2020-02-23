package me.izhong.db.common.service;

import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public interface CrudBaseService<K,T> {

     PageModel<T> selectPage(Query query, PageRequest pageRequest, T searchUser);

     PageModel<T> selectPage(PageRequest request, T target);

     List<T> selectList(Query query, PageRequest pageRequest, T searchUser);

     List<T> selectList(PageRequest request, T target);

     List<T> selectList(T target);

     long count(Query query, PageRequest pageRequest, T searchUser);

     long count(PageRequest request, T target);

     long count();

     T selectByPId(K pId);

     List<T> selectAll();

     long deleteByPId(K pId) throws BusinessException;

     long deleteByPIds(String pIds) throws BusinessException;

     long deleteAll();

     long clearAll();

     T insert(T target);

     T update(T target);

     long remove(K pId);

     long remove(List<K> pIds);

     long removeByPIds(String pIds) throws BusinessException;

}
