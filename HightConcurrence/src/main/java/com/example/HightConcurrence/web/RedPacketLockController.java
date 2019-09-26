package com.example.HightConcurrence.web;

import com.example.HightConcurrence.entity.RedPacketRecord;
import com.example.HightConcurrence.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RedPacketLockController {
    @Autowired
    private RedPacketService redPacketService;

    @RequestMapping("getRedPacket")
    public Boolean getRedPacket(){
        RedPacketRecord redPacketRecord = new RedPacketRecord();
        redPacketRecord.setCreateDate(new Date());
        redPacketRecord.setRedId(UUID.randomUUID().toString());
        redPacketRecord.setUserId("520");
        Boolean redPacket = redPacketService.getRedPacket(redPacketRecord);
        return  redPacket;
    }
}
