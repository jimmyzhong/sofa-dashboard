
-- 返回码 1:操作成功 0:未配置 -1: 获取失败 -2:修改错误，建议重新初始化 -500:不支持的操作
-- redis hashmap 中存放的内容:
-- last_mill_second 上次放入令牌或者初始化的时间
-- stored_permits 目前令牌桶中的令牌数量
-- max_permits 令牌桶容量
-- interval 放令牌间隔
-- app 一个标志位，表示对于当前key有没有限流存在

local SUCCESS = 1
local NO_LIMIT = 0
local ACQUIRE_FAIL = -1
local MODIFY_ERROR = -2
local UNSUPPORT_METHOD = -500

local ratelimit_info = redis.pcall("HMGET",KEYS[1], "last_mill_second", "stored_permits", "max_permits", "interval", "app")
local last_mill_second = ratelimit_info[1]
local stored_permits = tonumber(ratelimit_info[2])
local max_permits = tonumber(ratelimit_info[3])
local interval = tonumber(ratelimit_info[4])
local app = ratelimit_info[5]

local method = ARGV[1]

--获取当前毫秒
--考虑主从策略和脚本回放机制，这个time由客户端获取传入
--local curr_time_arr = redis.call('TIME')
--local curr_timestamp = curr_time_arr[1] * 1000 + curr_time_arr[2]/1000
local curr_timestamp = tonumber(ARGV[2])


-- 当前方法为初始化
if method == 'init' then
    --如果app不为null说明已经初始化过，不要重复初始化
    if(type(app) ~='boolean' and app ~=nil) then
        return SUCCESS
    end

    redis.pcall("HMSET", KEYS[1],
            "last_mill_second", curr_timestamp,
            "stored_permits", ARGV[3],
            "max_permits", ARGV[4],
            "interval", ARGV[5],
            "app", ARGV[6])
    --始终返回成功
    return SUCCESS
end

-- 当前方法为修改配置
if method == "modify" then
    if(type(app) =='boolean' or app ==nil) then
        return MODIFY_ERROR
    end
    --只能修改max_permits和interval
    redis.pcall("HMSET", KEYS[1],
            "max_permits", ARGV[3],
            "interval", ARGV[4])

    return SUCCESS

end

-- 当前方法为删除
if method == "delete" then
    --已经清除完毕
    if(type(app) =='boolean' or app ==nil) then
        return SUCCESS
    end
    redis.pcall("DEL", KEYS[1])
    return SUCCESS
end

-- 尝试获取permits
if method == "acquire" then
    -- 如果app为null说明没有对这个进行任何配置，返回0代表不限流
    if(type(app) =='boolean' or app ==nil) then
        return NO_LIMIT
    end
    --需要获取令牌数量
    local acquire_permits = tonumber(ARGV[3])
    --计算上一次放令牌到现在的时间间隔中，一共应该放入多少令牌
    local reserve_permits = math.max(0, math.floor((curr_timestamp - last_mill_second) / interval))

    local new_permits = math.min(max_permits, stored_permits + reserve_permits)
    local result = ACQUIRE_FAIL
    --如果桶中令牌数量够则放行
    if new_permits >= acquire_permits then
        result = SUCCESS
        new_permits = new_permits - acquire_permits
    end
    --更新当前桶中的令牌数量
    redis.pcall("HSET", KEYS[1], "stored_permits", new_permits)
    --如果这次有放入令牌，则更新时间
    if reserve_permits > 0 then
        redis.pcall("HSET", KEYS[1], "last_mill_second", curr_timestamp)
    end
    return result
end


return UNSUPPORT_METHOD