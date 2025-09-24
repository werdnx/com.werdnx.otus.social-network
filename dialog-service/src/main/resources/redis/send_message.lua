-- KEYS[1] = seqKey (dialog:{cid}:seq)
-- KEYS[2] = listKey (dialog:{cid}:messages)
-- ARGV[1] = senderId
-- ARGV[2] = receiverId
-- ARGV[3] = content
-- ARGV[4] = createdAt (epoch millis)

local seqKey = KEYS[1]
local listKey = KEYS[2]

local newId = redis.call('INCR', seqKey)

local msg = cjson.encode({
  id = newId,
  senderId = tonumber(ARGV[1]),
  receiverId = tonumber(ARGV[2]),
  content = ARGV[3],
  createdAt = tonumber(ARGV[4])
})

redis.call('RPUSH', listKey, msg)
return newId
