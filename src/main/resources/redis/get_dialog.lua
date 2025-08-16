-- KEYS[1] = listKey (dialog:{cid}:messages)
-- ARGV[1] = limit

local listKey = KEYS[1]
local limit = tonumber(ARGV[1]) or 50

local len = redis.call('LLEN', listKey)
if len == 0 then
  return '[]'
end

local start = 0
if len > limit then
  start = len - limit
end

local slice = redis.call('LRANGE', listKey, start, -1)

-- Хотим DESC по времени (последние сверху): разворачиваем
local res = {}
for i = #slice, 1, -1 do
  res[#res + 1] = cjson.decode(slice[i])
end

return cjson.encode(res)
