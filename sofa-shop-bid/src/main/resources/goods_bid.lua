local tokens_key = KEYS[1]   -- request_rate_limiter.${id}.tokens 令牌桶剩余令牌数的KEY值
local timestamp_key = KEYS[2] -- 令牌桶最后填充令牌时间的KEY值
local bidKey = KEYS[3]
local bidList = KEYS[4]

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


local function isNull(v)
    if (v==nil) then
        return true
    end
    if (v == nil or (type(v) == 'boolean' and not v)) then
        return true
    else
        return false
    end
end

local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
local currentPrice = -1
local currentIndex = -1
local testValue = -1
if allowed then
    -- 若成功，令牌桶剩余令牌数(new_tokens) 减消耗令牌数( requested )，并设置获取成功( allowed_num = 1 ) 。
    new_tokens = filled_tokens - requested
    -- 查看商品价格
    local startPriceS = redis.call("hget", bidKey, "startPrice")
    if isNull(startPriceS) then
        allowed_num = -1
        return {"allow", tostring(allowed_num),"msg","报价商品不存在","notice",bidKey}
    end

    local startPrice = tonumber(startPriceS)

    local endPrice = tonumber(redis.call("hget", bidKey, "endPrice"))
    local currentPriceS = redis.call("hget", bidKey, "currentPrice")
    local currentIndexS = redis.call("hget", bidKey, "currentIndex")
    local stageS = redis.call("hget", bidKey, "stage")
    local stepPrice = tonumber(redis.call("hget", bidKey, "stepPrice"))
    local bidStartTime = tonumber(redis.call("hget", bidKey, "startTime"))
    local bidEndTime = tonumber(redis.call("hget", bidKey, "endTime"))
    --校验时间 未开始bug
    if bidStartTime < now then
        --allowed_num = -3
        -- return {"allow", tostring(-3),"msg","报价未开始"}
    end
    --校验时间 结束
    if allowed_num == 0 and bidEndTime < now then
        --allowed_num = -4
    end

    if isNull(currentPriceS) then
        currentPrice = startPrice
    else
        currentPrice = tonumber(currentPriceS)
    end

    if isNull(currentIndexS) then
        currentIndex = 1
    else
        currentIndex = tonumber(currentIndexS) + 1
    end
    --testValue = 55

    -- 提前结束
    if allowed_num == 0 and not isNull(stageS) then
        if tonumber(stageS) > 0 then
            allowed_num = -5
            return {"allow", tostring(-5),"msg","报价已经到达最大值，结束"}
        end
    end

    -- 判断价格
    if allowed_num == 0 and currentPrice + stepPrice > endPrice then
        allowed_num = -5
        redis.call("hset", bidKey, "stage" , 1)
    end

    local currentIndexString = tostring(currentIndex)
    -- 开始出价
    if allowed_num == 0 then
        currentPrice = currentPrice + stepPrice
        redis.call("hset", bidKey, "currentPrice" , currentPrice)
        redis.call("hset", bidKey, "currentIndex" , currentIndex)
        redis.call("hset", bidList, currentIndexString , currentIndexString .. "," .. tostring(currentPrice))
        redis.call("expire", bidList, 3000)
    end
end

-- 设置令牌桶剩余令牌数( new_tokens ) ，令牌桶最后填充令牌时间(now) ttl是超时时间？
redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- 返回数组结果
-- allowed_num=0 成功
return {"allow", tostring(allowed_num), "msg","报价成功" ,"new_tokens", tostring(new_tokens), "price",tostring(currentPrice), "seqId", tostring(currentIndex), "test:", tostring(testValue)}
--return allowed_num
