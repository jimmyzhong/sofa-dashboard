local bid_key = KEYS[1]
local users_key = KEYS[2]

local actionKey = ARGV[1] -- #,
local actionValue = ARGV[2] -- ,

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

local function split(szFullString , szSeparator)
    local nFindStartIndex = 1
    local nSplitIndex = 1
    local nSplitArray = {}
    while true do
        local nFindLastIndex = string.find(szFullString, szSeparator, nFindStartIndex)
        if not nFindLastIndex then
            nSplitArray[nSplitIndex] = string.sub(szFullString, nFindStartIndex, string.len(szFullString))
            break
        end
        nSplitArray[nSplitIndex] = string.sub(szFullString, nFindStartIndex, nFindLastIndex - 1)
        nFindStartIndex = nFindLastIndex + string.len(szSeparator)
        nSplitIndex = nSplitIndex + 1
    end
    return nSplitArray
end

local rtValue = -1;

local resultValue
if actionKey == "stopBid" then
    redis.call("hset", bid_key , "canBid" , "N")
end
if actionKey == "startBid" then
    redis.call("hset", bid_key , "canBid" , "Y")
end

if actionKey == "getInfo" then
    local bidId = redis.call("hget", bid_key , "bidId")
    local startPrice = redis.call("hget", bid_key , "startPrice")
    local endPrice = redis.call("hget", bid_key , "endPrice")
    local stepPrice = redis.call("hget", bid_key , "stepPrice")
    local overPrice = redis.call("hget", bid_key , "overPrice")
    local startTime = redis.call("hget", bid_key , "startTime")
    local endTime = redis.call("hget", bid_key , "endTime")
    local createTime = redis.call("hget", bid_key , "createTime")
    local canBid = tostring(redis.call("hget", bid_key , "canBid"))
    local isOver = tostring(redis.call("hget", bid_key , "isOver"))
    resultValue = bidId .. "##" ..startPrice .. "##" .. endPrice.. "##" .. stepPrice
            .. "##" .. overPrice.. "##" .. startTime .. "##" .. endTime.. "##" .. createTime
            .. "##" .. canBid.. "##" .. isOver
end

if actionKey == "addUser" then
    if not isNull(actionValue) then
        local bidUserss = split(actionValue, "##")
        for i=1, #bidUserss do
            local bidUt = bidUserss[i]
            local bidVs = split(bidUt, ",,")
            local uid = bidVs[1]
            local avatar = bidVs[2]
            local nickName = bidVs[3]
            redis.call("hset", users_key , tostring(uid) , uid .. ",," .. avatar .. ",," .. nickName)
        end
    end
end

rtValue = 0

return { rtValue , resultValue}