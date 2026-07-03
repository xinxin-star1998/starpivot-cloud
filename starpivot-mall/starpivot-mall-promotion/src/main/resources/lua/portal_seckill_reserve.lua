local stockKey = KEYS[1]
local limitKey = KEYS[2]
local qty = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])

if qty == nil or qty <= 0 then
  return -3
end

local stock = tonumber(redis.call('GET', stockKey) or '0')
if stock < qty then
  return -1
end

if limit ~= nil and limit > 0 then
  local bought = tonumber(redis.call('GET', limitKey) or '0')
  if bought + qty > limit then
    return -2
  end
end

redis.call('DECRBY', stockKey, qty)
if limit ~= nil and limit > 0 then
  redis.call('INCRBY', limitKey, qty)
  redis.call('EXPIRE', limitKey, 86400)
end
return 1
