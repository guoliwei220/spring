package com.chngc.collect.entity;

import lombok.Data;
import java.io.Serializable;
@Data
public class GoodsInfo implements Serializable {
    private Long goodsId;
    private Long goodsNo;
    private String storehouseCode;
    private String addTime;
    private static final long serialVersionUID = 1L;
}
