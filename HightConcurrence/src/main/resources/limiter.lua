local key = KEYS[1] -- 限流key
local limit = tonumber(ARGV[1]) -- 限流大小10
local current = tonumber(redis.call('get',key) or "0")
if current + 1 > limit then
    return 0 -- 被限流
else  -- 请求数+1，并设置失效时间2s
    redis.call("INCRBY", key, "1")
    redis.call("EXPIRE", key, "2")
    return 1   --没被限流可以正常访问
end