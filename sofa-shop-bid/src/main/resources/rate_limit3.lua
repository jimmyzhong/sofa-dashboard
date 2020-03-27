local tokens_key = KEYS[1]   -- request_rate_limiter.${id}.tokens 令牌桶剩余令牌数的KEY值
local timestamp_key = KEYS[2] -- 令牌桶最后填充令牌时间的KEY值

local rate = tonumber(ARGV[1])  -- replenishRate 令令牌桶填充平均速率
local capacity = tonumber(ARGV[2]) -- burstCapacity 令牌桶上限
local now = tonumber(ARGV[3]) -- 得到从 1970-01-01 00:00:00 开始的秒数
local requested = tonumber(ARGV[4]) -- 消耗令牌数量，默认 1

local fill_time = capacity/rate   -- 计算令牌桶填充满令牌需要多久时间
local ttl = math.floor(fill_time*2)  -- *2 保证时间充足


local last_tokens = tonumber(redis.call("get", tokens_key))
-- 获得令牌桶剩余令牌数
if last_tokens == nil then  -- 第一次时，没有数值，所以桶时满的
    last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
-- 令牌桶最后填充令牌时间
if last_refreshed == nil then
    last_refreshed = 0
end

local delta = math.max(0, now-last_refreshed)
-- 获取距离上一次刷新的时间间隔
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))
-- 填充令牌，计算新的令牌桶剩余令牌数 填充不超过令牌桶令牌上限。

local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
    -- 若成功，令牌桶剩余令牌数(new_tokens) 减消耗令牌数( requested )，并设置获取成功( allowed_num = 1 ) 。
    new_tokens = filled_tokens - requested
    allowed_num = 1
end

-- 设置令牌桶剩余令牌数( new_tokens ) ，令牌桶最后填充令牌时间(now) ttl是超时时间？
redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- 返回数组结果
-- return { allowed_num, new_tokens }
return allowed_num