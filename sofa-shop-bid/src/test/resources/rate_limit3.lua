local tokens_key = KEYS[1]   -- request_rate_limiter.${id}.tokens 令牌桶剩余令牌数的KEY值
local timestamp_key = KEYS[2] -- 令牌桶最后填充令牌时间的KEY值


local key = KEYS[1] --限流KEY（一秒一个）
local limit = tonumber(ARGV[1]) --限流大小
local current = tonumber(redis.call('get', key) or "0")
if current + 1 > limit then --如果超出限流大小
    return 0
else --请求数+1，并设置2秒过期
    redis.call("INCRBY", key, "1")
    redis.call("expire", key, "2")
end
return 1