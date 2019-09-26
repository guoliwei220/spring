package com.example.HightConcurrence.web;

import com.example.HightConcurrence.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RedPacketController {
    @Autowired
    private RedPacketService redPacketService;
    /**
     * 发红包
     * @return
     */
    @RequestMapping("sendRedPacket")
    public Boolean sendRedPacket(){
        //保证订单ID是唯一的
        String order = "order-"+ UUID.randomUUID().toString();
        Integer totalAcount = 2000;//单位是分
        Integer totalPeopleNum = 120;//抢红包人数，也就是发红包人数
        Boolean redPacket = redPacketService.sendRedPacket(order, totalAcount, totalPeopleNum);
        return redPacket;
    }

    /**
     * 抢红包
     * @param order
     */
    @RequestMapping("grabRedPacket")
        public Boolean grabRedPacket(@RequestParam("order") String order){
        String userId = UUID.randomUUID().toString();
        Boolean grabFlag = redPacketService.grabRedPacket(order, userId);
        return grabFlag;
    }

}
