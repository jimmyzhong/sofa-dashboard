package me.izhong.db.mongo.service;

import me.izhong.db.mongo.dao.MongoLockDao;
import me.izhong.db.mongo.domain.MongoLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MongoDistributedLock {

    @Autowired
    private MongoLockDao mongoLockDao;

    /**
     * 获得锁的步骤：
     * 1、首先判断锁是否被其他请求获得；如果没被其他请求获得则往下进行；
     * 2、判断锁资源是否过期，如果过期则释放锁资源；
     * 3.1、尝试获得锁资源，如果value=1，那么获得锁资源正常;（在当前请求已经获得锁的前提下，还可能有其他请求尝试去获得锁，此时会导致当前锁的过期时间被延长，由于延长时间在毫秒级，可以忽略。）
     * 3.2、value>1,则表示当前请求在尝试获取锁资源过程中，其他请求已经获取了锁资源，即当前请求没有获得锁；
     * ！！！注意，不需要锁资源时，及时释放锁资源！！！。
     *
     * @param key
     * @param expire
     * @return
     */
    public boolean getLock(String key, long expire) {
        MongoLock mongoLocks = mongoLockDao.getByKey(key);
        //判断该锁是否被获得,锁已经被其他请求获得，直接返回
        if (mongoLocks != null && mongoLocks.getExpire() >= System.currentTimeMillis()) {
            return false;
        }
        //释放过期的锁
        if (mongoLocks  != null && mongoLocks.getExpire() < System.currentTimeMillis()) {
            releaseLockExpire(key, System.currentTimeMillis());
        }
        //！！(在高并发前提下)在当前请求已经获得锁的前提下，还可能有其他请求尝试去获得锁，此时会导致当前锁的过期时间被延长，由于延长时间在毫秒级，可以忽略。
        Map<String, Object> mapResult = mongoLockDao.incrByWithExpire(key, 1, System.currentTimeMillis() + expire);
        //如果结果是1，代表当前请求获得锁
        if ( (long) mapResult.get("value") == 1) {
            return true;
            //如果结果>1，表示当前请求在获取锁的过程中，锁已被其他请求获得。
        } else if ((long) mapResult.get("value") > 1) {
            return false;
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void releaseLock(String key) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("key", key);
        mongoLockDao.remove(condition);
    }

    /**
     * 释放过期锁
     *
     * @param key
     * @param expireTime
     */
    private void releaseLockExpire(String key, long expireTime) {
        mongoLockDao.removeExpire(key, expireTime);
    }
}