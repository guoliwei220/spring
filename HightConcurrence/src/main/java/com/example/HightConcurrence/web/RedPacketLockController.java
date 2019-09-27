package com.example.HightConcurrence.web;

import com.example.HightConcurrence.entity.RedPacketRecord;
import com.example.HightConcurrence.service.RedPacketService;
import com.example.HightConcurrence.service.ZkLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RedPacketLockController {
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private ZkLockService zkLockService;

    /**
     * 基于redis的分布式锁
     * @return
     */
    @RequestMapping("getRedPacket")
    public Boolean getRedPacket(){
        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setCreateDate(new Date());
        redPacketRecord.setRedId(UUID.randomUUID().toString());
        redPacketRecord.setUserId("520");
        Boolean redPacket = redPacketService.getRedPacket(redPacketRecord);
        return  redPacket;
    }

    /**
     *基于zookeeper的分布式锁
     * @throws InterruptedException
     */
    @RequestMapping("thread")
    public void thread() throws InterruptedException {
        zkLockService.threadJdk();
    }
}
