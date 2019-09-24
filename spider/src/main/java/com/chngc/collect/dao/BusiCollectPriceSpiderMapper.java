package com.chngc.collect.dao;


import com.chngc.collect.entity.BusiCollectPriceSpider;
import com.chngc.core.annotation.SqlMapper;

@SqlMapper
public interface BusiCollectPriceSpiderMapper {
    int insert(BusiCollectPriceSpider record);

    /**
     * 查询当前来源 最新的成交记录
     * @param sourceType
     * @return
     */
    BusiCollectPriceSpider getLastCollect(Integer sourceType);
}