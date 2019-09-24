package com.chngc.collect.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class BusiCollectPriceSpider implements Serializable {
    private Integer id;

    private Integer sourceType;

    private String sourceId;

    private String goodsName;

    private String goodsDesc;

    private Integer goodsQuantity;

    private Date dealEndDate;

    private Date creatDate;

    private BigDecimal dealPrice;

    private BigDecimal totalPrice;

    private Integer tradeType;

    private String tradeUrl;

    private String goodsImageUrl;

    private String currencyYear;

    private Boolean flag;

    private static final long serialVersionUID = 1L;

   }