local bid_key = KEYS[1]
local users_key = KEYS[2]

local upBid = ARGV[1] -- #,
local upUsers = ARGV[2] -- ,

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
local sysTime = redis.call('TIME')[1]
local bidVs = split(upBid, "##")
local bidId = bidVs[1]
local startPrice = tonumber(bidVs[2])
local endPrice = tonumber(bidVs[3])
local stepPrice = tonumber(bidVs[4])
local overPrice = tonumber(bidVs[5])
local startTime = tonumber(bidVs[6])
local endTime = tonumber(bidVs[7])
local avaTime = endTime - startTime + 3000
redis.call("hset", bid_key , "bidId" ,bidId)
redis.call("hset", bid_key , "startPrice" ,startPrice)
redis.call("hset", bid_key , "endPrice" ,endPrice)
redis.call("hset", bid_key , "stepPrice" ,stepPrice)
redis.call("hset", bid_key , "overPrice" ,overPrice)
redis.call("hset", bid_key , "startTime" ,startTime)
redis.call("hset", bid_key , "endTime" ,endTime)
redis.call("hset", bid_key , "createTime" ,sysTime)
redis.call("hset", bid_key , "canBid" , "Y")
redis.call("hset", bid_key , "isOver" , "N")
redis.call("expire", bid_key, avaTime)

local bidUserss = split(upUsers, "##")
local xx = bidUserss[1]
for i=1, #bidUserss do
    local bidUt = bidUserss[i]
    xx = bidUt
    local bidVs = split(bidUt, ",,")
    local uid = bidVs[1]
    local avatar = bidVs[2]
    local nickName = bidVs[3]
    xx = nickName
    redis.call("hset", users_key , tostring(uid) , uid .. ",," .. avatar .. ",," .. nickName)
end
redis.call("expire", users_key, avaTime)

rtValue = 0
--[[for i=1, #list do
    --print(table1[i])
end]]

return { rtValue , "list"}