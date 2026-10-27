package com.trace.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.chain.entity.ChainRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 链上存证 Mapper
 */
public interface ChainRecordMapper extends BaseMapper<ChainRecord> {

    @Select("SELECT * FROM chain_record WHERE biz_type = #{bizType} AND biz_id = #{bizId} AND deleted = 0 LIMIT 1")
    ChainRecord selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);
}
