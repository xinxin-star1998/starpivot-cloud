local reservedKey = KEYS[1]
local qty = tonumber(ARGV[1])
local maxAvailable = tonumber(ARGV[2])
if qty == nil or qty <= 0 then
  return 0
end
if maxAvailable == nil or maxAvailable < 0 then
  maxAvailable = 0
end
local current = tonumber(redis.call('GET', reservedKey) or '0')
if current + qty > maxAvailable then
  return 0
end
redis.call('INCRBY', reservedKey, qty)
return 1
