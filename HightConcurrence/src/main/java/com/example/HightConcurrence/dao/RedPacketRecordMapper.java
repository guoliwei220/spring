package com.example.HightConcurrence.dao;

import com.example.HightConcurrence.entity.RedPacketRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
public interface RedPacketRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RedPacketRecord record);

    RedPacketRecord selectByPrimaryKey(Integer id);

    List<RedPacketRecord> selectAll(@Param("userId") String userId);

    int updateByPrimaryKey(RedPacketRecord record);
}
