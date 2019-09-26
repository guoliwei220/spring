package com.example.HightConcurrence.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RedPacketRecord {

    private Integer id;

    private String redId;

    private String userId;

    private Date createDate;
}
