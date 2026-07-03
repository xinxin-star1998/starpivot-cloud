local stockKey = KEYS[1]
local limitKey = KEYS[2]
local qty = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])

if qty == nil or qty <= 0 then
  return 0
end

redis.call('INCRBY', stockKey, qty)
if limit ~= nil and limit > 0 then
  local bought = tonumber(redis.call('GET', limitKey) or '0')
  local newBought = bought - qty
  if newBought <= 0 then
    redis.call('DEL', limitKey)
  else
    redis.call('DECRBY', limitKey, qty)
  end
end
return 1
