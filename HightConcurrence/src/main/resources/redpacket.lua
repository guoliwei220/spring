--KEYS[1]  redpacket:pool:list:orderid
--KEYS[2]  redpacket:detail:list:orderid
--KEYS[3]  redpacket:hold:list:orderid
--KEYS[4]  抢红包用户id

--查询用户是否已经抢过红包，若已经抢过红包返回1，则表示已经抢过
if redis.call('hexists', KEYS[3], KEYS[4]) ~=0
then
    --如果已经抢过，返回1
    return '1';
else
    --从红包池中弹出一个红包
    local redpacket = redis.call('rpop', KEYS[1])
    --判断从红包池中弹出红包是否为空
    if redpacket
    then
        local x = cjson.decode(redpacket);
        --将用户信息追加到红包信息中，表达该用户已经抢到该红包
        x['userId'] = KEYS[4];
        local redjson = cjson.encode(x);
        --记录用户已经抢过，记录redpacket:hold:hash:{orderId}
        redis.call('hset',KEYS[3], KEYS[4], '1');
        --将红包的结果详情存入redpacket:detail:list:{orderId}
        redis.call('lpush', KEYS[2], redjson);
        return '0';
    else
        --如果红包已经抢完，返回-1；
        return '-1';
    end
end
