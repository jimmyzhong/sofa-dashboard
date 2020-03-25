local tokens_key = KEYS[1]

local startSeq = tonumber(ARGV[1])
local maxSize = tonumber(ARGV[2])

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

local bids = {}
local gap = maxSize
for i = 1, gap do
    local data = redis.call("hget", tokens_key,  tostring(i - 1 + startSeq))
    if isNull(data) then
        break
    end
    bids[i] = data
end

--bids[1] = "xxx"
--bids[2] = gap

return bids