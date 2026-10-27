package com.trace.carbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trace.carbon.entity.CarbonReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 碳足迹报告 Mapper
 */
public interface CarbonReportMapper extends BaseMapper<CarbonReport> {

    @Select("SELECT * FROM carbon_report WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY report_time DESC LIMIT 1")
    CarbonReport selectLatestByBatchId(@Param("batchId") Long batchId);

    @Select("SELECT * FROM carbon_report WHERE batch_no = #{batchNo} AND deleted = 0 ORDER BY report_time DESC LIMIT 1")
    CarbonReport selectByBatchNo(@Param("batchNo") String batchNo);
}
