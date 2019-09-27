package com.example.HightConcurrence.service;

import com.alibaba.fastjson.JSONObject;
import com.example.HightConcurrence.dao.RedPacketRecordMapper;
import com.example.HightConcurrence.entity.RedPacket;
import com.example.HightConcurrence.entity.RedPacketRecord;
import com.example.HightConcurrence.utils.DivideRedPacketUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedPacketService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript redisScript;
    @PostConstruct
    public void init(){
        redisScript = new DefaultRedisScript<List>();
        redisScript.setResultType(List.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redpacket.lua")));
    }
    @Autowired
    private RedPacketRecordMapper redPacketRecordMapper;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 发红包
     * @param order
     * @param totalAcount
     * @param totalPeopleNum
     * @return
     */
    public Boolean sendRedPacket(String order, Integer totalAcount, Integer totalPeopleNum){
        List<Integer> sendRedPacketList = DivideRedPacketUtils.divideRedPacket(totalAcount, totalPeopleNum);
        List<String> redPacketList = new ArrayList<>();
        for(Integer amount : sendRedPacketList){
            BigDecimal redMoney = new BigDecimal(amount).divide(new BigDecimal(100));
            RedPacket redPacket = new RedPacket();
            redPacket.setId(UUID.randomUUID().toString());
            redPacket.setMoney(redMoney);//红包金额
            redPacketList.add(JSONObject.toJSONString(redPacket));
        }
        log.info("发送红包的数据："+redPacketList);
        Long redPacketNum = stringRedisTemplate.opsForList().leftPushAll("redpacket:pool:list:" + order, redPacketList);
        if(redPacketNum > 0){
            return true;
        }
        return false;
    }

    /**
     * 抢红包
     * @param order
     * @param userId
     */
    public Boolean grabRedPacket(String order, String userId){
        List<String> grabRedPacketList = new ArrayList<>();
        //不同的红包
        grabRedPacketList.add("redpacket:pool:list:" + order);
        grabRedPacketList.add("redpacket:detail:list:" + order);
        grabRedPacketList.add("redpacket:hold:list:" + order);
        grabRedPacketList.add(userId);
        List result = (List) stringRedisTemplate.execute(redisScript, grabRedPacketList);
        log.info("抢红包结果："+result);
        String grabResult = result.get(0).toString();
        if("0".equals(grabResult)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * synchronized:线程锁，只适合用于单机
     * @param redPacketRecord
     * @param syncMointer
     * @return
     */
    public  Boolean getRedPacket4(RedPacketRecord redPacketRecord, Object syncMointer){
        //线程锁只适合单机版本，不能在高并发的情况下使用
        synchronized (syncMointer){
                List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                if (redPacketRecords.size() == 0) {
                    int insert = redPacketRecordMapper.insert(redPacketRecord);
                    if (insert > 0) {
                        return true;
                    }
                }
        }
        return false;
    }
    public  Boolean getRedPacket3(RedPacketRecord redPacketRecord){
        //当锁没有删除成功的时候，没有设置失效时间，就会出现死锁的情况
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("redPacket:record:" + redPacketRecord.getRedId(), redPacketRecord.getRedId());
        if(flag){
            try {
                List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                if (redPacketRecords.size() == 0) {
                    int insert = redPacketRecordMapper.insert(redPacketRecord);
                    if (insert > 0) {
                        return true;
                    }
                }
            }finally {
                stringRedisTemplate.delete("redPacket:record:" + redPacketRecord.getRedId());
            }
        }
        return false;
    }

    public  Boolean getRedPacket2(RedPacketRecord redPacketRecord){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("redPacket:record:" + redPacketRecord.getRedId(), redPacketRecord.getRedId());
        //当第一个线程刚设置锁还没有执行下面的失效时间，第二个线程进来就会出现两个数据
        stringRedisTemplate.expire("redPacket:record:" + redPacketRecord.getRedId(), 15, TimeUnit.SECONDS);
        if(flag){
            try {
                List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                if (redPacketRecords.size() == 0) {
                    int insert = redPacketRecordMapper.insert(redPacketRecord);
                    if (insert > 0) {
                        return true;
                    }
                }
            }finally {
                stringRedisTemplate.delete("redPacket:record:" + redPacketRecord.getRedId());
            }
        }
        return false;
    }

    //这样的方式会出现自己的锁被别人解锁
    public  Boolean getRedPacket1(RedPacketRecord redPacketRecord){
        //这样可以解决上面的问题，实现原子性
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("redPacket:record:" + redPacketRecord.getRedId(), redPacketRecord.getRedId(), 15, TimeUnit.SECONDS);
        if(flag){
            try {
                List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                if (redPacketRecords.size() == 0) {
                    int insert = redPacketRecordMapper.insert(redPacketRecord);
                    if (insert > 0) {
                        return true;
                    }
                }
            }finally {
                stringRedisTemplate.delete("redPacket:record:" + redPacketRecord.getRedId());
            }
        }
        return false;
    }
    /**
     * 分布式锁实现领红包
     * @param redPacketRecord
     * @return
     */
    public  Boolean getRedPacket(RedPacketRecord redPacketRecord){
        //当程序执行时间超过锁的失效时间，这时应该有一个监控机制，当程序还没有执行完延长锁的失效时间，否则数据库会出来两条数据
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("redPacket:record:" + redPacketRecord.getRedId(), redPacketRecord.getRedId(), 15, TimeUnit.SECONDS);
        if(flag){
            try {
                List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                if (redPacketRecords.size() == 0) {
                    int insert = redPacketRecordMapper.insert(redPacketRecord);
                    if (insert > 0) {
                        return true;
                    }
                }
            }finally {
                //自己的锁自己解，当上面的程序执行时间超过redis锁的失效时间，锁就会失效，失效后第二个锁会进来，但是第一个线程执到下面得判断是不是自己的锁才可以解锁
                if(redPacketRecord.getRedId().equals(stringRedisTemplate.opsForValue().get("redPacket:record:" + redPacketRecord.getRedId()))){
                    stringRedisTemplate.delete("redPacket:record:" + redPacketRecord.getRedId());
                }
            }
        }
        return false;
    }

    /**
     * 基于redisson实现分布式锁
     * @param redPacketRecord
     * @return
     */
    //下面的锁存在比较极端的情况，当redis分为主从库，当第一个线程访问到主库，然后还没有同步到从库的时候，主库给挂了，
    //然后第二个线程访问进来，从库变成主库后，访问到从库上这时就会出现两个线程同时执行程序
    //解决方案：使用zookeeper实现分布式锁，或者使用redis红锁
    public  Boolean getRedPacketR(RedPacketRecord redPacketRecord){
        //获取分布式锁
        RLock lock = redissonClient.getLock("redPacket:record:" + redPacketRecord.getUserId());
        //添加分布式锁
        try {
            if (lock.tryLock(500, 30000, TimeUnit.MILLISECONDS)) {//两个数字是获取锁的时间，锁过期时间
                try {
                    List<RedPacketRecord> redPacketRecords = redPacketRecordMapper.selectAll(redPacketRecord.getUserId());
                    if (redPacketRecords.size() == 0) {
                        int insert = redPacketRecordMapper.insert(redPacketRecord);
                        if (insert > 0) {
                            return true;
                        }
                    }
                } finally {
                    //释放锁
                    lock.unlock();
                }
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(lock.isHeldByCurrentThread() && lock.isLocked()){
                lock.unlock();
            }
        }
        return false;
    }

    public int limiter(){
        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setCreateDate(new Date());
        redPacketRecord.setRedId(UUID.randomUUID().toString());
        redPacketRecord.setUserId("520");
        int insert = redPacketRecordMapper.insert(redPacketRecord);
        log.info("下单成功==================");
        return insert;
    }
}
