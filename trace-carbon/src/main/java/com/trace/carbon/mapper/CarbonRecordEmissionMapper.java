package com.trace.carbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.carbon.entity.CarbonRecordEmission;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 单条农事碳排放明细 Mapper
 */
public interface CarbonRecordEmissionMapper extends BaseMapper<CarbonRecordEmission> {

    @Select("SELECT * FROM carbon_record_emission WHERE record_id = #{recordId} AND deleted = 0 LIMIT 1")
    CarbonRecordEmission selectByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT * FROM carbon_record_emission WHERE batch_id = #{batchId} AND deleted = 0")
    List<CarbonRecordEmission> selectByBatchId(@Param("batchId") Long batchId);
}
