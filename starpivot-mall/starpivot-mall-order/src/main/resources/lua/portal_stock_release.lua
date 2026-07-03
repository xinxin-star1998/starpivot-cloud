local reservedKey = KEYS[1]
local qty = tonumber(ARGV[1])
if qty == nil or qty <= 0 then
  return 1
end
local current = tonumber(redis.call('GET', reservedKey) or '0')
local newVal = current - qty
if newVal <= 0 then
  redis.call('DEL', reservedKey)
else
  redis.call('SET', reservedKey, newVal)
end
return 1
